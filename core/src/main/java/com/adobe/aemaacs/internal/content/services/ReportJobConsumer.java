package com.adobe.aemaacs.internal.content.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.packaging.services.ExportService;
import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;

@Component(
		enabled = true, 
		service = JobConsumer.class,
		property = { 
				"service.vendor=Adobe Systems" ,
				"job.topics=com/adobe/aemaacs/jobs/content/report"
				}
		)
public class ReportJobConsumer extends AbstractJobConsumer implements JobConsumer {

	@Reference
	private ResourceResolverFactory resolverFactory;
	
	@Reference
	private SearchService searchService;
	
	@Reference
	private ExportService exportService;
	
	private static final String AUDIT_EVENTS_STORAGE_PAGE_EVENTS = "/var/audit/com.day.cq.wcm.core.page";
	private static final String AUDIT_EVENTS_STORAGE_ASSET_EVENTS = "/var/audit/com.day.cq.dam";
	private static final String PAGES = "pages";
	private static final String ASSETS = "assets";
	
	@Override
	public JobResult process(Job job) {
		Map<String, Object> param = new HashMap<>();
		param.put(ResourceResolverFactory.SUBSERVICE, "read-write-service");
		String artifactType = StringUtils.startsWith(job.getProperty("contentRoot", String.class), "/content/dam")?ASSETS:PAGES;
		
		try (ResourceResolver resolver = this.resolverFactory.getServiceResourceResolver(param);) {
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			Session session = resolver.adaptTo(Session.class);

			SearchCriteria searchCriteria = new SearchCriteria(formatDate(dateFormat, -7), formatDate(dateFormat, 1),
					job.getProperty("contentRoot", String.class));
			searchCriteria.setEventType(artifactType);
			List<String> addedFiles = this.searchService.getArtifacts(searchCriteria, session);

			searchCriteria.setEventType(artifactType.equals(PAGES) ? "PageDeleted" : "ASSET_REMOVED");
			searchCriteria.setSearchPath(artifactType.equals(PAGES)
					? AUDIT_EVENTS_STORAGE_PAGE_EVENTS.concat(searchCriteria.getSearchPath())
					: AUDIT_EVENTS_STORAGE_ASSET_EVENTS.concat(searchCriteria.getSearchPath()));
			
			List<String> deletedFilterList = this.searchService.getDeletedArtifacts(searchCriteria, session);
			if (addedFiles.isEmpty() && deletedFilterList.isEmpty()) {
				return JobResult.OK;
			}
			PackageId jcrPackage = exportService.buildPackage(addedFiles, resolver,
					"content-" + System.currentTimeMillis(), CONTENT_UPDATE_PACKAGE_GROUP);
			
			//commit report results
			ModifiableValueMap modifiableValueMap = resolver.getResource(job.getProperty("path", String.class)).getChild(JcrConstants.JCR_CONTENT).adaptTo(ModifiableValueMap.class);
			modifiableValueMap.put("package", (null == jcrPackage )? StringUtils.EMPTY : "/etc/packages/".concat(jcrPackage.getGroup()).concat("/").concat(jcrPackage.getDownloadName()));
			modifiableValueMap.put("addedFiles", addedFiles.toArray());
			modifiableValueMap.put("deletedFiles",deletedFilterList.toArray());
			if(resolver.hasChanges()) {
				resolver.commit();
			}
			return JobResult.OK;
		} catch (IOException | LoginException e) {
			return JobResult.FAILED;
		}
	}

}
