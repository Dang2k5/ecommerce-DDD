package com.dang.identityservice.infrastructure.persistence.jpa.impl;

import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import com.dang.identityservice.domain.repository.RoleRepository;
import com.dang.identityservice.infrastructure.persistence.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository jpa;

    @Override
    public Optional<Role> findById(RoleId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpa.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public List<Role> findAllById(Collection<RoleId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpa.findAllById(ids);
    }

    @Override
    public boolean existsByPermissionId(PermissionId permissionId) {
        return jpa.existsByPermissionIdsContains(permissionId);
    }

    @Override
    public Role save(Role role) {
        return jpa.save(role);
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll();
    }

    @Override
    public void delete(Role role) {
        jpa.delete(role);
    }
}
