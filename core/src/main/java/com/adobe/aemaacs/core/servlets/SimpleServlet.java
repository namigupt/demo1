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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.osgi.service.component.annotations.Component;

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

                "sling.servlet.paths=/bin/services/demo1",
                "sling.servlet.methods=GET"
        }
)
public class SimpleServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
    	

		try {
			String tmpFolder = Files.createTempDirectory("demo").toString();
			Git git = Git.cloneRepository().setURI("https://git.corp.adobe.com/namigupt/aep-ds-integration.git")
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider("namigupt", "bf9ac7ce648951ce5ffd718a1cd9ee09854a6fdb"))
					.setDirectory(new File(tmpFolder)).call();
			// Ref ref = repository.checkout().setName("master").call();
			Repository repository = git.getRepository();
			Ref branch = git.branchCreate().setName("demo").setStartPoint("origin/develop")
					.call();
			git.checkout().setName("demo").call();
			
			  Files.write(
				        Paths.get(tmpFolder+"\\demo.txt"),
				        "hello world".getBytes(StandardCharsets.UTF_8));
			
			git.add().addFilepattern("demo.txt").call();
			git.commit().setAuthor("Nam", "namigupt@adobe.com").setMessage("demo").call();
			
			git.push()
			.setCredentialsProvider(
					new UsernamePasswordCredentialsProvider("namigupt", "bf9ac7ce648951ce5ffd718a1cd9ee09854a6fdb"))
			.setRemote("origin")
			.setRefSpecs(new RefSpec("demo:demo"))
			.call();
			
			git.close();
		} catch (RevisionSyntaxException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
