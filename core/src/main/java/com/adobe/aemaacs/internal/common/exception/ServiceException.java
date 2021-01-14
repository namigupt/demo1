package com.adobe.aemaacs.internal.common.exception;

/**
 * 
 * This is the business exception for scenarios like;
 * <ul>
 * 		<li>Vault configuration not initialized.</li>
 * 		<li>Key/Value Pair not found.</li>
 * </ul>
 * 
 *
 * @author Demo Team
 * @version 1.0
 * @since 2018.10.30
 * 
 * ******************************************************************************
 *          		Version History
 * ******************************************************************************
 * 
 *          1.0 	Initial Version 	Demo Team
 * 
 * *******************************************************************************
 */
public class ServiceException extends BaseException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -88963611648936552L;


	/**
	 * Instantiates a new vault exception.
	 *
	 * @param errors the errors
	 * @param e {@link Throwable} the e
	 */
	public ServiceException(String errors, Throwable e) {
		super(errors, e);
	}

	/**
	 * Parameterized constructor. Takes exception message, code and object as
	 * argument
	 * 
	 * @param message exception message.
	 * @param eCode   exception error code
	 */
	public ServiceException(String message, String eCode) {
		super(message, eCode);
	}
	
	/**
	 * Parameterized constructor. Takes exception message, code and object as
	 * argument
	 * 
	 * @param message exception message.
	 * @param eCode   exception error code
	 */
	public ServiceException(String message, String eCode, Throwable e) {
		super(message, eCode, e);
	}
}
