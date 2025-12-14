package com.dang.identityservice.domain.model.valueobjects;

public enum UserStatus {
    ACTIVE,
    LOCKED,
    PENDING_ACTIVATION,
    DISABLED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
