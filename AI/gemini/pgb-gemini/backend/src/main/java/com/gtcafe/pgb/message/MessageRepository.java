package com.gtcafe.pgb.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gtcafe.pgb.account.Account;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByUser(Account user, Pageable pageable);
    Page<Message> findByUserId(Long userId, Pageable pageable);
}
