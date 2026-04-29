package com.authsystem.controller;

import com.authsystem.AuthSystemApplication;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.service.AuthService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {

    @FXML private VBox requestForm;
    @FXML private VBox resetForm;
    @FXML private TextField identifierField;
    @FXML private TextField resetIdentifierField;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Label resetErrorLabel;
    @FXML private Label resetInfoLabel;
    @FXML private Label resetPasswordStrengthLabel;
    @FXML private Label resetPasswordMatchLabel;
    @FXML private Button sendOtpButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private ProgressIndicator resetProgressIndicator;

    private final AuthService authService = new AuthService();
    private String identifier;

    @FXML
    private void handleBack(ActionEvent event) {
        AuthSystemApplication.showLoginView();
    }

    @FXML
    private void handleSendOtp(ActionEvent event) {
        identifier = identifierField.getText().trim();
        if (identifier.isEmpty()) {
            showRequestMessage("Please enter your username or email", false);
            return;
        }
        setRequestLoading(true);
        showRequestMessage("", true);

        new Thread(() -> {
            ApiResponse<Void> response = authService.forgotPassword(identifier);
            Platform.runLater(() -> {
                setRequestLoading(false);
                if (response.isSuccess()) {
                    showRequestMessage("OTP generated! Check the backend console.", true);
                    requestForm.setVisible(false);
                    resetForm.setVisible(true);
                    resetIdentifierField.setText(identifier);
                } else {
                    showRequestMessage(response.getErrorMessage() != null ? response.getErrorMessage() : "Failed to generate OTP", false);
                }
            });
        }).start();
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String otpCode = otpField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (otpCode.isEmpty() || newPassword.isEmpty()) {
            showResetMessage("Please fill in all fields", false);
            return;
        }
        if (newPassword.length() < 8) {
            showResetMessage("Password must be at least 8 characters", false);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showResetMessage("Passwords do not match", false);
            return;
        }

        setResetLoading(true);
        showResetMessage("", true);

        new Thread(() -> {
            ApiResponse<Void> response = authService.resetPassword(identifier, otpCode, newPassword);
            Platform.runLater(() -> {
                setResetLoading(false);
                if (response.isSuccess()) {
                    showResetMessage("Password reset successful! Redirecting to login...", true);
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> AuthSystemApplication.showLoginView());
                    }).start();
                } else {
                    showResetMessage(response.getErrorMessage() != null ? response.getErrorMessage() : "Password reset failed", false);
                }
            });
        }).start();
    }

    @FXML
    private void handlePasswordInput() {
        String password = newPasswordField.getText();
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLength = password.length() >= 8;

        if (hasLength && hasLower && hasUpper && hasDigit) {
            resetPasswordStrengthLabel.setText("Strong");
            resetPasswordStrengthLabel.setStyle("-fx-text-fill: green;");
        } else if (hasLength && ((hasLower && hasUpper) || (hasLower && hasDigit) || (hasUpper && hasDigit))) {
            resetPasswordStrengthLabel.setText("Medium");
            resetPasswordStrengthLabel.setStyle("-fx-text-fill: orange;");
        } else {
            resetPasswordStrengthLabel.setText("Weak");
            resetPasswordStrengthLabel.setStyle("-fx-text-fill: red;");
        }

        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) {
            resetPasswordMatchLabel.setText("");
        } else if (password.equals(confirm)) {
            resetPasswordMatchLabel.setText("Passwords match");
            resetPasswordMatchLabel.setStyle("-fx-text-fill: green;");
        } else {
            resetPasswordMatchLabel.setText("Passwords do not match");
            resetPasswordMatchLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void showRequestMessage(String message, boolean isSuccess) {
        if (isSuccess) {
            successLabel.setText(message);
            errorLabel.setText("");
        } else {
            errorLabel.setText(message);
            successLabel.setText("");
        }
    }

    private void showResetMessage(String message, boolean isSuccess) {
        if (isSuccess) {
            resetInfoLabel.setText(message);
            resetErrorLabel.setText("");
        } else {
            resetErrorLabel.setText(message);
            resetInfoLabel.setText("");
        }
    }

    private void setRequestLoading(boolean loading) {
        sendOtpButton.setDisable(loading);
        progressIndicator.setVisible(loading);
    }

    private void setResetLoading(boolean loading) {
        resetProgressIndicator.setVisible(loading);
    }
}

