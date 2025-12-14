package com.dang.identityservice.infrastructure.security;

import com.dang.identityservice.application.port.CurrentUserPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserPort implements CurrentUserPort {

    @Override
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal == null) return null;
        return String.valueOf(principal);
    }
}
