package com.gtcafe.messageboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gtcafe.messageboard.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.userId = :userId ORDER BY m.createdAt DESC")
    Page<Message> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT m FROM Message m ORDER BY m.createdAt DESC")
    Page<Message> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    // 保留原有方法以保持向後兼容性
    Page<Message> findByUserId(String userId, Pageable pageable);
}