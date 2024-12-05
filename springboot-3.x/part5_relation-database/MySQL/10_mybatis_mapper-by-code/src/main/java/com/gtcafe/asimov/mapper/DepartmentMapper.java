package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.gtcafe.asimov.model.Department;

@Mapper
public interface DepartmentMapper {
    @Select("SELECT * FROM departments WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "employees", column = "id", 
                many = @Many(select = "com.gtcafe.asimov.mapper.EmployeeMapper.findByDepartmentId"))
    })
    Department findDepartmentWithEmployeesById(Integer id);
}
