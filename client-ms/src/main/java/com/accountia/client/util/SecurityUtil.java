package com.accountia.client.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return null;
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
