
package com.adobe.aemaacs.external.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.jcr.query.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.lang.annotations.NotNull;

@Model(adaptables = SlingHttpServletRequest.class, adapters = { ConfigurationList.class })
public class ConfigurationListImpl implements ConfigurationList {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationListImpl.class);

	private List<Configuration> configs = new ArrayList<>();

	public ConfigurationListImpl(SlingHttpServletRequest slingRequest) {

		String template = Optional.ofNullable(slingRequest.getRequestPathInfo().getSuffix()).orElse("");

		if (StringUtils.isNotBlank(template)) {
			String query = "SELECT * FROM [cq:Page] WHERE ISDESCENDANTNODE([/conf]) AND [jcr:content/cq:template]='"
					+ template.replace("'", "''") + "'";
			log.debug("Finding configuerations with: {}", query);

			slingRequest.getResourceResolver().findResources(query, Query.JCR_SQL2).forEachRemaining(ccr -> 
				configs.add(ccr.adaptTo(Configuration.class))
			);
		} else {
			log.debug("Suffix not specified");

		}
	}

	@NotNull
	@Override
	public List<Configuration> getCloudConfigurations() {
		return Collections.unmodifiableList(this.configs);
	}
}