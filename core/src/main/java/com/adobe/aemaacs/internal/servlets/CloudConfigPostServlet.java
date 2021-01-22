/*
 * #%L
 * Hootsuite Integration
 * %%
 * Copyright 2020 Adobe. All rights reserved.
 * %%
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.aemaacs.internal.servlets;

import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.servlets.post.HtmlResponse;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

@Component(
		service = Servlet.class, 
		property = { 
				"sling.servlet.methods=POST",
				"sling.servlet.resourceTypes=aemaacs-administration-app/components/admin/cloudconfiglist" 
				}
		)
public class CloudConfigPostServlet extends BasePostServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CloudConfigPostServlet.class);
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
		ResourceResolver resolver = request.getResourceResolver();
		PageManager pageManager = resolver.adaptTo(PageManager.class);

		HtmlResponse resp = new HtmlResponse();

		if (pageManager == null) {
			resp.setError(new IOException("Unable to get page manager"));
		} else {

			String configPath = getParam(request, "configPath");
			Resource cloudConfigFolder = ResourceUtil.getOrCreateResource(resolver,
					configPath + "/settings/cloudconfigs",
					Collections.singletonMap(JcrConstants.JCR_PRIMARYTYPE, JcrResourceConstants.NT_SLING_FOLDER),
					JcrResourceConstants.NT_SLING_FOLDER, false);
			log.debug("Creating Cloud Config in: {}", cloudConfigFolder);

			resp.setParentLocation(cloudConfigFolder.getPath());

			// create a new page
			try {
				Page page = pageManager.create(cloudConfigFolder.getPath(), super.getParam(request, "name"),
						getParam(request, "template"), super.getParam(request, "title"));
				resp.setPath(page.getPath());
				resp.setLocation(page.getPath());
				resp.setStatus(200, "Created Cloud Configuration");
				log.debug("Created configuration: {}", page.getPath());
				resolver.commit();
				
				//Setting the configuration values
				ModifiableValueMap valueMap = page.getContentResource().adaptTo(ModifiableValueMap.class);
				for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
					valueMap.put(entry.getKey(),
							entry.getValue() == null || entry.getValue().length == 0 ? "" : entry.getValue()[0]);
				}
				if(resolver.hasChanges()) {
					resolver.commit();
				}
			} catch (WCMException e) {
				resp.setError(e);
			}

		}
		response.setContentType("text/plain");
		resp.send(response, true);

	}

}
