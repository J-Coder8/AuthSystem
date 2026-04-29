package com.authsystem.controller;

import com.authsystem.AuthSystemApplication;
import com.authsystem.model.AuthResponse;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.service.AuthService;
import com.authsystem.util.JwtTokenUtil;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class OtpController {

    @FXML private TextField otpField;
    @FXML private Label instructionLabel;
    @FXML private Label countdownLabel;
    @FXML private Label messageLabel;
    @FXML private Button verifyButton;
    @FXML private Button resendButton;
    @FXML private ProgressIndicator progressIndicator;

    private String username;
    private String deliveryEmail;
    private boolean totpMode;
    private String initialMessage;

    private final AuthService authService = new AuthService();
    private Timeline countdownTimeline;
    private int remainingSeconds = 300; // 5 minutes for OTP

    public void setUsername(String username) { this.username = username; }

    public void setDeliveryEmail(String deliveryEmail) {
        this.deliveryEmail = deliveryEmail;
        configureView();
    }

    public void setTotpMode(boolean totpMode) {
        this.totpMode = totpMode;
        configureView();
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
        configureView();
    }

    @FXML
    public void initialize() {
        configureView();
    }

    private void configureView() {
        if (instructionLabel == null) {
            return;
        }
        if (totpMode) {
            instructionLabel.setText("Enter your TOTP code from your authenticator app");
            resendButton.setVisible(false);
            countdownLabel.setVisible(false);
            stopCountdown();
        } else {
            instructionLabel.setText("Enter the OTP code shown in the backend console");
            if (deliveryEmail != null && !deliveryEmail.isBlank()) {
                instructionLabel.setText("Enter the OTP sent to " + deliveryEmail);
            }
            resendButton.setVisible(true);
            countdownLabel.setVisible(true);
            if (countdownTimeline == null) {
                startCountdown();
            }
        }
        if (initialMessage != null && !initialMessage.isBlank()) {
            showMessage(initialMessage, true);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        stopCountdown();
        AuthSystemApplication.showLoginView();
    }

    @FXML
    private void handleVerify(ActionEvent event) {
        String otpCode = otpField.getText().trim();
        if (otpCode.isEmpty()) {
            showMessage("Please enter the OTP code", false);
            return;
        }
        setLoading(true);
        showMessage("", true);

        new Thread(() -> {
            ApiResponse<AuthResponse> response = authService.verifyOtp(username, otpCode);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess() && response.getData() != null) {
                    AuthResponse data = response.getData();
                    if (data.getToken() != null && !data.getToken().isBlank()) {
                        JwtTokenUtil.setToken(data.getToken());
                        JwtTokenUtil.setCurrentUsername(data.getUsername());
                    }
                    showMessage(data.getMessage() != null ? data.getMessage() : "Verification successful!", true);
                    stopCountdown();
                    // Navigate to dashboard after short delay
                    new Thread(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> {
                            if (data.isRequiresFace()) {
                                AuthSystemApplication.showFaceView("verify");
                            } else {
                                AuthSystemApplication.showDashboardView(
                                    data.getUsername(), data.getEmail(), data.getFullName(), data.getRole()
                                );
                            }
                        });
                    }).start();
                } else {
                    showMessage(response.getErrorMessage() != null ? response.getErrorMessage() : "Verification failed", false);
                }
            });
        }).start();
    }

    @FXML
    private void handleResend(ActionEvent event) {
        if (totpMode) return;
        setLoading(true);
        showMessage("", true);
        new Thread(() -> {
            ApiResponse<AuthResponse> response = authService.resendOtp(username);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    showMessage("OTP resent successfully! Check the backend console.", true);
                    remainingSeconds = 300;
                    startCountdown();
                } else {
                    showMessage(response.getErrorMessage() != null ? response.getErrorMessage() : "Failed to resend OTP", false);
                }
            });
        }).start();
    }

    private void startCountdown() {
        stopCountdown();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                countdownLabel.setText("OTP expired. Please resend.");
                resendButton.setDisable(false);
                stopCountdown();
            } else {
                int mins = remainingSeconds / 60;
                int secs = remainingSeconds % 60;
                countdownLabel.setText(String.format("Expires in: %d:%02d", mins, secs));
                resendButton.setDisable(true);
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void setLoading(boolean loading) {
        verifyButton.setDisable(loading);
        resendButton.setDisable(loading || (!totpMode && remainingSeconds > 0));
        progressIndicator.setVisible(loading);
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        if (message == null || message.isEmpty()) {
            messageLabel.setVisible(false);
        } else {
            messageLabel.setVisible(true);
            messageLabel.setStyle(isSuccess ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
    }
}

