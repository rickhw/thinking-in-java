package com.gtcafe.rws.booter.payload.response;

import java.util.Date;

public class RetrieveTaskResponse {

	private String id;
	private String state;
	private String data;
	private Date craetedAt;
	private Date finishedAt;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public Date getCraetedAt() {
		return craetedAt;
	}
	public void setCraetedAt(Date craetedAt) {
		this.craetedAt = craetedAt;
	}
	public Date getFinishedAt() {
		return finishedAt;
	}
	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}


}
