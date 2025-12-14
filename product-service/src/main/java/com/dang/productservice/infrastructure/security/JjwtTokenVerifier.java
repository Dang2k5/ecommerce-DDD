package com.dang.productservice.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class JjwtTokenVerifier implements JwtTokenVerifier {

    private final Key key;
    private final String issuer;

    public JjwtTokenVerifier(
            @Value("${jwt.signerKey}") String signerKey,
            @Value("${jwt.issuer:identity-service}") String issuer
    ) {
        byte[] bytes = signerKey.getBytes(StandardCharsets.UTF_8);
        // HS512 safe minimum: 64 bytes recommended
        if (bytes.length < 64) {
            throw new IllegalArgumentException("jwt.signerKey too short for HS512. Need >= 64 characters.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.issuer = issuer;
    }

    @Override
    public Decoded verify(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token);

        Claims c = jws.getBody();

        String userId = c.getSubject();                 // sub
        String username = (String) c.get("username");   // access token should have
        String jti = c.getId();                         // jti
        Instant exp = c.getExpiration().toInstant();    // exp
        String type = (String) c.get("type");           // access / refresh

        // ✅ ROLE-ONLY
        Set<String> roles = normalizeRolesToSet(readStringSetClaim(c.get("roles")));

        // Backward compatible with older tokens that used "scope" (space-separated)
        // ✅ Only keep ROLE_* entries, ignore non-role tokens.
        if (roles.isEmpty()) {
            String scope = (String) c.get("scope");
            if (scope != null && !scope.isBlank()) {
                Set<String> tmp = new HashSet<>();
                for (String a : scope.split("\\s+")) {
                    if (a == null || a.isBlank()) continue;
                    String t = a.trim();
                    if (t.startsWith("ROLE_")) tmp.add(t);
                }
                roles = normalizeRolesToSet(tmp);
            }
        }

        // ❌ no perms anymore
        return new Decoded(userId, username, jti, exp, roles, type);
    }

    private static Set<String> normalizeRolesToSet(Set<String> input) {
        if (input == null || input.isEmpty()) return Set.of();
        Set<String> out = new HashSet<>();
        for (String s : input) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isBlank()) continue;
            // ensure ROLE_ prefix
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
        Set<String> out = new HashSet<>();
        for (String p : s.split(",")) {
            String t = p == null ? "" : p.trim();
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }
}
