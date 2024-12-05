package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.gtcafe.asimov.model.User;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userDetail", column = "id", 
                one = @One(select = "com.gtcafe.asimov.mapper.UserDetailMapper.findByUserId"))
    })
    User findUserWithDetailById(Integer id);
}

