package com.dang.identityservice.infrastructure.persistence.jpa;

import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import com.dang.identityservice.domain.model.valueobjects.UserId;
import com.dang.identityservice.domain.model.valueobjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, UserId> {
    Optional<User> findByUsername(Username username);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);

    boolean existsByRoleIdsContains(RoleId roleId);
}
