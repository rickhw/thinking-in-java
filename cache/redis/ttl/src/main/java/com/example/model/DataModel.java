package com.example.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataModel implements Serializable {
    private String id;
    private List<Map<String, String>> data;

    public DataModel() {
        this.data = new LinkedList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    public void addItem(Map<String, String> item) {
        this.data.add(item);
    }
}
