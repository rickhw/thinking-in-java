package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gtcafe.asimov.model.UserDetail;

@Mapper
public interface UserDetailMapper {
    UserDetail findByUserId(Integer userId);
}