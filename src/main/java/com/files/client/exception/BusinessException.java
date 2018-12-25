package com.files.client.exception;

public class BusinessException extends Exception {

	private static final long serialVersionUID = -3774674184634867724L;

	public BusinessException(String msg) {
		super(msg);
	}
	
	public BusinessException(String msg, Exception e) {
		super(msg,e);
	}


}
