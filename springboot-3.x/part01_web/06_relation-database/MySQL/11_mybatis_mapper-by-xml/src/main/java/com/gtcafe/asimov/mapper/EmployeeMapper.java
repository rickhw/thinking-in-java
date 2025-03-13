package com.gtcafe.asimov.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gtcafe.asimov.model.Employee;

@Mapper
public interface EmployeeMapper {
    List<Employee> findByDepartmentId(Integer departmentId);
}