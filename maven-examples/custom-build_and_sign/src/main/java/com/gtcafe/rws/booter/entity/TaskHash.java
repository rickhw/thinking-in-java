package com.gtcafe.rws.booter.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("Task")
public class TaskHash implements Serializable {

    private String id;
    private String data;
    private ETaskState state;
    private Date createdAt;
    private Date finishedAt;

    public TaskHash(String id, String data, ETaskState state) {
        this.id = id;
        this.data = data;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ETaskState getState() {
        return state;
    }

    public void setState(ETaskState state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }


}
