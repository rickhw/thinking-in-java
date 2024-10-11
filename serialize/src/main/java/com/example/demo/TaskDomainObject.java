package com.example.demo;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

@JsonTypeInfo(
    // use = JsonTypeInfo.Id.CLASS, // 使用完全限定類名來儲存類型資訊
    // include = JsonTypeInfo.As.WRAPPER_ARRAY // 包裝為陣列形式
	use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY // 使用屬性來存儲類型資訊
    // property = "@class" // 類型資訊的屬性名稱為 @class
)
public class TaskDomainObject {

	private String kind;
	private String taskId;
	private TaskMetadata metadata;
	private Object spec;

	public TaskDomainObject() {
		this.taskId = UUID.randomUUID().toString();
		this.kind = "sys.Tasks";
		this.metadata = new TaskMetadata();
		// this.spec = new Spec();
	}

	public String getTaskId() {
		return taskId;
	}

	public String getKind() {
		return kind;
	}

	public TaskMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(TaskMetadata metadata) {
		this.metadata = metadata;
	}

	public Object getSpec() {
		return spec;
	}

	public void setSpec(Object spec) {
		this.spec = spec;
	}
}