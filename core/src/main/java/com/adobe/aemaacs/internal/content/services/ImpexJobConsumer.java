package com.adobe.aemaacs.internal.content.services;

import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Session;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.eclipse.jgit.api.Git;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.git.services.GitProfile;
import com.adobe.aemaacs.external.git.services.GitWrapperService;
import com.adobe.aemaacs.external.packaging.services.ExportService;
import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;

@Component(
		enabled = true, 
		service = JobConsumer.class,
		property = { 
				"service.vendor=Adobe Systems" ,
				"job.topics=com/adobe/aemaacs/jobs/impex"
				}
		)
public class ImpexJobConsumer implements JobConsumer {

	@Reference
	private transient ResourceResolverFactory resolverFactory;
	
	@Reference
	private transient SearchService searchService;
	
	@Reference
	private transient ExportService exportService;
	
	@Reference
	private transient GitWrapperService girWrapperService;
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	private static final String CONTENT_UPDATE_PACKAGE_GROUP = "com.adobe.aemaacs.hol";
	
	@Override
	public JobResult process(Job job) {
		try (ResourceResolver resolver = this.resolverFactory.getAdministrativeResourceResolver(null);) {
			String searchPath = job.getProperty("contentRoot", String.class);
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			List<String> filterList = this.searchService.getPages(
					new SearchCriteria(getStartDate(dateFormat), getEndDate(dateFormat), searchPath),
					resolver.adaptTo(Session.class));
			if (filterList.isEmpty()) {
				return JobResult.OK;
			}

			//Read the Cloud config
			ValueMap gitConfigMap = resolver.getResource(job.getProperty("gitConfig", String.class)).getChild("jcr:content").getValueMap();
			
			// Checkout code
			String folderName = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
			String tmpFolder = Files.createTempDirectory(folderName).toString();
			
			GitProfile gitProfile = new GitProfile(gitConfigMap.get("username", String.class),
					gitConfigMap.get("password", String.class), gitConfigMap.get("repoURL", String.class));
			Git git = this.girWrapperService.cloneRepo(gitProfile, tmpFolder);
			
			String branchID = getEndDate(new SimpleDateFormat(TIMESTAMP_FORMAT));
			String branchName = job.getProperty("branchPrefix", String.class).concat("/").concat(branchID);
			git.branchCreate().setName(branchName).setStartPoint(job.getProperty("sourceBranch", String.class)).call();
			git.checkout().setName(branchName).call();
			

			JcrPackage jcrPackage = this.exportService.buildPackage(filterList, resolver,"content-"+branchID, CONTENT_UPDATE_PACKAGE_GROUP);
			Archive archive = this.exportService.getPackageArchive(jcrPackage);
			this.exportService.deserializeEnteries(archive, filterList, tmpFolder);

			for(String filter : filterList) {
				git.add().addFilepattern("ui.content/src/main/content/jcr_root"+filter+"/.content.xml").call();
			}
			git
			.commit()
			.setAuthor(job.getProperty("gitAuthor", String.class), job.getProperty("gitAuthorEmail", String.class))
			.setMessage(job.getProperty("commitMessage", String.class))
			.call();
			
			this.girWrapperService.pushRepo(gitProfile, git, branchName);
			git.close();
			
			return JobResult.OK;
		} catch (Exception e) {
			return JobResult.FAILED;
		}
	}

	private String getStartDate(DateFormat dateFormat) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		return dateFormat.format(calendar.getTime());
	}
	
	private String getEndDate(DateFormat dateFormat) {
		Calendar calendar = Calendar.getInstance();
		return dateFormat.format(calendar.getTime());
	}
}
