package com.gtcafe.rws.booter.payload.standard.response;

public class StandardErrorResponse {

	private String errorCode;
	private String message;
	private Object data;

	public StandardErrorResponse(String code, String message) {
		this(code, message, null);
	}


	public StandardErrorResponse(String code, String message, Object data) {
		this.errorCode = code;
		this.message = message;
		this.data = data;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String code) {
		this.errorCode = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
