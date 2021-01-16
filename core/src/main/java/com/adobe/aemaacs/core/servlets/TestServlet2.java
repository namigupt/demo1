/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aemaacs.core.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.util.DefaultProgressListener;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.packaging.services.ExportService;
import com.adobe.aemaacs.external.search.services.SearchCriteria;
import com.adobe.aemaacs.external.search.services.SearchService;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(
        immediate = true,
        service = Servlet.class,
        property = {

                "sling.servlet.paths=/bin/services/demo",
                "sling.servlet.methods=GET"
        }
)
public class TestServlet2 extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    
    @Reference
    SearchService searchService;
    
    @Reference
    ExportService exportService;

    @Override
    protected void doGet(final SlingHttpServletRequest request,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
    	

		try {
			SearchCriteria searchCriteria = new SearchCriteria("2021-01-01", "2021-01-16", "/content/we-retail");
			List<String> pageList = this.searchService.getPages(searchCriteria, request.getResourceResolver().adaptTo(Session.class));
			JcrPackage jcrPackage = this.exportService.buildPackage(pageList, request.getResourceResolver());
			
			Archive archive = this.exportService.getPackageArchive(jcrPackage);
			this.exportService.deserializeEnteries(archive, pageList, "C:/work/demo2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
