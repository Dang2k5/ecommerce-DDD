package com.dang.identityservice.application.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private String permissionId;
    private String code;
    private String name;
}
