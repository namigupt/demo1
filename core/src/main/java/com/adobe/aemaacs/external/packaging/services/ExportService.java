package com.adobe.aemaacs.external.packaging.services;

import java.util.List;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.sling.api.resource.ResourceResolver;

public interface ExportService {
	
	JcrPackage buildPackage(List<String> filters, ResourceResolver resolver);

	void deserializeEnteries(Archive archive, List<String> filterList, String sourceCodeWorkspace);

	void deserializeEntry(Archive archive, String filter, String sourceCodeWorkspace);

	Archive getPackageArchive(JcrPackage jcrPackage);

}
