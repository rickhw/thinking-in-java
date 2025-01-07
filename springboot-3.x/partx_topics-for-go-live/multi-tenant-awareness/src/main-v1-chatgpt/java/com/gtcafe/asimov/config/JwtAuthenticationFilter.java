package com.gtcafe.asimov.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.gtcafe.asimov.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class JwtAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return token != null && token.startsWith("Bearer ")
                ? JwtUtils.extractTenantId(token.substring(7))
                : null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
                
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String tenantId = JwtUtils.extractTenantId(token.substring(7));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(tenantId, null, null)
            );
        }
        chain.doFilter(request, response);
    }
}
