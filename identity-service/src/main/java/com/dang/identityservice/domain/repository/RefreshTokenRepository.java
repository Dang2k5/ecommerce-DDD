package com.dang.identityservice.domain.repository;

import com.dang.identityservice.domain.model.aggregates.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
