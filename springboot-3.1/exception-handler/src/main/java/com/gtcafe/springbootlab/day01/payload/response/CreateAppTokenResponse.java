package com.gtcafe.springbootlab.day01.payload.response;

public class CreateAppTokenResponse {

	private Long id;

	private String token;

	public CreateAppTokenResponse(Long id, String token) {
		this.id = id;
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	// public void setTokenName(String id) {
	// 	this.id = id;
	// }

	public String getToken() {
		return token;
	}

	// public void setToken(String token) {
	// 	this.token = token;
	// }
}
