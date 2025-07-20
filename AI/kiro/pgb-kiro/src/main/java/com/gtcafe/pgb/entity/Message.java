package com.gtcafe.pgb.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message entity representing a message in the message board system
 */
@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Message author cannot be null")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_owner_id", nullable = false)
    @NotNull(message = "Board owner cannot be null")
    private User boardOwner;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    @OneToMany(mappedBy = "parentMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> replies = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /**
     * Constructor for creating a new message with required fields
     */
    public Message(User user, User boardOwner, String content) {
        this.user = user;
        this.boardOwner = boardOwner;
        this.content = content;
        this.isDeleted = false;
    }

    /**
     * Constructor for creating a reply message
     */
    public Message(User user, User boardOwner, String content, Message parentMessage) {
        this.user = user;
        this.boardOwner = boardOwner;
        this.content = content;
        this.parentMessage = parentMessage;
        this.isDeleted = false;
    }

    /**
     * Check if the message is deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }

    /**
     * Check if this is a reply message
     */
    public boolean isReply() {
        return parentMessage != null;
    }

    /**
     * Check if this is a root message (not a reply)
     */
    public boolean isRootMessage() {
        return parentMessage == null;
    }

    /**
     * Check if the message has replies
     */
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }

    /**
     * Get the count of replies
     */
    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }

    /**
     * Check if the given user is the author of this message
     */
    public boolean isAuthor(User user) {
        return this.user != null && user != null && this.user.getId().equals(user.getId());
    }

    /**
     * Check if the given user is the board owner
     */
    public boolean isBoardOwner(User user) {
        return this.boardOwner != null && user != null && this.boardOwner.getId().equals(user.getId());
    }

    /**
     * Soft delete the message
     */
    public void softDelete() {
        this.isDeleted = true;
    }

    /**
     * Add a reply to this message
     */
    public void addReply(Message reply) {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        replies.add(reply);
        reply.setParentMessage(this);
    }

    /**
     * Remove a reply from this message
     */
    public void removeReply(Message reply) {
        if (replies != null) {
            replies.remove(reply);
            reply.setParentMessage(null);
        }
    }
}