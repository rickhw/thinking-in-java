package com.gtcafe.asimov.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "member")
@Data
public class Member {

    @Id
    private String id;
    private String username;
    private String password;
    private List<MemberAuthority> authorities = new ArrayList<>();

}