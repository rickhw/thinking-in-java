package com.gtcafe.asimov.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.asimov.model.Member;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Member findByUsername(String username);
}