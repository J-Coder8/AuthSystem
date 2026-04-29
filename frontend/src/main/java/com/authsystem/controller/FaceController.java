package com.authsystem.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.authsystem.AuthSystemApplication;
import com.authsystem.model.FaceLoginResponse;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.service.AuthService;
import com.authsystem.service.FaceService;
import com.authsystem.util.JwtTokenUtil;
import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FaceController {

    @FXML private ImageView previewImageView;
    @FXML private Label statusLabel;
    @FXML private Label modeLabel;
    @FXML private TextField usernameField;
    @FXML private Button captureButton;
    @FXML private Button verifyButton;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator progressIndicator;

    private String mode;
    private Webcam webcam;
    private BufferedImage capturedImage;
    private volatile BufferedImage latestFrame;
    private ScheduledExecutorService cameraExecutor;
    private final AuthService authService = new AuthService();
    private final FaceService faceService = new FaceService();

    public void setMode(String mode) {
        this.mode = mode;
        if ("register".equals(mode)) {
            modeLabel.setText("Register your face for login");
            verifyButton.setVisible(false);
            registerButton.setVisible(true);
            usernameField.setText(JwtTokenUtil.getCurrentUsername());
            usernameField.setEditable(false);
        } else {
            modeLabel.setText("Verify your face to login");
            verifyButton.setVisible(true);
            registerButton.setVisible(false);
        }
        startCamera();
    }

    private void startCamera() {
        setCameraControls(false);
        statusLabel.setText("Starting camera...");
        Thread cameraThread = new Thread(() -> {
            try {
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    Platform.runLater(() -> statusLabel.setText("No webcam detected."));
                    return;
                }

                java.awt.Dimension[] sizes = webcam.getViewSizes();
                if (sizes != null && sizes.length > 0) {
                    java.awt.Dimension selected = sizes[0];
                    for (java.awt.Dimension size : sizes) {
                        if (size.width >= 640 && size.height >= 480) {
                            selected = size;
                            break;
                        }
                    }
                    webcam.setViewSize(selected);
                }

                if (!webcam.open()) {
                    Platform.runLater(() -> statusLabel.setText("Unable to open camera. Close other apps using it and try again."));
                    return;
                }

                Platform.runLater(() -> {
                    statusLabel.setText("Camera ready. Position your face and capture a frame.");
                    setCameraControls(true);
                });

                cameraExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                    Thread thread = new Thread(runnable, "face-camera-preview");
                    thread.setDaemon(true);
                    return thread;
                });
                cameraExecutor.scheduleAtFixedRate(() -> {
                    if (webcam == null || !webcam.isOpen()) {
                        return;
                    }
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        latestFrame = image;
                        Image fxImage = SwingFXUtils.toFXImage(image, null);
                        Platform.runLater(() -> previewImageView.setImage(fxImage));
                    }
                }, 0, 80, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Camera error: " + e.getMessage());
                    setCameraControls(false);
                });
            }
        }, "face-camera-open");
        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        stopCamera();
        if ("register".equals(mode)) {
            AuthSystemApplication.showDashboardView(
                JwtTokenUtil.getCurrentUsername(), null, null, null);
        } else {
            AuthSystemApplication.showLoginView();
        }
    }

    @FXML
    private void handleCapture(ActionEvent event) {
        if (latestFrame != null) {
            capturedImage = latestFrame;
            Image fxImage = SwingFXUtils.toFXImage(capturedImage, null);
            previewImageView.setImage(fxImage);
            statusLabel.setText("Face captured. Click Verify or Register.");
        } else {
            statusLabel.setText("No camera frame available yet. Wait for the preview to appear.");
        }
    }

    @FXML
    private void handleVerify(ActionEvent event) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            return;
        }
        if (capturedImage == null) {
            statusLabel.setText("Please capture your face first.");
            return;
        }
        setLoading(true);
        String base64Image = encodeImageToBase64(capturedImage);
        new Thread(() -> {
            ApiResponse<FaceLoginResponse> response = authService.verifyFace(username, base64Image);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess() && response.getData() != null) {
                    FaceLoginResponse faceResponse = response.getData();
                    if (faceResponse.isFaceMatched()) {
                        statusLabel.setText("Face verified! " + faceResponse.getMessage());
                        if (faceResponse.isRequiresTotp()) {
                            AuthSystemApplication.showOtpView(username, true);
                        } else if (faceResponse.isRequiresOtp()) {
                            AuthSystemApplication.showOtpView(username);
                        } else {
                            AuthSystemApplication.showDashboardView(
                                faceResponse.getUsername(),
                                faceResponse.getEmail(),
                                faceResponse.getFullName(),
                                "USER"
                            );
                        }
                    } else {
                        statusLabel.setText("Face verification failed.");
                    }
                } else {
                    statusLabel.setText("Error: " + response.getErrorMessage());
                }
            });
        }).start();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            statusLabel.setText("Username is required.");
            return;
        }
        if (capturedImage == null) {
            statusLabel.setText("Please capture your face first.");
            return;
        }
        setLoading(true);
        String base64Image = encodeImageToBase64(capturedImage);
        String token = JwtTokenUtil.getToken();
        new Thread(() -> {
            ApiResponse<Void> response = faceService.registerFace(username, base64Image, token);
            Platform.runLater(() -> {
                setLoading(false);
                if (response.isSuccess()) {
                    statusLabel.setText("Face registered successfully!");
                } else {
                    statusLabel.setText("Registration failed: " + response.getErrorMessage());
                }
            });
        }).start();
    }

    private String encodeImageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisible(loading);
        captureButton.setDisable(loading);
        verifyButton.setDisable(loading);
        registerButton.setDisable(loading);
    }

    private void setCameraControls(boolean enabled) {
        captureButton.setDisable(!enabled);
        verifyButton.setDisable(!enabled || !"verify".equals(mode));
        registerButton.setDisable(!enabled || !"register".equals(mode));
    }

    private void stopCamera() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
            cameraExecutor = null;
        }
        if (webcam != null) {
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            } finally {
                webcam = null;
                latestFrame = null;
            }
        }
    }
}
