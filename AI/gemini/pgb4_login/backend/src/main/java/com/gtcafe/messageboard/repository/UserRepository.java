package com.gtcafe.messageboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gtcafe.messageboard.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
