package com.adobe.aemaacs.external.ui;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

@Model(adaptables = SlingHttpServletRequest.class)
public class ReportModel {

	@Self
	private SlingHttpServletRequest request;
	
	private String jcrPackage;
	private List<String> addedFiles;
	private List<String> DeletedFiles;

	@PostConstruct
	protected void init() {
		String item = request.getParameter("item");
		if(StringUtils.isBlank(item)) {
			return;
		}
		ResourceResolver resolver = this.request.getResourceResolver();
		ValueMap valueMap = resolver.getResource(item).getChild("jcr:content").getValueMap();
		this.jcrPackage = valueMap.containsKey("package") ? valueMap.get("package", String.class):null;
		this.addedFiles = valueMap.containsKey("addedFiles") ? Arrays.asList(valueMap.get("addedFiles", String[].class)) : null;
		this.DeletedFiles = valueMap.containsKey("deletedFiles") ? Arrays.asList(valueMap.get("deletedFiles", String[].class)) : null;
	}

	public String getJcrPackage() {
		return this.jcrPackage;
	}

	public List<String> getAddedFiles() {
		return addedFiles;
	}

	public List<String> getDeletedFiles() {
		return DeletedFiles;
	}

}