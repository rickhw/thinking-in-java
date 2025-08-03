package com.gtcafe.messageboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.messageboard.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
}