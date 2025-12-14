package com.dang.identityservice.application.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),

    INVALID_REQUEST(1000, "Invalid request"),
    UNAUTHENTICATED(1001, "Unauthenticated"),
    UNAUTHORIZED(1002, "Unauthorized"),

    USER_EXISTED(1101, "User existed"),
    USER_NOT_EXISTED(1102, "User not existed"),
    USER_INACTIVE(1103, "User is inactive"),
    USER_EMAIL_EXISTED(1106, "Email existed"),

    ROLE_EXISTED(1201, "Role existed"),
    ROLE_NOT_EXISTED(1202, "Role not existed"),
    PERMISSION_EXISTED(1301, "Permission existed"),
    PERMISSION_NOT_EXISTED(1302, "Permission not existed"),

    REFRESH_TOKEN_INVALID(1401, "Invalid refresh token"),
    REFRESH_TOKEN_EXPIRED(1402, "Refresh token expired"),
    REFRESH_TOKEN_REVOKED(1403, "Refresh token revoked"),

    TOKEN_INVALID(1501, "Invalid token"),
    TOKEN_REVOKED(1502, "Token revoked"),

    ROLE_IN_USE(1203, "Role is in use"),
    PERMISSION_IN_USE(1303, "Permission is in use"),

    CANNOT_DELETE_SELF(1104, "Cannot delete yourself"),
    CANNOT_DELETE_BOOTSTRAP_ADMIN(1105, "Cannot delete bootstrap admin");

    private final int code;
    private final String message;
}
