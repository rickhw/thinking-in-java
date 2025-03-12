package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 將 JSON 字串轉換為模型
        String jsonString = """
                {
                    "name": "John Doe",
                    "age": 30,
                    "address": {
                        "street": "123 Main St",
                        "city": "Anytown"
                    }
                }
                """;

        Person person = objectMapper.readValue(jsonString, Person.class);
        System.out.println("Converted from JSON: " + person.getName() + ", " + person.getAddress().getCity());

        // 將模型轉換為 JSON 字串
        String jsonOutput = objectMapper.writeValueAsString(person);
        System.out.println("Converted to JSON: " + jsonOutput);
    }
}