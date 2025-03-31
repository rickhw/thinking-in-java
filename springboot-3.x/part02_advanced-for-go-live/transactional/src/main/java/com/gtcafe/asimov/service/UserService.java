package com.gtcafe.asimov.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.asimov.UserRepository;
import com.gtcafe.asimov.UserCreatedEvent;
import com.gtcafe.asimov.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createUser(String name) {
        User user = new User(name);
        userRepository.save(user);

        // 只有當 Transaction 提交成功後，事件才會觸發
        eventPublisher.publishEvent(new UserCreatedEvent(user));

        System.out.println("📌 User saved in DB: " + name);
    }

    @Transactional
    public void createUserWithRollback(String name) {
        User user = new User(name);
        userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(user));

        // 人為製造異常，測試 Transaction Rollback
        throw new RuntimeException("❌ Simulating an error to rollback transaction");
    }
}