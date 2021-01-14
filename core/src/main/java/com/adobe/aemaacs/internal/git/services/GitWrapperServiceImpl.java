package com.adobe.aemaacs.internal.git.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.osgi.service.component.annotations.Component;

import com.adobe.aemaacs.external.git.services.GitProfile;
import com.adobe.aemaacs.external.git.services.GitWrapperService;

@Component(immediate = true, service = GitWrapperService.class)
public class GitWrapperServiceImpl implements GitWrapperService {

	@Override
	public Git cloneRepo(GitProfile gitProfile) {
		String folderName = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
		try {
			String tmpFolder = Files.createTempDirectory(folderName).toString();
			Git git = Git.cloneRepository().setURI(gitProfile.getRepository())
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(gitProfile.getUserName(), gitProfile.getPassword()))
					.setDirectory(new File(tmpFolder)).call();
			return git;
		} catch (IOException | GitAPIException e) {
			throw new GitException(e.getMessage(), "GIT101");
		}
	}

	@Override
	public void addArtifact(String pattern, Git git) {
		try {
			git.add().addFilepattern(pattern).call();
		} catch (Exception e) {
			throw new GitException(e.getMessage(), "GIT102");
		}
	}

	@Override
	public void addArtifacts(String[] patterns, Git git) {
		try {
			for (String pattern : patterns) {
				git.add().addFilepattern(pattern).call();
			}
		} catch (Exception e) {
			throw new GitException(e.getMessage(), "GIT102");
		}
	}

	@Override
	public void pushRepo(GitProfile gitProfile, Git git) {
		try {
			git.commit().call();
			git.push()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(gitProfile.getUserName(), gitProfile.getPassword()))
					.setRemote("origin").setRefSpecs(new RefSpec("demo:demo")).call();
		} catch (Exception e) {
			throw new GitException(e.getMessage(), "GIT103");
		}

	}

}
