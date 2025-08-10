-- MySQL Database Initialization Script for PGB4 Message Board
-- This script sets up the initial database structure and configuration

-- Set character set and collation
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create database if it doesn't exist (handled by MYSQL_DATABASE env var)
-- CREATE DATABASE IF NOT EXISTS pgb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE pgb;

-- Create application user with proper permissions (handled by MYSQL_USER env var)
-- CREATE USER IF NOT EXISTS 'pgb_user'@'%' IDENTIFIED BY 'pgb_password';
-- GRANT ALL PRIVILEGES ON pgb.* TO 'pgb_user'@'%';

-- Optimize MySQL settings for the application
SET GLOBAL innodb_buffer_pool_size = 268435456; -- 256MB
SET GLOBAL max_connections = 200;
SET GLOBAL innodb_log_file_size = 50331648; -- 48MB

-- Create indexes for better performance (these will be created by Hibernate/JPA)
-- But we can prepare some initial optimizations

-- Flush privileges to ensure all changes take effect
FLUSH PRIVILEGES;

-- Log initialization completion
SELECT 'PGB4 Database initialization completed' AS status;