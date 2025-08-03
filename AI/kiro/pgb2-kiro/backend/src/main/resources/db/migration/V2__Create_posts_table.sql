-- Create posts table
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_content_length CHECK (CHAR_LENGTH(content) <= 280 AND CHAR_LENGTH(content) >= 1)
);

-- Create indexes for posts table
CREATE INDEX idx_post_author_id ON posts(author_id);
CREATE INDEX idx_post_created_at ON posts(created_at);
CREATE INDEX idx_post_deleted ON posts(deleted);
CREATE INDEX idx_post_author_created ON posts(author_id, created_at);