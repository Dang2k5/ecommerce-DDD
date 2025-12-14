package com.dang.identityservice.domain.repository;

import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import com.dang.identityservice.domain.model.valueobjects.UserId;
import com.dang.identityservice.domain.model.valueobjects.Username;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    Optional<User> findByUsername(Username username);

    boolean existsByUsername(Username username);

    boolean existsByEmail(Email email);

    /**
     * Query-side helper for invariants like: cannot delete Role that is still assigned to any user.
     */
    boolean existsByRoleId(RoleId roleId);

    User save(User user);

    List<User> findAll();

    void delete(User user);
}
