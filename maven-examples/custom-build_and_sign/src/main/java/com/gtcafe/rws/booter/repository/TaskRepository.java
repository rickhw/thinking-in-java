package com.gtcafe.rws.booter.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.rws.booter.entity.TaskHash;

@Repository
public interface TaskRepository extends CrudRepository<TaskHash, String> {}