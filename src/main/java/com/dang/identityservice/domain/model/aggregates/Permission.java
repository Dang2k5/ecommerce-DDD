package com.dang.identityservice.domain.model.aggregates;

import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(name = "permissions")
@Getter
public class Permission {

    @EmbeddedId
    private PermissionId permissionId;

    @Column(name = "code", nullable = false, unique = true, length = 120)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    protected Permission() {
    }

    private Permission(PermissionId id, String code, String name) {
        this.permissionId = Objects.requireNonNull(id);
        this.code = normalizeAndValidateCode(code);
        this.name = normalizeName(name);
    }

    public static Permission create(String code, String name) {
        return new Permission(PermissionId.generate(), code, name);
    }

    public void rename(String newName) {
        this.name = normalizeName(newName);
    }

    private static String normalizeAndValidateCode(String code) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Permission code required");
        String normalized = code.strip().toUpperCase();

        // IMPORTANT: do NOT allow permission codes to start with ROLE_.
        // Reason: roles also start with ROLE_, and old tokens that used a single "scope" claim
        // would mis-classify ROLE_* entries as roles.
        if (normalized.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Permission code must not start with ROLE_. Use PERM_* (e.g. PERM_ROLE_WRITE).");
        }

        return normalized;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Permission name required");
        return name.strip();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Permission other && Objects.equals(permissionId, other.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionId);
    }
}
