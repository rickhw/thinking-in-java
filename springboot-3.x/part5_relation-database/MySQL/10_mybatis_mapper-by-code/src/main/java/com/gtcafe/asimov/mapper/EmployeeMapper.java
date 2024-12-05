package com.gtcafe.asimov.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.gtcafe.asimov.model.Employee;

@Mapper
public interface EmployeeMapper {
    @Select("SELECT * FROM employees WHERE department_id = #{departmentId}")
    List<Employee> findByDepartmentId(Integer departmentId);
}