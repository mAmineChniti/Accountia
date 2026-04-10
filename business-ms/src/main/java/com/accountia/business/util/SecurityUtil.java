package com.accountia.business.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;

public class SecurityUtil {

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // This is the email from JWT
        }
        return null;
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<?, ?> details = getDetailsMap(authentication);
        if (details != null) {
            Object userId = details.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim instanceof Number number) {
                return number.longValue();
            }
            if (userIdClaim instanceof String value) {
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    public static String getCurrentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null && authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return true;
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?, ?> realmAccessMap) {
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof Collection<?> roleList) {
                    return roleList.stream().anyMatch(role -> "ADMIN".equals(String.valueOf(role)));
                }
            }
        }
        return false;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<?, ?> details = getDetailsMap(authentication);
        if (details != null) {
            Object username = details.get("username");
            return username != null ? username.toString() : null;
        }
        return null;
    }

    private static Map<?, ?> getDetailsMap(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() instanceof Map<?, ?> details) {
            return details;
        }
        return null;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }
}
