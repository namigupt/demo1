package com.adobe.aemaacs.internal.content.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Session;

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
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	@Override
	public JobResult process(Job job) {
		try (ResourceResolver resolver = this.resolverFactory.getAdministrativeResourceResolver(null);) {
			String searchPath = job.getProperty("contentRoot", String.class);
			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			List<String> filterList = this.searchService.getPages(
					new SearchCriteria(getStartDate(dateFormat), getEndDate(dateFormat), searchPath),
					resolver.adaptTo(Session.class));
			this.exportService.buildPackage(filterList, resolver);
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
