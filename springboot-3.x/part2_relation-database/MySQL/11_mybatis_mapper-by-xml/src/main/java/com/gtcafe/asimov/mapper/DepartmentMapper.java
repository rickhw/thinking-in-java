package com.gtcafe.asimov.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gtcafe.asimov.model.Department;

@Mapper
public interface DepartmentMapper {
    Department findDepartmentWithEmployeesById(Integer id);
}
