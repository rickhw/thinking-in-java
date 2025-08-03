package com.gtcafe.messageboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtcafe.messageboard.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
