package com.gtcafe.rws.booter.payload.standard.response;

public class StandardResponse {

	private String code;
	private String message;
	private Object data;

	public StandardResponse(String code, String message) {
		this(code, message, null);
	}


	public StandardResponse(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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
