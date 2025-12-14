package com.dang.productservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenVerifier verifier;

    public JwtAuthenticationFilter(JwtTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        try {
            JwtTokenVerifier.Decoded d = verifier.verify(token);

            // ✅ reject refresh tokens
            if ("refresh".equalsIgnoreCase(d.type())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // access token should have username
            if (d.username() == null || d.username().isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (d.expiresAt() == null || d.expiresAt().isBefore(Instant.now())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // ✅ ROLE-ONLY authorities (keep ROLE_* only)
            Set<SimpleGrantedAuthority> authorities =
                    (d.roles() == null ? Set.<String>of() : d.roles())
                            .stream()
                            .filter(r -> r != null && !r.isBlank() && r.startsWith("ROLE_"))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toUnmodifiableSet());

            var principal = new AuthenticatedUser(d.userId(), d.username());
            var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
