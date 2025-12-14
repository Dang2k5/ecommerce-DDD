package com.dang.identityservice.application.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String username;
    private String email;

    /**
     * DDD domain uses status; this is derived for backward compatibility.
     */
    private boolean active;

    /**
     * ACTIVE / DISABLED / LOCKED / PENDING_ACTIVATION
     */
    private String status;

    private Set<String> roles;
    private Set<String> permissions;
}
