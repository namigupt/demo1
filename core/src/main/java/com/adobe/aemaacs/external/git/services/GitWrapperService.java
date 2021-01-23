package com.adobe.aemaacs.external.git.services;

import org.eclipse.jgit.api.Git;

public interface GitWrapperService {
	
	Git cloneRepo(GitProfile gitProfile, String tmpFolder);
	
	void addArtifact(String pattern, Git git);

	void addAndCommitArtifacts(String[] patterns, Git git);

	void pushRepo(GitProfile gitProfile, Git git, String targetBranch);

}
