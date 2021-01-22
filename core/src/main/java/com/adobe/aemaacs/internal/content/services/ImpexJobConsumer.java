package com.adobe.aemaacs.internal.content.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.osgi.framework.ServiceException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.git.services.GitProfile;
import com.adobe.aemaacs.external.git.services.GitWorkspace;
import com.adobe.aemaacs.external.git.services.GitWrapperService;
import com.adobe.aemaacs.external.packaging.services.ExportService;
import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;

@Component(
		enabled = true, 
		service = JobConsumer.class,
		property = { 
				"service.vendor=Adobe Systems" ,
				"job.topics=com/adobe/aemaacs/jobs/impex/pages",
				"job.topics=com/adobe/aemaacs/jobs/impex/assets"
				}
		)
public class ImpexJobConsumer extends AbstractJobConsumer implements JobConsumer {

	@Reference
	private  ResourceResolverFactory resolverFactory;
	
	@Reference
	private  SearchService searchService;
	
	@Reference
	private  ExportService exportService;
	
	@Reference
	private  GitWrapperService gitWrapperService;
	
	@Reference
	private  Packaging packagingService;
	
	private static final String AUDIT_EVENTS_STORAGE_PAGE_EVENTS = "/var/audit/com.day.cq.wcm.core.page";
	private static final String AUDIT_EVENTS_STORAGE_ASSET_EVENTS = "/var/audit/com.day.cq.dam";
	
	@Override
	public JobResult process(Job job) {
		Map<String, Object> param = new HashMap<>();
		param.put(ResourceResolverFactory.SUBSERVICE, "read-write-service");
		String artifactType = StringUtils.substringAfterLast(job.getTopic(), "/");
		try (ResourceResolver resolver = this.resolverFactory.getServiceResourceResolver(param);) {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			Session session = resolver.adaptTo(Session.class);
			
			SearchCriteria searchCriteria = new SearchCriteria(formatDate(dateFormat, -7),
					formatDate(dateFormat, 1), job.getProperty("contentRoot", String.class));
			searchCriteria.setEventType(artifactType);
			List<String> addedFiles = this.searchService.getArtifacts(searchCriteria, session);

			searchCriteria.setEventType(artifactType.equals("pages") ? "PageDeleted" : "ASSET_REMOVED");
			searchCriteria.setSearchPath(artifactType.equals("pages")
					? AUDIT_EVENTS_STORAGE_PAGE_EVENTS.concat(searchCriteria.getSearchPath())
					: AUDIT_EVENTS_STORAGE_ASSET_EVENTS.concat(searchCriteria.getSearchPath()));
			List<String> deletedFilterList = this.searchService.getDeletedArtifacts(searchCriteria, session);

			if (addedFiles.isEmpty() && deletedFilterList.isEmpty()) {
				return JobResult.OK;
			}
			
			GitProfile gitProfile = super.getGitProfile(job, resolver);
			GitWorkspace workspace = super.checkoutCode(gitWrapperService, gitProfile, job);
			try (Git git = workspace.getGitRepo()) {
				PackageId packageId = exportService.buildPackage(addedFiles, resolver,
						"content-" + workspace.getBranchID(), CONTENT_UPDATE_PACKAGE_GROUP);

				final JcrPackageManager jcrPackageManager = this.packagingService.getPackageManager(session);
				try(JcrPackage jcrPackage = jcrPackageManager.open(packageId);){
				VaultPackage vaultPackage = (null != jcrPackage) ?jcrPackage.getPackage():null;
				if(null == vaultPackage) {
					throw new ServiceException("Unable to open content package");
				}
				try (Archive archive = vaultPackage.getArchive();) {

					super.commitArtifacts(exportService, addedFiles, deletedFilterList, git,
							workspace.getSourceFolder(), archive, artifactType);

					git.commit()
							.setAuthor(job.getProperty("gitAuthor", String.class),
									job.getProperty("gitAuthorEmail", String.class))
							.setMessage(job.getProperty("commitMessage", String.class)).call();

					this.gitWrapperService.pushRepo(gitProfile, git, workspace.getBranchName());
				}
			}
			}
			super.cleanup(workspace);
			return JobResult.OK;
		} catch (IOException | GitAPIException | LoginException | RepositoryException e) {
			return JobResult.FAILED;
		}
	}

}
