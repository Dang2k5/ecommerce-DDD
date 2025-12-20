package com.dang.customerservice.infrastructure.security;

import com.dang.customerservice.application.port.CurrentUserPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserPort implements CurrentUserPort {

    @Override public String userId() { var u = principal(); return u == null ? null : u.userId(); }
    @Override public String username() { var u = principal(); return u == null ? null : u.username(); }

    private AuthenticatedUser principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        return (p instanceof AuthenticatedUser u) ? u : null;
    }
}
