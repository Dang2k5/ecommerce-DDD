package com.dang.identityservice.infrastructure.persistence.jpa.impl;

import com.dang.identityservice.domain.model.aggregates.User;
import com.dang.identityservice.domain.model.valueobjects.Email;
import com.dang.identityservice.domain.model.valueobjects.RoleId;
import com.dang.identityservice.domain.model.valueobjects.UserId;
import com.dang.identityservice.domain.model.valueobjects.Username;
import com.dang.identityservice.domain.repository.UserRepository;
import com.dang.identityservice.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpa;

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return jpa.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsByRoleId(RoleId roleId) {
        return jpa.existsByRoleIdsContains(roleId);
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll();
    }

    @Override
    public void delete(User user) {
        jpa.delete(user);
    }
}
