package com.dang.identityservice.domain.repository;

import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    Optional<Permission> findById(PermissionId id);

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findAllById(Collection<PermissionId> ids);

    Permission save(Permission permission);

    List<Permission> findAll();

    void delete(Permission permission);
}
