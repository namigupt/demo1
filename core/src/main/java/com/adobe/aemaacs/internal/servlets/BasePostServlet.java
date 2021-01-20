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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasePostServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -7809747322538401363L;
	private static final Logger log = LoggerFactory.getLogger(BasePostServlet.class);

	/**
	 * Gets the param.
	 *
	 * @param request the request
	 * @param param   the param
	 * @return the param
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getParam(SlingHttpServletRequest request, String param) throws IOException {
		String value = request.getParameter(param);
		if (StringUtils.isBlank(value)) {
			throw new IOException("Parameter " + param + " must not be blank");
		} else {
			log.debug("Loaded {} for parameter {}", value, param);
		}
		return value;
	}

}
