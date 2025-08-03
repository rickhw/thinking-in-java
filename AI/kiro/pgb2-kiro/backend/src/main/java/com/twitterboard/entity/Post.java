package com.twitterboard.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_author_id", columnList = "author_id"),
    @Index(name = "idx_post_created_at", columnList = "created_at"),
    @Index(name = "idx_post_deleted", columnList = "deleted"),
    @Index(name = "idx_post_author_created", columnList = "author_id, created_at")
})
@SQLDelete(sql = "UPDATE posts SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Author cannot be null")
    private User author;
    
    @Column(nullable = false, length = 280)
    @NotBlank(message = "Content cannot be blank")
    @Size(min = 1, max = 280, message = "Content must be between 1 and 280 characters")
    private String content;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Post() {}
    
    // Constructor for creating new post
    public Post(User author, String content) {
        this.author = author;
        this.content = content;
        this.deleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getAuthor() {
        return author;
    }
    
    public void setAuthor(User author) {
        this.author = author;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isDeleted() {
        return deleted != null && deleted;
    }
    
    public void markAsDeleted() {
        this.deleted = true;
    }
    
    public void restore() {
        this.deleted = false;
    }
    
    public boolean isOwnedBy(User user) {
        return author != null && author.equals(user);
    }
    
    public boolean isOwnedBy(Long userId) {
        return author != null && author.getId().equals(userId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", authorId=" + (author != null ? author.getId() : null) +
                ", content='" + content + '\'' +
                ", deleted=" + deleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}