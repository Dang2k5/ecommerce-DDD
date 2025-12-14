package com.dang.identityservice.domain.repository;

import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.model.valueobjects.RoleId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(RoleId id);

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Bulk-load roles by their IDs (query side).
     */
    List<Role> findAllById(Collection<RoleId> ids);

    /**
     * Query-side helper for invariants like: cannot delete Permission that is still assigned to any role.
     */
    boolean existsByPermissionId(PermissionId permissionId);

    Role save(Role role);

    List<Role> findAll();

    void delete(Role role);
}
