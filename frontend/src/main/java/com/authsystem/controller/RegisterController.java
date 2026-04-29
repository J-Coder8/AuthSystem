package com.authsystem.controller;

import java.io.IOException;
import java.util.regex.Pattern;

import com.authsystem.AuthSystemApplication;
import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField fullNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private Label passwordMatchLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;

    @FXML
    private ProgressIndicator progressIndicator;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:8082/api/auth";

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    @FXML
    private void handleLogin(ActionEvent event) {
        AuthSystemApplication.showLoginView();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            showMessage("All fields are required", false);
            return;
        }

        if (username.length() < 3 || username.length() > 50) {
            showMessage("Username must be between 3 and 50 characters", false);
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address", false);
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showMessage("Password must be at least 8 characters with uppercase, lowercase, and number", false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match", false);
            return;
        }

        setLoading(true);
        showMessage("", true);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setFullName(fullName);
        registerRequest.setPassword(password);

        new Thread(() -> {
            try {
                String json = gson.toJson(registerRequest);
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(BASE_URL + "/register")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        AuthResponse authResponse = gson.fromJson(response.body().string(), AuthResponse.class);

                        Platform.runLater(() -> {
                            setLoading(false);
                            handleRegisterResponse(authResponse);
                        });
                    } else {
                        String errorMessage = "Registration failed";
                        try {
                            if (response.body() != null) {
                                errorMessage = response.body().string();
                            }
                        } catch (IOException e) {
                        }
                        final String msg = errorMessage;
                        Platform.runLater(() -> {
                            setLoading(false);
                            showMessage(msg, false);
                        });
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showMessage("Connection error: " + e.getMessage(), false);
                });
            }
        }).start();
    }

    @FXML
    private void handlePasswordInput() {
        String password = passwordField.getText();
        updatePasswordStrength(password);
        updatePasswordMatch();
    }

    @FXML
    private void handleConfirmPasswordInput() {
        updatePasswordMatch();
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }

        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLength = password.length() >= 8;

        if (hasLength && hasLower && hasUpper && hasDigit) {
            passwordStrengthLabel.setText("Strong");
            passwordStrengthLabel.setStyle("-fx-text-fill: green;");
        } else if (hasLength && ((hasLower && hasUpper) || (hasLower && hasDigit) || (hasUpper && hasDigit))) {
            passwordStrengthLabel.setText("Medium");
            passwordStrengthLabel.setStyle("-fx-text-fill: orange;");
        } else {
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void updatePasswordMatch() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (confirm.isEmpty()) {
            passwordMatchLabel.setText("");
            return;
        }

        if (password.equals(confirm)) {
            passwordMatchLabel.setText("Passwords match");
            passwordMatchLabel.setStyle("-fx-text-fill: green;");
        } else {
            passwordMatchLabel.setText("Passwords do not match");
            passwordMatchLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleRegisterResponse(AuthResponse response) {
        if (response.getToken() != null && !response.getToken().isBlank()) {
            com.authsystem.util.JwtTokenUtil.setToken(response.getToken());
            com.authsystem.util.JwtTokenUtil.setCurrentUsername(response.getUsername());
        }
        if (response.isRequiresOtp()) {
            AuthSystemApplication.showOtpView(response.getUsername());
            showMessage("Registration successful! Please check your email for OTP verification.", true);
        } else {
            showMessage("Registration successful! Please login.", true);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> AuthSystemApplication.showLoginView());
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        progressIndicator.setVisible(loading);
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
        }
        messageLabel.setVisible(!message.isEmpty());
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    @SuppressWarnings("unused")
    private static class RegisterRequest {
        private String username;
        private String email;
        private String fullName;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @SuppressWarnings("unused")
    private static class AuthResponse {
        private String token;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private boolean requiresOtp;
        private boolean requiresTotp;
        private boolean requiresFace;
        private String message;

        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public boolean isRequiresOtp() { return requiresOtp; }
        public boolean isRequiresTotp() { return requiresTotp; }
        public boolean isRequiresFace() { return requiresFace; }
        public String getMessage() { return message; }
    }
}
