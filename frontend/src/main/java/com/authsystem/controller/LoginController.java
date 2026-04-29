package com.authsystem.controller;

import com.authsystem.AuthSystemApplication;
import com.authsystem.model.AuthResponse;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.service.AuthService;
import com.authsystem.util.JwtTokenUtil;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button forgotPasswordButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        setLoading(true);
        errorLabel.setText("");

        new Thread(() -> {
            ApiResponse<AuthResponse> response = authService.login(username, password);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess() && response.getData() != null) {
                    AuthResponse data = response.getData();
                    if (data.isRequiresOtp() || data.isRequiresTotp()) {
                        AuthSystemApplication.showOtpView(data.getUsername(), data.getEmail(), data.isRequiresTotp(), data.getMessage());
                    } else {
                        JwtTokenUtil.setToken(data.getToken());
                        JwtTokenUtil.setCurrentUsername(data.getUsername());
                        AuthSystemApplication.showDashboardView(data.getUsername(), data.getEmail(), data.getFullName(), data.getRole());
                    }
                } else {
                    errorLabel.setText(response.getErrorMessage() != null ? response.getErrorMessage() : "Login failed");
                }
            });
        }).start();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        AuthSystemApplication.showRegisterView();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        AuthSystemApplication.showForgotPasswordView();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        if (registerButton != null) {
            registerButton.setDisable(loading);
        }
        if (forgotPasswordButton != null) {
            forgotPasswordButton.setDisable(loading);
        }
        progressIndicator.setVisible(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
    }
}

