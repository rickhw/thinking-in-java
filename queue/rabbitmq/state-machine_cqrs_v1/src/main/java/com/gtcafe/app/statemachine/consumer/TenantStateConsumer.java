package com.gtcafe.app.statemachine.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.statemachine.handler.TenantStateHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantStateConsumer {
    private final TenantStateHandler stateHandler;
    
    @RabbitListener(queues = "${rabbitmq.queues.tenant}")
    public void handleStateChange(@Payload Tenant tenant) {
        log.info("tenant: [{}]", tenant);
        
        switch (tenant.getState()) {
            case INITING -> stateHandler.handleIniting(tenant);
            case ACTIVE -> stateHandler.handleActive(tenant);
            case INACTIVE -> stateHandler.handleInactive(tenant);
            case TERMINATED -> stateHandler.handleTerminated(tenant);
        }
    }
}