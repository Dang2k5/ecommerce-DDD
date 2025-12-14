package com.dang.identityservice.infrastructure.persistence.jpa;

import com.dang.identityservice.domain.model.aggregates.Role;
import com.dang.identityservice.domain.model.valueobjects.PermissionId;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<Role, RoleId> {
    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByPermissionIdsContains(PermissionId permissionId);
}
