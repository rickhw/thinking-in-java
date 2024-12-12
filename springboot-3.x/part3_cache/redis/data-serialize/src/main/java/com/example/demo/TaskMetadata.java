package com.example.demo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    // use = JsonTypeInfo.Id.CLASS,
    // include = JsonTypeInfo.As.WRAPPER_ARRAY
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY
    // property = "@class"
)

public class TaskMetadata {

	String _apiVersion; // "v1alpha"
	TaskState _state;		// "pending"
	String _creationTime;	// "2021-12-10T00:29:06.800+08:00",
	String _lastModified;	// "2021-12-10T00:29:06.800+08:00"

	public TaskMetadata() {
		this._apiVersion = "v1alpha";
		this._state = TaskState.PENDING;
		this._creationTime = new Date().toString();
		this._lastModified = new Date().toString();
	}

	public String get_apiVersion() {
		return _apiVersion;
	}

	public void set_apiVersion(String _apiVersion) {
		this._apiVersion = _apiVersion;
	}

	public TaskState get_state() {
		return _state;
	}

	public void set_state(TaskState _state) {
		this._state = _state;
	}

	public String get_creationTime() {
		return _creationTime;
	}

	public void set_creationTime(String _creationTime) {
		this._creationTime = _creationTime;
	}

	public String get_lastModified() {
		return _lastModified;
	}

	public void set_lastModified(String _lastModified) {
		this._lastModified = _lastModified;
	}
}