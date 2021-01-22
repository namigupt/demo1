package com.adobe.aemaacs.external.packaging.services;

import com.adobe.aemaacs.internal.common.exception.BaseException;

public class ImpexException extends BaseException {

	private static final long serialVersionUID = -3539692796953277913L;

	public ImpexException(String message, String eCode) {
		super(message, eCode);
	}

}
