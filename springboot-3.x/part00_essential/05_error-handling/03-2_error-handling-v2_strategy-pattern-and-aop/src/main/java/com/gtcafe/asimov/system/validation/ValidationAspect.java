package com.gtcafe.asimov.system.validation;

import java.util.function.Consumer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)  // 確保驗證在事務之前執行
public class ValidationAspect {
    

    private final ValidationStrategyFactory validationStrategyFactory;
    
    public ValidationAspect(ValidationStrategyFactory validationStrategyFactory) {
        this.validationStrategyFactory = validationStrategyFactory;
    }
    
    @Before("@annotation(validateRequest) && args(request,..)")
    public void validate(JoinPoint joinPoint, ValidateRequest validateRequest, Object request) {
        IValidationStrategy<?> strategy = validationStrategyFactory.getStrategy(request.getClass());
        
        if (validateRequest.validatePayload()) {
            executeValidation(strategy::validatePayload, request);
        }
        
        if (validateRequest.validateQuota()) {
            executeValidation(strategy::validateQuota, request);
        }
        
        if (validateRequest.validateAssociations()) {
            executeValidation(strategy::validateAssociations, request);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> void executeValidation(Consumer<T> validationMethod, Object request) {
        validationMethod.accept((T) request);
    }
    
    // private final TenantValidationService validationService;
    
    // public ValidationAspect(TenantValidationService validationService) {
    //     this.validationService = validationService;
    // }
    
    // @Before("@annotation(validateRequest) && args(request,..)")
    // public void validate(JoinPoint joinPoint, ValidateRequest validateRequest, Object request) {
    //     // 檢查請求對象是否是 CreateTenantRequest
    //     if (!(request instanceof CreateTenantRequest)) {
    //         return;
    //     }
        
    //     CreateTenantRequest tenantRequest = (CreateTenantRequest) request;
        
    //     // 根據註解配置執行驗證
    //     if (validateRequest.validatePayload()) {
    //         validationService.validatePayload(tenantRequest);
    //     }
        
    //     if (validateRequest.validateQuota()) {
    //         validationService.validateQuota(tenantRequest.getName());
    //     }
        
    //     if (validateRequest.validateAssociations()) {
    //         validationService.validateAssociations(tenantRequest);
    //     }
    // }
}