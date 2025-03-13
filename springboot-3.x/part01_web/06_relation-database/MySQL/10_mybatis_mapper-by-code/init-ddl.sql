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