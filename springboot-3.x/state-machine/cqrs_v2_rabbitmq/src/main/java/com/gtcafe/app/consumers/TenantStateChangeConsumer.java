package com.gtcafe.app.consumers;

import com.gtcafe.app.commands.UpdateTenantStatusCommand;
import com.gtcafe.app.services.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantStateChangeConsumer {
    
    private final TenantService tenantService;
    
    @RabbitListener(queues = "tenant.queue")
    public void handleTenantStateChange(UpdateTenantStatusCommand command) {
        tenantService.updateTenantStatus(command);
    }
}