package com.gtcafe.asimov.service;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.mapper.DepartmentMapper;
import com.gtcafe.asimov.mapper.UserMapper;
import com.gtcafe.asimov.model.Department;
import com.gtcafe.asimov.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DemoService {
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    public User getUserWithDetailById(Integer id) {
        return userMapper.findUserWithDetailById(id);
    }

    public Department getDepartmentWithEmployees(Integer id) {
        return departmentMapper.findDepartmentWithEmployeesById(id);
    }
}