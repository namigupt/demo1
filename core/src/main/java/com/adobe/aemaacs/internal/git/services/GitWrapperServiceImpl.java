package com.adobe.aemaacs.internal.git.services;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
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
	public Git cloneRepo(GitProfile gitProfile, String tmpFolder) {
		try {
			return Git.cloneRepository().setURI(gitProfile.getRepository())
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(gitProfile.getUserName(), gitProfile.getPassword()))
					.setDirectory(Paths.get(System.getProperty("java.io.tmpdir"),
							FilenameUtils.getName(tmpFolder)).toFile())
					.call();
		} catch (GitAPIException e) {
			throw new GitException(e.getMessage(), "GIT101");
		}
	}

	@Override
	public void addArtifact(String pattern, Git git) {
		try {
			git.add().addFilepattern(pattern).call();
		} catch (GitAPIException e) {
			throw new GitException(e.getMessage(), "GIT102");
		}
	}

	@Override
	public void addAndCommitArtifacts(String[] patterns, Git git) {
		try {
			for (String pattern : patterns) {
				git.add().addFilepattern(pattern).call();
			}
			git.commit().call();
		} catch (GitAPIException e) {
			throw new GitException(e.getMessage(), "GIT102");
		}
	}

	@Override
	public void pushRepo(GitProfile gitProfile, Git git, String targetBranch) {
		try {
			git.push()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(gitProfile.getUserName(), gitProfile.getPassword()))
					.setRemote("origin").setRefSpecs(new RefSpec(targetBranch + ":" + targetBranch)).call();
		} catch (GitAPIException e) {
			throw new GitException(e.getMessage(), "GIT103");
		}
	}

}
