package com.dang.identityservice.infrastructure.persistence.jpa.impl;

import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.repository.PermissionRepository;
import com.dang.identityservice.infrastructure.persistence.jpa.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpa;

    @Override
    public Optional<Permission> findById(PermissionId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return jpa.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public List<Permission> findAllById(Collection<PermissionId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpa.findAllById(ids);
    }

    @Override
    public Permission save(Permission p) {
        return jpa.save(p);
    }

    @Override
    public List<Permission> findAll() {
        return jpa.findAll();
    }

    @Override
    public void delete(Permission p) {
        jpa.delete(p);
    }
}
