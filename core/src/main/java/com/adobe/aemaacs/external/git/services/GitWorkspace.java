package com.adobe.aemaacs.external.git.services;

import org.eclipse.jgit.api.Git;

public class GitWorkspace {

	private String sourceFolder;
	private String branchID;
	private String branchName;
	private Git gitRepo;

	public GitWorkspace(String sourceFolder, String branchID, String branchName, Git gitRepo) {
		super();
		this.sourceFolder = sourceFolder;
		this.branchID = branchID;
		this.branchName = branchName;
		this.gitRepo = gitRepo;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}


	public String getBranchID() {
		return branchID;
	}


	public Git getGitRepo() {
		return gitRepo;
	}


	public String getBranchName() {
		return branchName;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchID == null) ? 0 : branchID.hashCode());
		result = prime * result + ((branchName == null) ? 0 : branchName.hashCode());
		result = prime * result + ((gitRepo == null) ? 0 : gitRepo.hashCode());
		result = prime * result + ((sourceFolder == null) ? 0 : sourceFolder.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GitWorkspace other = (GitWorkspace) obj;
		if (branchID == null) {
			if (other.branchID != null)
				return false;
		} else if (!gitRepo.equals(other.gitRepo))
			return false;
		if (sourceFolder == null) {
			if (other.sourceFolder != null)
				return false;
		} else if (!sourceFolder.equals(other.sourceFolder))
			return false;
		return true;
	}

}
