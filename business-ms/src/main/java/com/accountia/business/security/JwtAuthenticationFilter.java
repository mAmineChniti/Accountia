package com.accountia.business.security;

import com.accountia.business.util.JwtUtil;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                    if (jwtUtil.validateToken(token)) {
                        Map<String, Object> claims = jwtUtil.parseClaims(token);
                        String email = claims.get("email") == null ? null : String.valueOf(claims.get("email"));
                        String username = claims.get("username") == null ? null : String.valueOf(claims.get("username"));
                        
                        logger.debug("Token validated for user: " + email);
                        
                        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            auth.setDetails(Map.of(
                                "email", email,
                                "username", username,
                                "userId", claims.get("userId")
                            ));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            logger.info("Security context set for user: " + email);
                        }
                    } else {
                        logger.warn("Token validation failed for token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
                    }
                } catch (Exception e) {
                    logger.error("Error processing JWT token: " + e.getMessage());
                }
            }
            filterChain.doFilter(request, response);
        }
}
