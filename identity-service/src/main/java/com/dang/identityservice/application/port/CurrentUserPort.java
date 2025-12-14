package com.dang.identityservice.application.port;

/**
 * Abstraction to obtain the current authenticated principal (if any).
 * Keeps application layer independent from Spring Security.
 */
public interface CurrentUserPort {
    /**
     * @return current username (principal name) or null if unauthenticated.
     */
    String getUsername();
}
