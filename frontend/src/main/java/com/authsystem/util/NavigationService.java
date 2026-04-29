package com.authsystem.util;

import com.authsystem.AuthSystemApplication;

public class NavigationService {

    public static void showLoginView() {
        AuthSystemApplication.showLoginView();
    }

    public static void showRegisterView() {
        AuthSystemApplication.showRegisterView();
    }

    public static void showOtpView(String username, String deliveryEmail, boolean totpMode, String initialMessage) {
        AuthSystemApplication.showOtpView(username, deliveryEmail, totpMode, initialMessage);
    }

    public static void showDashboardView(String username, String email, String fullName, String role) {
        AuthSystemApplication.showDashboardView(username, email, fullName, role);
    }

    public static void showForgotPasswordView() {
        AuthSystemApplication.showForgotPasswordView();
    }

    public static void showAdminPanelView(String username, String email, String fullName) {
        AuthSystemApplication.showAdminPanelView(username, email, fullName);
    }

    public static void showFaceView(String mode) {
        AuthSystemApplication.showFaceView(mode);
    }

    public static void logoutAndReturnToLogin() {
        JwtTokenUtil.clearToken();
        AuthSystemApplication.showLoginView();
    }
}
