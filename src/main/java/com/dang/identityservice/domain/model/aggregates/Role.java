package com.dang.identityservice.domain.model.aggregates;

import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
public class Role {

    @EmbeddedId
    private RoleId roleId;

    @Column(name = "code", nullable = false, unique = true, length = 120)
    private String code; // vd: ROLE_ADMIN

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Cross-aggregate reference: we only store PermissionId(s) here.
     * Permission aggregate is loaded via repository when needed (application/query side).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    )
    @AttributeOverride(name = "value", column = @Column(name = "permission_id", nullable = false, length = 36))
    @Getter(AccessLevel.NONE)
    private Set<PermissionId> permissionIds = new HashSet<>();

    protected Role() {
    }

    private Role(RoleId id, String code, String name) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Role code required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Role name required");
        String normalized = code.strip().toUpperCase();
        if (!normalized.startsWith("ROLE_")) throw new IllegalArgumentException("Role code must start with ROLE_");
        this.roleId = Objects.requireNonNull(id);
        this.code = normalized;
        this.name = name.strip();
    }

    public static Role create(String code, String name) {
        return new Role(RoleId.generate(), code, name);
    }

    /**
     * DDD: expose immutable view so callers cannot mutate aggregate state bypassing invariants.
     */
    public Set<PermissionId> getPermissionIds() {
        return Set.copyOf(permissionIds);
    }

    public void assignPermission(PermissionId permissionId) {
        permissionIds.add(Objects.requireNonNull(permissionId));
    }

    public void removePermission(PermissionId permissionId) {
        permissionIds.remove(Objects.requireNonNull(permissionId));
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("Role name required");
        this.name = newName.strip();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Role other && Objects.equals(roleId, other.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId);
    }
}
