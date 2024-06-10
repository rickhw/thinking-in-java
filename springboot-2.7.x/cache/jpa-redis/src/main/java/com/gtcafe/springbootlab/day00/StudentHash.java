package com.gtcafe.springbootlab.day00;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("Student")
public class StudentHash implements Serializable {
  
    private String id;
    private String name;
    private Gender gender;
    private int grade;
    private Date lastModified;

    public StudentHash(String id, String name, Gender gender, int grade, Date lastModified) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.grade = grade;
        this.lastModified = lastModified;
    }

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
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public int getGrade() {
        return grade;
    }
    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    

    public String toString() { 
        return String.format("id: [%s], name: [%s]", id, name);
    }
}
