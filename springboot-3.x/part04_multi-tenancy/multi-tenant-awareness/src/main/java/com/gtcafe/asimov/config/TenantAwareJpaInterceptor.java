package com.gtcafe.asimov.config;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantAwareJpaInterceptor {
    @PersistenceUnit
    private final EntityManagerFactory entityManagerFactory;
    private final HttpServletRequest request;

    public void configure() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);

        if (Boolean.TRUE.equals(getAwarenessFromToken())) {
            Long tenantId = getTenantIdFromToken();
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
        } else {
            session.disableFilter("tenantFilter");
        }
    }

    private Long getTenantIdFromToken() {
        // 模擬從 token 中取出 tenantId
        return 1L; // 假設 token 中的 tenantId 為 1
    }

    private Boolean getAwarenessFromToken() {
        // 模擬從 token 中取出 awareness
        return true; // 假設 token 中的 awareness 為 true
    }
}
