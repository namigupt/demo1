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
import java.util.Calendar;
import java.util.Map.Entry;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.HtmlResponse;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;

@Component(
		service = Servlet.class, 
		property = { 
				"sling.servlet.methods=POST",
				"sling.servlet.resourceTypes=aemaacs-administration-app/components/admin/git-cloud-config",
				"sling.servlet.resourceTypes=aemaacs-administration-app/components/admin/impexjobconfig"
				}
		)
public class ConfigUpdateServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -9002991183379329625L;
	private static final Logger log = LoggerFactory.getLogger(ConfigUpdateServlet.class);
	
	
	/**
	 * {@inheritDoc}
	 * @throws IOException 
	 */
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
		ResourceResolver resolver = request.getResourceResolver();
		HtmlResponse resp = new HtmlResponse();
		try {
			// Setting the configuration values
			ModifiableValueMap valueMap = request.getResource().adaptTo(ModifiableValueMap.class);
			for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				valueMap.put(entry.getKey(),
						entry.getValue() == null || entry.getValue().length == 0 ? "" : entry.getValue()[0]);
			}
			valueMap.put(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
			if (resolver.hasChanges()) {
				resolver.commit();
			}
			response.setContentType("text/plain");
			resp.send(response, true);
		} catch (IOException e) {
			resp.setStatus(500, e.getMessage());
			resp.send(response, true);
			log.error("Unable to update/create Sync job.");
		}


	}


}
