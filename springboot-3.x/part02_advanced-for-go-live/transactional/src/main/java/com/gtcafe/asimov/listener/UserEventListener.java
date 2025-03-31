package com.gtcafe.asimov.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gtcafe.asimov.UserCreatedEvent;

@Component
public class UserEventListener {

    @TransactionalEventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("✅ Transaction committed! User created: " + event.user().getName());
    }

    @EventListener
    @Async
    public void handleUserCreatedAsync(UserCreatedEvent event) {
        System.out.println("📢 Async Event Triggered: " + event.user().getName());
    }
}