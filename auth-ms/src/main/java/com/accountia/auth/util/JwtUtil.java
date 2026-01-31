package com.accountia.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expirationMs;

    public JwtUtil(@Value("${security.jwt.secret:changeit}") String secret,
                   @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.algorithm = Algorithm.HMAC256(secret.getBytes());
        this.verifier = JWT.require(algorithm).build();
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    public String generateTokenWithClaims(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        com.auth0.jwt.JWTCreator.Builder b = JWT.create()
                .withSubject(subject)
                .withIssuedAt(now)
                .withExpiresAt(exp);
        if (claims != null) {
            claims.forEach((k, v) -> {
                if (v instanceof String) b.withClaim(k, (String) v);
                else if (v instanceof Integer) b.withClaim(k, (Integer) v);
                else if (v instanceof Long) b.withClaim(k, (Long) v);
                else if (v instanceof Boolean) b.withClaim(k, (Boolean) v);
                else if (v != null) b.withClaim(k, v.toString());
            });
        }
        return b.sign(algorithm);
    }

    public String getSubject(String token) {
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

    public Map<String, Object> parseClaims(String token) {
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaims().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            com.auth0.jwt.interfaces.Claim c = e.getValue();
                            if (c.isNull()) return null;
                            if (c.asList(Object.class) != null) return c.asList(Object.class);
                            if (c.asBoolean() != null) return c.asBoolean();
                            if (c.asLong() != null) return c.asLong();
                            if (c.asInt() != null) return c.asInt();
                            if (c.asDouble() != null) return c.asDouble();
                            if (c.asString() != null) return c.asString();
                            return c.as(Object.class);
                        }
                ));
    }
}
