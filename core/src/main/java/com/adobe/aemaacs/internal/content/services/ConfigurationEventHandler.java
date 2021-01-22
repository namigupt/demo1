package com.adobe.aemaacs.internal.content.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageEvent;
import com.day.cq.wcm.api.PageModification;
import com.day.cq.wcm.api.PageModification.ModificationType;

@Component(
		immediate = true, 
		property = { 
				"event.topics="+PageEvent.EVENT_TOPIC
				}
		)
public class ConfigurationEventHandler implements EventHandler {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    private JobManager jobManager;
    
    @Reference
    private ResourceResolverFactory resolverFactory;

	@Override
	public void handleEvent(Event event) {
		PageEvent pageEvent = PageEvent.fromEvent(event);
		Iterator<PageModification> iterator = pageEvent.getModifications();
		while (iterator.hasNext()) {
			PageModification pageModification = iterator.next();
			ModificationType type = pageModification.getType();
			String path = pageModification.getPath();
			log.debug("Modification Type: {}", pageModification.getType());
			if (StringUtils.startsWith(path, "/conf")
					&& StringUtils.contains(path, "/settings/jobs/")) {
				
				Map<String, Object> param = new HashMap<>();
				param.put(ResourceResolverFactory.SUBSERVICE, "read-write-service");
				
				try (ResourceResolver resolver = this.resolverFactory.getServiceResourceResolver(param);) {

					switch (type) {

					case CREATED:
						addJob(path, resolver);
						break;

					case MODIFIED:
						updateJob(path, resolver);
						break;

					case DELETED:
						removejob(path);
						break;

					default:
						break;
					}
				} catch (LoginException e) {
					log.error("unable to create job", e);
				}
			}
		}
	}
	
	private void addJob(String path, ResourceResolver resolver) {
		ValueMap valueMap = resolver.getResource(path).getChild(JcrConstants.JCR_CONTENT).getValueMap();
		Map<String, Object> properties = new HashMap<>();
		properties.putAll(valueMap);
		properties.put("jobID", path);
		JobBuilder.ScheduleBuilder scheduleBuilder = this.jobManager
				.createJob(StringUtils.startsWith(valueMap.get("contentRoot", String.class), "/content/dam/")
						? "com/adobe/aemaacs/jobs/impex/assets"
						: "com/adobe/aemaacs/jobs/impex/pages")
				.properties(properties).schedule();
		scheduleBuilder.cron(valueMap.get("frequency", String.class));
		scheduleBuilder.add();
	}
	private void removejob(String jobID) {
		Collection<ScheduledJobInfo> scheduledJobInfos = this.jobManager.getScheduledJobs();
		for (ScheduledJobInfo scheduledJobInfo : scheduledJobInfos) {
			Map<String, Object> properties = scheduledJobInfo.getJobProperties();
			if(StringUtils.equalsIgnoreCase(jobID, properties.getOrDefault("jobID", StringUtils.EMPTY).toString())) {
				scheduledJobInfo.unschedule();
				break;
			}
		}
	}
	
	private void updateJob(String path, ResourceResolver resolver) {
		removejob(path);
		addJob(path, resolver);
	}
}