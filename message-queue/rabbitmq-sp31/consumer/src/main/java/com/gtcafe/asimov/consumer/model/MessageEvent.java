package com.gtcafe.asimov.consumer.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
	generator = ObjectIdGenerators.IntSequenceGenerator.class,
	property = "@id",
	scope = MessageEvent.class)
public class MessageEvent {

	private String id;
	private String name;
    private Integer age;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}

	public String getId() {
		return id;
	}
	public void setId(String messageId) {
		this.id = messageId;
	}

    public String toString() {
		return String.format("name: [%s], age:[%d]", name, age);
	}

}
