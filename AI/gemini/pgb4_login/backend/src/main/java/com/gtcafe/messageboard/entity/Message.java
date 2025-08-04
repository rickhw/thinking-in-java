package com.gtcafe.messageboard.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private static MessageIdGenerator _idGenerator;

    /**
     * Sets the MessageIdGenerator instance for ID generation
     * This is called by Spring during application startup
     */
    public static void setIdGenerator(MessageIdGenerator generator) {
        _idGenerator = generator;
    }

    /**
     * Automatically generates ID before persisting to database
     */
    @PrePersist
    public void generateId() {
        if (this.id == null && _idGenerator != null) {
            this.id = _idGenerator.generateId();
        }
    }
}