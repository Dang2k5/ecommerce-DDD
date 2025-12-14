package com.dang.identityservice.application.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email
    private String email;        // optional

    /**
     * Backward compatible flag:
     * - true  => status = ACTIVE
     * - false => status = DISABLED
     */
    private Boolean active; // optional

    /**
     * Preferred: set explicit status (ACTIVE, DISABLED, LOCKED, PENDING_ACTIVATION).
     * If both 'status' and 'active' are provided, 'status' wins.
     */
    private String status; // optional

    private String password; // optional (đổi password)
}
