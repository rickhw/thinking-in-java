package com.gtcafe.lab;

import java.util.Date;

public class Todo {
    private String title;
    private String text;
    private boolean done;
    private Date createdOn;
    private Date completedOn;
    
    // constructors, getters and setters
    public Todo(String title, String text) {
        this.title = title;
        this.text = text;
        this.createdOn = new Date();
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getCompletedOn() {
        return completedOn;
    }

    
}