package com.gtcafe.messageboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.gtcafe.messageboard.entity.Message;
import com.gtcafe.messageboard.service.MessageIdGenerator;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class to inject dependencies into JPA entities
 */
@Configuration
public class EntityConfiguration {

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    /**
     * Injects the MessageIdGenerator into the Message entity after Spring context initialization
     */
    @PostConstruct
    public void configureEntities() {
        Message.setMessageIdGenerator(messageIdGenerator);
    }
}