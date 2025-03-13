package com.gtcafe.asimov.model;

import lombok.Data;

@Data
public class UserDetail {
    private Integer id;
    private Integer userId;
    private String fullName;
    private Integer age;
    private String phoneNumber;
}