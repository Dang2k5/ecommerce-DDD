package com.dang.identityservice.infrastructure.persistence.jpa.impl;

import com.dang.identityservice.domain.model.aggregates.RefreshToken;
import com.dang.identityservice.domain.repository.RefreshTokenRepository;
import com.dang.identityservice.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<RefreshToken> findByTokenValue(String tokenValue) {
        return jpa.findByTokenValue(tokenValue);
    }
}
