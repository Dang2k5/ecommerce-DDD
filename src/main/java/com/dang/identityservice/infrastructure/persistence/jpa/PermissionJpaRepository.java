package com.dang.identityservice.infrastructure.persistence.jpa;

import com.dang.identityservice.domain.model.aggregates.Permission;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionJpaRepository extends JpaRepository<Permission, PermissionId> {
    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);
}
