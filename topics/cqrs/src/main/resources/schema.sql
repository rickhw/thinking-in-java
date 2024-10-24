CREATE TABLE IF NOT EXISTS article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP
);
