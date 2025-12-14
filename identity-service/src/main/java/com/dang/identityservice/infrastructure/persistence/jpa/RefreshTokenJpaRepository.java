package com.dang.identityservice.infrastructure.persistence.jpa;

import com.dang.identityservice.domain.model.aggregates.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
