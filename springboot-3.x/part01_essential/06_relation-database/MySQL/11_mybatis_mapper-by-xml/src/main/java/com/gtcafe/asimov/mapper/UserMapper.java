package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gtcafe.asimov.model.User;

@Mapper
public interface UserMapper {
    User findUserWithDetailById(Integer id);
}