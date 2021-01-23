package com.adobe.aemaacs.external.packaging.services;

import java.util.List;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.sling.api.resource.ResourceResolver;

public interface ExportService {
	
	PackageId buildPackage(List<String> filters, ResourceResolver resolver, String string, String contentUpdatePackageGroup);

	void deserializeEnteries(Archive archive, List<String> filterList, String sourceCodeWorkspace,String intermediatePath,String name);

	void deserializeEntry(Archive archive, String filter, String sourceCodeWorkspace, String intermediatePath,
			String name);

}
