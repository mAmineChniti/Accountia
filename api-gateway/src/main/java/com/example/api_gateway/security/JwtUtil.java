package com.example.api_gateway.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final JWTVerifier verifier;

    public JwtUtil(@Value("${gateway.jwt.secret:changeit}") String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
        this.verifier = JWT.require(algorithm).build();
    }

    public Map<String, Object> parseClaims(String token) {
        DecodedJWT jwt = verifier.verify(token);
        Map<String, Object> map = new HashMap<>();
        map.put("sub", jwt.getSubject());
        jwt.getClaims().forEach((k, v) -> {
            try {
                if (v.asList(Object.class) != null) map.put(k, v.asList(Object.class));
            } catch (Exception e) {
                try { map.put(k, v.asLong()); } catch (Exception ex) {}
                try { map.put(k, v.asInt()); } catch (Exception ex) {}
                try { map.put(k, v.asString()); } catch (Exception ex) {}
                try { map.put(k, v.asBoolean()); } catch (Exception ex) {}
            }
        });
        return map;
    }
}
