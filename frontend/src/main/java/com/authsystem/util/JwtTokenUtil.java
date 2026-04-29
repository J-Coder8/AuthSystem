package com.authsystem.util;

public class JwtTokenUtil {

    private static String currentToken;
    private static String currentUsername;

    public static void setToken(String token) {
        currentToken = token;
    }

    public static String getToken() {
        return currentToken;
    }

    public static void clearToken() {
        currentToken = null;
        currentUsername = null;
    }

    public static boolean hasToken() {
        return currentToken != null && !currentToken.isBlank();
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }
}
