package com.gtcafe.app.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtcafe.app.domain.Tenant;
import com.gtcafe.app.dto.TenantCreateRequest;
import com.gtcafe.app.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantCommandService {
    private final TenantRepository tenantRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchanges.state-machine}")
    private String exchange;
    
    @Value("${rabbitmq.routing-keys.tenant}")
    private String routingKey;
    
    public Tenant createTenant(TenantCreateRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setState(Tenant.TenantState.INITING);
        tenant = tenantRepository.save(tenant);
        
        rabbitTemplate.convertAndSend(exchange, routingKey, tenant);
        return tenant;
    }
    
    public void updateState(Long id, Tenant.TenantState newState) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setState(newState);
        tenant = tenantRepository.save(tenant);
        
        rabbitTemplate.convertAndSend(exchange, routingKey, tenant);
    }
}