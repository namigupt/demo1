package com.adobe.aemaacs.external.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
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
	private List<String> addedFiles = new ArrayList<>();
	private List<String> deletedFiles = new ArrayList<>();

	@PostConstruct
	protected void init() {
		String item = request.getParameter("item");
		if(StringUtils.isBlank(item)) {
			return;
		}
		ResourceResolver resolver = this.request.getResourceResolver();
		ValueMap valueMap = resolver.getResource(item).getChild(JcrConstants.JCR_CONTENT).getValueMap();
		this.jcrPackage = valueMap.containsKey("package") ? valueMap.get("package", String.class):null;
		if(valueMap.containsKey("addedFiles")) 
			this.addedFiles.addAll(Arrays.asList(valueMap.get("addedFiles", String[].class)));
		if(valueMap.containsKey("DeletedFiles")) 
			this.deletedFiles.addAll(Arrays.asList(valueMap.get("deletedFiles", String[].class)));
	}

	public String getJcrPackage() {
		return this.jcrPackage;
	}

	public List<String> getAddedFiles() {
		return Collections.unmodifiableList(this.addedFiles);
	}

	public List<String> getDeletedFiles() {
		return Collections.unmodifiableList(this.deletedFiles);
	}

}