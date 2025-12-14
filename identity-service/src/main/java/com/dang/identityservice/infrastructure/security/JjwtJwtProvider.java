package com.dang.identityservice.infrastructure.security;

import com.dang.identityservice.application.port.ClockPort;
import com.dang.identityservice.application.port.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

@Component
public class JjwtJwtProvider implements JwtProvider {

    private final Key key;
    private final ClockPort clock;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JjwtJwtProvider(
            ClockPort clock,
            @Value("${jwt.signerKey}") String signerKey,
            @Value("${jwt.accessTtlSeconds:900}") long accessTtlSeconds,
            @Value("${jwt.refreshTtlSeconds:2592000}") long refreshTtlSeconds,
            @Value("${jwt.issuer:identity-service}") String issuer
    ) {
        this.clock = clock;

        byte[] keyBytes = signerKey.getBytes(StandardCharsets.UTF_8);
        // HS512 requires key length >= 64 bytes (512 bits)
        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "Invalid jwt.signerKey: HS512 requires at least 64 bytes, current=" + keyBytes.length
            );
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    @Override
    public JwtResult issueAccessToken(String userId, String username, Set<String> roles) {
        Instant now = clock.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);
        String jti = UUID.randomUUID().toString();

        // ✅ normalize roles to ROLE_* and sorted
        List<String> roleList = normalizeRolesToSortedList(roles);

        String token = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("type", "access")
                .claim("username", username)
                .claim("roles", roleList)
                // ❌ no perms claim anymore
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return new JwtResult(token, exp, jti);
    }

    @Override
    public String issueRefreshToken(String userId) {
        Instant now = clock.now();
        Instant exp = now.plusSeconds(refreshTtlSeconds);

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public Decoded decodeAndVerify(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token);

        Claims c = jws.getBody();

        String userId = c.getSubject();
        String username = (String) c.get("username");
        String jti = c.getId();
        Instant exp = c.getExpiration().toInstant();

        TokenType tokenType = TokenType.fromClaim((String) c.get("type"));

        // ✅ ROLE-ONLY read
        Set<String> roles = readStringSetClaim(c.get("roles"));
        roles = normalizeRolesToSet(roles);

        // Backward compatibility: older tokens had single "scope" claim
        // We only take entries starting with ROLE_
        if (roles.isEmpty()) {
            String scope = (String) c.get("scope");
            if (scope != null && !scope.isBlank()) {
                Set<String> tmp = new HashSet<>();
                for (String a : scope.split("\\s+")) {
                    if (a == null || a.isBlank()) continue;
                    String trimmed = a.trim();
                    if (trimmed.startsWith("ROLE_")) tmp.add(trimmed);
                }
                roles = normalizeRolesToSet(tmp);
            }
        }

        return new Decoded(userId, username, jti, exp, tokenType, roles);
    }

    private static List<String> normalizeRolesToSortedList(Set<String> input) {
        if (input == null || input.isEmpty()) return List.of();
        return input.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                .distinct()
                .sorted()
                .toList();
    }

    private static Set<String> normalizeRolesToSet(Set<String> input) {
        if (input == null || input.isEmpty()) return Set.of();
        Set<String> out = new HashSet<>();
        for (String s : input) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isBlank()) continue;
            out.add(t.startsWith("ROLE_") ? t : "ROLE_" + t);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> readStringSetClaim(Object claim) {
        if (claim == null) return new HashSet<>();

        if (claim instanceof Collection<?> col) {
            Set<String> out = new HashSet<>();
            for (Object o : col) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isBlank()) out.add(s);
            }
            return out;
        }

        // tolerate comma-separated strings
        String s = String.valueOf(claim).trim();
        if (s.isBlank()) return new HashSet<>();
        String[] parts = s.split(",");
        Set<String> out = new HashSet<>();
        for (String p : parts) {
            String t = p == null ? "" : p.trim();
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }
}
