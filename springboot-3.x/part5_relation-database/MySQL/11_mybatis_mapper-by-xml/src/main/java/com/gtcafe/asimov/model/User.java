package com.gtcafe.asimov.model;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String email;
    
    // One-to-One: 用户详情
    private UserDetail userDetail;
}
