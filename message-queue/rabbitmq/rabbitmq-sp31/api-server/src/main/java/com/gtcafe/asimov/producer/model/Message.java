package com.gtcafe.asimov.producer.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
	generator = ObjectIdGenerators.IntSequenceGenerator.class,
	property = "@id",
	scope = Message.class)
public class Message {

	private String id;
	private String name;
    private Integer age;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String toString() {
		return String.format("MessageEvent={id: [%s], name: [%s], age:[%d]}", id, name, age);
	}

}
