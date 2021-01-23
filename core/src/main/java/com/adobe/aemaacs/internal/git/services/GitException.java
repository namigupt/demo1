package com.adobe.aemaacs.internal.git.services;

import com.adobe.aemaacs.internal.common.exception.BaseException;

public class GitException extends BaseException {

	private static final long serialVersionUID = -3539692796953277913L;

	public GitException(String message, String eCode) {
		super(message, eCode);
	}

}
