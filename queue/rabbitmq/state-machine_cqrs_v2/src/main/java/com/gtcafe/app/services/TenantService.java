package com.gtcafe.app.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtcafe.app.commands.CreateTenantCommand;
import com.gtcafe.app.commands.UpdateTenantStatusCommand;
import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.domain.TenantStatus;
import com.gtcafe.app.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {
    
    private final TenantRepository tenantRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Transactional
    public Tenant createTenant(CreateTenantCommand command) {
        Tenant tenant = new Tenant();
        tenant.setName(command.getName());
        tenant.setStatus(TenantStatus.PENDING);
        tenant = tenantRepository.save(tenant);
        
        // 使用完整建構的命令物件
        UpdateTenantStatusCommand statusCommand = new UpdateTenantStatusCommand(tenant.getId(), TenantStatus.ACTIVE);
        
        // 發送狀態變更消息到 RabbitMQ
        rabbitTemplate.convertAndSend("tenant.exchange", "tenant.state.change", statusCommand);
        
        return tenant;
    }
    
    public Tenant getTenant(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
    
    @Transactional
    public Tenant updateTenantStatus(UpdateTenantStatusCommand command) {
        Tenant tenant = getTenant(command.getTenantId());
        validateStateTransition(tenant.getStatus(), command.getTargetStatus());

        // 1. 發送狀態變更消息到 RabbitMQ
        rabbitTemplate.convertAndSend("tenant.exchange", "tenant.state.change", command);

        // 2. update to cache
        tenant.setStatus(command.getTargetStatus());
        return tenantRepository.save(tenant);
    }
    
    private void validateStateTransition(TenantStatus currentStatus, TenantStatus targetStatus) {
        // 實作狀態轉換驗證邏輯
        if (currentStatus == TenantStatus.DELETED) {
            throw new IllegalStateException("Cannot change status of terminated tenant");
        }
        // 可以添加更多驗證規則
    }
}