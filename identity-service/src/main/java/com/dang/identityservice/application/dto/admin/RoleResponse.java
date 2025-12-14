package com.dang.identityservice.application.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleResponse {
    private String roleId;
    private String code;
    private String name;
    private Set<String> permissions;
}
