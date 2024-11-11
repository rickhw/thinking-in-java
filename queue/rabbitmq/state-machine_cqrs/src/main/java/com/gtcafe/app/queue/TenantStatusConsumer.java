// TenantStatusConsumer.java
package com.gtcafe.app.queue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.gtcafe.app.config.RabbitMQConfig;
import com.gtcafe.app.dto.TenantStatusUpdateRequest;
import com.gtcafe.app.service.CommandService;

@Component
public class TenantStatusConsumer {
    private final CommandService commandService;

    public TenantStatusConsumer(CommandService commandService) {
        this.commandService = commandService;
    }

    @RabbitListener(queues = RabbitMQConfig.TENANT_QUEUE)
    public void handleTenantStatusUpdate(TenantStatusUpdateRequest request) {
        commandService.updateTenantStatus(request.getTenantId(), request.getNewStatus());
    }
}
