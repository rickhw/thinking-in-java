package com.gtcafe.asimov.model;

import lombok.Data;
import java.util.List;

@Data
public class Department {
    private Integer id;
    private String deptName;
    
    // One-to-Many: 部门下的员工
    private List<Employee> employees;
}
