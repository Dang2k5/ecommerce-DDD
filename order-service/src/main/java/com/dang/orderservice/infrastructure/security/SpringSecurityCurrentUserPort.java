package com.dang.orderservice.infrastructure.security;

import com.dang.orderservice.application.port.CurrentUserPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentUserPort implements CurrentUserPort {

    @Override
    public String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "anonymous";

        // JwtAuthenticationFilter sets principal = AuthenticatedUser(userId, username)
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser u) {
            return u.userId();
        }

        // Fallbacks
        if (auth.getName() == null || auth.getName().isBlank()) return "anonymous";
        return auth.getName();
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
