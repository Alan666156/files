package com.files.client.exception;

public class SystemException extends Exception {
	private static final long serialVersionUID = 4086346391981872887L;
	
	public SystemException(String msg) {
		super(msg);
	}

	public SystemException(String msg, Exception e) {
		super(msg, e);
	}
}
