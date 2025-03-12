package com.gtcafe.asimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;

public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
    default String getCurrentTenantId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
