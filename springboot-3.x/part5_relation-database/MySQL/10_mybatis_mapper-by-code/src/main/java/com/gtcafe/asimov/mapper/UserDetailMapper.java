package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.gtcafe.asimov.model.UserDetail;

@Mapper
public interface UserDetailMapper {
    @Select("SELECT * FROM user_details WHERE user_id = #{userId}")
    UserDetail findByUserId(Integer userId);
}