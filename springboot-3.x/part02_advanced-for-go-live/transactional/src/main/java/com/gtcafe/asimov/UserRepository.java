package com.gtcafe.asimov;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.asimov.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}