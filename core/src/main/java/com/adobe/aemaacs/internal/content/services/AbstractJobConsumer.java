package com.adobe.aemaacs.internal.content.services;

import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;

import com.adobe.aemaacs.external.git.services.GitProfile;
import com.adobe.aemaacs.external.git.services.GitWorkspace;
import com.adobe.aemaacs.external.git.services.GitWrapperService;
import com.adobe.aemaacs.external.packaging.services.ExportService;

public abstract class AbstractJobConsumer {

	protected static final String DATE_FORMAT = "yyyy-MM-dd";
	protected static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	protected static final String CONTENT_UPDATE_PACKAGE_GROUP = "com.adobe.aemaacs.hol";

	protected String formatDate(DateFormat dateFormat, int offset) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, offset);
		return dateFormat.format(calendar.getTime());
	}

	protected GitProfile getGitProfile(Job job, ResourceResolver resolver) {
		ValueMap gitConfigMap = resolver.getResource(job.getProperty("gitConfig", String.class)).getChild("jcr:content")
				.getValueMap();
		return new GitProfile(gitConfigMap.get("username", String.class), gitConfigMap.get("password", String.class),
				gitConfigMap.get("repoURL", String.class));

	}

	protected GitWorkspace checkoutCode(GitWrapperService gitWrapperService, GitProfile gitProfile, Job job) throws IOException, RefAlreadyExistsException,
			RefNotFoundException, InvalidRefNameException, GitAPIException {
		// Checkout code
		String folderName = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
		String tmpFolder = Files.createTempDirectory(folderName).toString();
		Git git = gitWrapperService.cloneRepo(gitProfile, tmpFolder);
		String branchID = formatDate(new SimpleDateFormat(TIMESTAMP_FORMAT), 0);
		String branchName = job.getProperty("branchPrefix", String.class).concat("/").concat(branchID);
		git.branchCreate().setName(branchName).setStartPoint(job.getProperty("sourceBranch", String.class)).call();
		git.checkout().setName(branchName).call();
		return new GitWorkspace(tmpFolder, branchID, branchName, git);
	}

	protected void commitArtifacts(ExportService exportService, List<String> addedFiles, List<String> deletedFiles, Git git,
			String sourceFolder, Archive archive, String artifactType) {
		addArtifacts(addedFiles, git, artifactType,archive, sourceFolder, exportService);
		deleteArtifacts(addedFiles, deletedFiles, git, artifactType);
	}

	private void deleteArtifacts(List<String> addedFiles, List<String> deletedFiles, Git git, String artifactType) {
		deletedFiles.stream().filter(item -> !addedFiles.contains(item)).forEach(item -> {
			try {
				git.rm().addFilepattern("ui.content/src/main/content/jcr_root" + item + "/.content.xml").call();

				switch (artifactType) {
				case "assets":
					git.rm().addFilepattern(
							"ui.content/src/main/content/jcr_root" + item + "/_jcr_content/renditions/original").call();
					break;

				default:
					break;
				}
			} catch (GitAPIException e) {

			}

		});
	}

	private void addArtifacts(List<String> addedFiles, Git git, String artifactType, Archive archive, String sourceFolder, ExportService exportService) {
		for (String item : addedFiles) {
			try {
				exportService.deserializeEntry(archive, item, sourceFolder, "", ".content.xml");
				git.add().addFilepattern("ui.content/src/main/content/jcr_root" + item + "/.content.xml").call();

				switch (artifactType) {
				case "assets":
					exportService.deserializeEntry(archive, item, sourceFolder, "_jcr_content/renditions/", "original");
					git.add()
							.addFilepattern(
									"ui.content/src/main/content/jcr_root" + item + "/_jcr_content/renditions/original")
							.call();
					break;

				default:
					break;
				}
			} catch (Exception e) {
				continue;
			}
		}
	}
}
