

---

## Claude

我將提供一個完整的範例，展示如何使用 Java 17、Spring Boot 3.2、Gradle 和 MyBatis 進行 MySQL 資料庫操作，並包含 one-to-one 和 one-to-many 的關聯操作。



首先，我们将建立一個完整的專案結構：

1. 首先，我們需要 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: demo-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: demo_db
      MYSQL_USER: demo_user
      MYSQL_PASSWORD: demo_pass
    ports:
      - "3306:3306"
    volumes:
      - ./init-ddl.sql:/docker-entrypoint-initdb.d/init.sql
      - mysql-data:/var/lib/mysql

volumes:
  mysql-data:

```

2. 資料庫初始化 SQL：

```sql
-- 用户表（one-to-one: 用户详情）
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- 用户详情表（one-to-one: 与用户关联）
CREATE TABLE user_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNIQUE,
    full_name VARCHAR(100),
    age INT,
    phone_number VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 部门表
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL UNIQUE
);

-- 员工表（one-to-many: 员工属于部门）
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 初始化测试数据
INSERT INTO users (username, email) VALUES 
    ('john_doe', 'john@example.com'),
    ('jane_smith', 'jane@example.com');

INSERT INTO user_details (user_id, full_name, age, phone_number) VALUES 
    (1, 'John Doe', 30, '123-456-7890'),
    (2, 'Jane Smith', 28, '987-654-3210');

INSERT INTO departments (dept_name) VALUES 
    ('IT Department'),
    ('HR Department');

INSERT INTO employees (name, department_id) VALUES 
    ('Alice Johnson', 1),
    ('Bob Williams', 1),
    ('Charlie Brown', 2);

```

3. `build.gradle`：

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    
    // MySQL Connector
    runtimeOnly 'mysql:mysql-connector-java:8.0.33'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

```

4. 領域模型 - User：

```java
package com.example.demo.model;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String email;
    
    // One-to-One: 用户详情
    private UserDetail userDetail;
}

@Data
public class UserDetail {
    private Integer id;
    private Integer userId;
    private String fullName;
    private Integer age;
    private String phoneNumber;
}

```

5. 領域模型 - Department 和 Employee：

```java
package com.example.demo.model;

import lombok.Data;
import java.util.List;

@Data
public class Department {
    private Integer id;
    private String deptName;
    
    // One-to-Many: 部门下的员工
    private List<Employee> employees;
}

@Data
public class Employee {
    private Integer id;
    private String name;
    private Integer departmentId;
}

```

6. MyBatis Mapper - UserMapper：

```java
package com.example.demo.mapper;

import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.One;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userDetail", column = "id", 
                one = @One(select = "com.example.demo.mapper.UserDetailMapper.findByUserId"))
    })
    User findUserWithDetailById(Integer id);
}

@Mapper
public interface UserDetailMapper {
    @Select("SELECT * FROM user_details WHERE user_id = #{userId}")
    UserDetail findByUserId(Integer userId);
}

```

7. MyBatis Mapper - DepartmentMapper：

```java
package com.example.demo.mapper;

import com.example.demo.model.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Many;

@Mapper
public interface DepartmentMapper {
    @Select("SELECT * FROM departments WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "employees", column = "id", 
                many = @Many(select = "com.example.demo.mapper.EmployeeMapper.findByDepartmentId"))
    })
    Department findDepartmentWithEmployeesById(Integer id);
}

@Mapper
public interface EmployeeMapper {
    @Select("SELECT * FROM employees WHERE department_id = #{departmentId}")
    List<Employee> findByDepartmentId(Integer departmentId);
}

```

8. Service 層：

```java
package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.DepartmentMapper;
import com.example.demo.model.User;
import com.example.demo.model.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

```

9. 控制器：

```java
package com.example.demo.controller;

import com.example.demo.service.DemoService;
import com.example.demo.model.User;
import com.example.demo.model.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {
    private final DemoService demoService;

    @GetMapping("/user/{id}")
    public User getUserWithDetail(@PathVariable Integer id) {
        return demoService.getUserWithDetailById(id);
    }

    @GetMapping("/department/{id}")
    public Department getDepartmentWithEmployees(@PathVariable Integer id) {
        return demoService.getDepartmentWithEmployees(id);
    }
}

```

10. 主應用程式：

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

```

11. 配置文件 `application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demo_db
    username: demo_user
    password: demo_pass
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  configuration:
    map-underscore-to-camel-case: true
  type-aliases-package: com.example.demo.model

```

使用步驟：
1. 創建專案目錄結構
2. 複製上述文件到相應位置
3. 執行 `docker-compose up -d` 啟動 MySQL
4. 使用 Gradle 構建並運行 Spring Boot 應用程式

範例展示了：
- One-to-One: 用戶和用戶詳情之間的關係
- One-to-Many: 部門和員工之間的關係
- MyBatis 的關聯查詢
- Spring Boot 整合 MyBatis
- Docker Compose 配置 MySQL

測試端點：
- `/api/user/1`: 獲取用戶及其詳情
- `/api/department/1`: 獲取部門及其所有員工

請根據需要調整配置和代碼。希望這個範例對你有幫助！



---
❯ curl http://localhost:8080/api/user/1
{"id":1,"username":"john_doe","email":"john@example.com","userDetail":{"id":1,"userId":1,"fullName":"John Doe","age":30,"phoneNumber":"123-456-7890"}}

❯ curl http://localhost:8080/api/department/1
{"id":1,"deptName":"IT Department","employees":[{"id":1,"name":"Alice Johnson","departmentId":1},{"id":2,"name":"Bob Williams","departmentId":1}]}