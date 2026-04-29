package com.authsystem.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.authsystem.AuthSystemApplication;
import com.authsystem.model.AuthResponse;
import com.authsystem.model.AuthenticatorAccountDto;
import com.authsystem.model.AuthenticatorAccountRequest;
import com.authsystem.model.BackupCodesResponse;
import com.authsystem.model.LoginHistoryDto;
import com.authsystem.model.NotificationDto;
import com.authsystem.model.SessionInfo;
import com.authsystem.model.TotpSetupResponse;
import com.authsystem.model.UserProfileResponse;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.service.AuthenticatorService;
import com.authsystem.service.FaceService;
import com.authsystem.service.NotificationService;
import com.authsystem.service.UserService;
import com.authsystem.util.DialogUtil;
import com.authsystem.util.JwtTokenUtil;
import com.authsystem.util.QrCodeService;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label unreadAlertsLabel;
    @FXML private Button adminPanelBtn;
    @FXML private Button themeToggleBtn;

    @FXML private Label totalLoginsLabel;
    @FXML private Label totalSessionsLabel;
    @FXML private Label securityScoreLabel;
    @FXML private Label accountAgeLabel;
    @FXML private Label unreadStatsLabel;
    @FXML private Label profileStatusStatLabel;
    @FXML private Label backupCodesStatLabel;
    @FXML private Label lastSeenStatLabel;
    @FXML private Label lastLoginIpLabel;
    @FXML private Label lastLoginDeviceLabel;
    @FXML private Label lastLoginTimeLabel;
    @FXML private ProgressBar profileCompletionBar;
    @FXML private Label profileCompletionLabel;
    @FXML private Label snapshotTotpLabel;
    @FXML private Label snapshotFaceLabel;
    @FXML private Label snapshotAccountLabel;
    @FXML private ListView<String> recentActivityList;

    @FXML private TableView<LoginHistoryDto> historyTable;
    @FXML private TableView<SessionInfo> sessionsTable;
    @FXML private Label sessionDeviceLabel;
    @FXML private Label sessionIpLabel;
    @FXML private Label sessionLastActiveLabel;
    @FXML private Label sessionActiveForLabel;
    @FXML private Label sessionCurrentLabel;
    @FXML private Label sessionIdLabel;
    @FXML private Label sessionAuthenticatorLabel;

    @FXML private Label totpStatusLabel;
    @FXML private Button enableTotpBtn;
    @FXML private Button disableTotpBtn;
    @FXML private VBox totpLiveCard;
    @FXML private Label totpLiveCodeLabel;
    @FXML private Label totpLiveCountdownLabel;
    @FXML private ProgressBar totpCooldownBar;
    @FXML private Label backupCodesStatusLabel;
    @FXML private Label faceStatusLabel;
    @FXML private Button registerFaceBtn;
    @FXML private Button viewFaceStatusBtn;
    @FXML private Button enableFaceLoginBtn;
    @FXML private Button disableFaceLoginBtn;
    @FXML private CheckBox checkTotp;
    @FXML private CheckBox checkFace;
    @FXML private CheckBox checkProfile;
    @FXML private CheckBox checkAlerts;
    @FXML private Label passwordChangedLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label roleLabel;

    @FXML private Label authenticatorStatusLabel;
    @FXML private Label authenticatorCountdownLabel;
    @FXML private ProgressBar authenticatorCooldownBar;
    @FXML private TableView<AuthenticatorAccountDto> authenticatorTable;

    @FXML private TableView<NotificationDto> notificationsTable;

    @FXML private ComboBox<String> designPresetCombo;
    @FXML private CheckBox darkModeCheck;
    @FXML private ComboBox<String> designTemplateCombo;
    @FXML private TextArea customCssArea;
    @FXML private Label designStatusLabel;
    @FXML private CheckBox emailNotificationsCheck;
    @FXML private CheckBox loginAlertsCheck;
    @FXML private Label usernameLabel;
    @FXML private Label settingsEmailLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label phoneLabel;

    @FXML private TabPane tabPane;

    private String currentUsername;
    private String currentEmail;
    private String currentFullName;
    private String currentRole;
    private UserProfileResponse profile;
    private Timeline cooldownTimeline;
    private int totpCooldownSeconds = -1;
    private int authenticatorCooldownSeconds = -1;
    private boolean totpRefreshInFlight;
    private boolean authenticatorRefreshInFlight;

    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();
    private final FaceService faceService = new FaceService();
    private final AuthenticatorService authenticatorService = new AuthenticatorService();

    private static final Map<String, String> DESIGN_TEMPLATES = Map.of(
            "Clean Light", ".root {\n    -fx-font-family: \"Segoe UI\";\n    -fx-background-color: #f4f7fb;\n}\n.card {\n    -fx-background-color: white;\n    -fx-border-color: #d8e0ea;\n}\n.btn-primary {\n    -fx-background-color: #2563eb;\n    -fx-text-fill: white;\n}",
            "Dark Console", ".root {\n    -fx-font-family: \"Consolas\";\n    -fx-background-color: #101418;\n}\n.card {\n    -fx-background-color: #182026;\n    -fx-border-color: #2f3b45;\n}\n.label-secondary {\n    -fx-text-fill: #9fb0bf;\n}\n.btn-primary {\n    -fx-background-color: #22c55e;\n    -fx-text-fill: #07110a;\n}",
            "High Contrast", ".root {\n    -fx-background-color: #ffffff;\n}\n.card {\n    -fx-background-color: #ffffff;\n    -fx-border-color: #111111;\n    -fx-border-width: 2;\n}\n.header-label {\n    -fx-text-fill: #111111;\n}\n.btn-primary {\n    -fx-background-color: #111111;\n    -fx-text-fill: #ffffff;\n}"
    );

    public void setUserInfo(String username, String email, String fullName, String role) {
        this.currentUsername = username;
        this.currentEmail = email;
        this.currentFullName = fullName;
        this.currentRole = role;
        Platform.runLater(() -> {
            welcomeLabel.setText("Welcome, " + (fullName != null ? fullName : username));
            emailLabel.setText(email != null ? email : "");
            adminPanelBtn.setVisible("ADMIN".equals(role));
            themeToggleBtn.setText(AuthSystemApplication.isDarkModeEnabled() ? "Light Mode" : "Dark Mode");
            loadProfileData();
            loadLoginHistory();
            loadSessions();
            loadNotifications();
            loadAuthenticatorCodes();
        });
    }

    @FXML
    public void initialize() {
        if (designPresetCombo != null) {
            designPresetCombo.getItems().addAll(AuthSystemApplication.getDesignPresets());
            designPresetCombo.setValue(AuthSystemApplication.getDesignPreset());
        }
        if (darkModeCheck != null) {
            darkModeCheck.setSelected(AuthSystemApplication.isDarkModeEnabled());
        }
        if (designTemplateCombo != null) {
            designTemplateCombo.getItems().setAll(DESIGN_TEMPLATES.keySet());
            designTemplateCombo.setValue("Clean Light");
        }
        if (customCssArea != null) {
            customCssArea.setText(AuthSystemApplication.getCustomDesignCss());
        }
        if (sessionsTable != null) {
            sessionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    sessionDeviceLabel.setText("Device: " + newVal.getDeviceName());
                    sessionIpLabel.setText("IP Address: " + newVal.getIpAddress());
                    sessionLastActiveLabel.setText("Last Active: " + formatDateTime(newVal.getLastActiveAt()));
                    if (sessionActiveForLabel != null) sessionActiveForLabel.setText("Active For: " + newVal.getActiveFor());
                    sessionCurrentLabel.setText("Current Session: " + (newVal.isCurrent() ? "Yes" : "No"));
                    sessionIdLabel.setText("Session ID: " + newVal.getSessionId());
                }
            });
        }
        startCooldownAnimation();
        setupTableCellFactories();
    }

private void setupTableCellFactories() {
        // Skip cell factory setup to avoid FXML initialization crash
        // Cell factories will be applied after data loads
        System.out.println("[Dashboard] Table cell factories skipped to avoid FXML crash");
    }


    private void loadProfileData() {
        String token = JwtTokenUtil.getToken();
        if (token == null || token.isBlank()) {
            System.err.println("[Dashboard] No token available for loadProfileData");
            return;
        }
        new Thread(() -> {
            try {
                ApiResponse<UserProfileResponse> response = userService.getProfile(token);
                Platform.runLater(() -> {
                    try {
                        if (response.isSuccess() && response.getData() != null) {
                            profile = response.getData();
                            updateOverviewTab(profile);
                            updateSecurityTab(profile);
                            updateSettingsTab(profile);
                        } else {
                            System.err.println("[Dashboard] getProfile failed: " + response.getErrorMessage());
                        }
                    } catch (Exception e) {
                        System.err.println("[Dashboard] Error in updateProfile UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in loadProfileData thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void updateOverviewTab(UserProfileResponse p) {
        if (p == null) return;
        int score = 0;
        if (p.isTotpEnabled()) score += 30;
        if (p.isFaceEnabled()) score += 30;
        if (p.getPhone() != null && !p.getPhone().isBlank()) score += 10;
        if (p.getBackupCodesRemaining() != null && p.getBackupCodesRemaining() > 0) score += 10;
        if (p.isLoginAlerts()) score += 10;
        if (p.getPasswordChangedAt() != null &&
            ChronoUnit.DAYS.between(p.getPasswordChangedAt(), LocalDateTime.now()) < 90) score += 10;
        if (securityScoreLabel != null) securityScoreLabel.setText(score + "/100");

        if (p.getCreatedAt() != null && accountAgeLabel != null) {
            long days = ChronoUnit.DAYS.between(p.getCreatedAt(), LocalDateTime.now());
            accountAgeLabel.setText(days + " days");
        }

        int complete = 0, total = 6;
        if (p.getFullName() != null && !p.getFullName().isBlank()) complete++;
        if (p.getEmail() != null && !p.getEmail().isBlank()) complete++;
        if (p.getPhone() != null && !p.getPhone().isBlank()) complete++;
        if (p.getBio() != null && !p.getBio().isBlank()) complete++;
        if (p.getAddress() != null && !p.getAddress().isBlank()) complete++;
        if (p.getProfilePicture() != null && !p.getProfilePicture().isBlank()) complete++;
        int pct = (complete * 100) / total;
        if (profileCompletionBar != null) profileCompletionBar.setProgress(pct / 100.0);
        if (profileCompletionLabel != null) profileCompletionLabel.setText(pct + "% complete");

        if (p.getBackupCodesRemaining() != null && p.getBackupCodesRemaining() > 0) {
            if (backupCodesStatLabel != null) backupCodesStatLabel.setText(p.getBackupCodesRemaining() + " remaining");
        } else {
            if (backupCodesStatLabel != null) backupCodesStatLabel.setText("None");
        }

        if (p.getLastLoginAt() != null) {
            if (lastSeenStatLabel != null) lastSeenStatLabel.setText(formatDateTime(p.getLastLoginAt()));
            if (lastLoginTimeLabel != null) lastLoginTimeLabel.setText(formatDateTime(p.getLastLoginAt()));
        }
        if (p.getLastLoginIp() != null && lastLoginIpLabel != null) lastLoginIpLabel.setText(p.getLastLoginIp());
        if (p.getLastLoginDevice() != null && lastLoginDeviceLabel != null) lastLoginDeviceLabel.setText(p.getLastLoginDevice());

        if (snapshotTotpLabel != null) snapshotTotpLabel.setText("TOTP: " + (p.isTotpEnabled() ? "Enabled" : "Disabled"));
        if (snapshotFaceLabel != null) snapshotFaceLabel.setText("Face Login: " + (p.isFaceEnabled() ? "Enabled" : "Disabled"));
        if (snapshotAccountLabel != null) snapshotAccountLabel.setText("Account: " + (p.isEnabled() ? "Active" : "Disabled"));
        if (profileStatusStatLabel != null) profileStatusStatLabel.setText(p.isEnabled() ? "Active" : "Disabled");
    }

    private void updateSecurityTab(UserProfileResponse p) {
        if (p == null) return;
        if (totpStatusLabel != null) totpStatusLabel.setText(p.isTotpEnabled() ? "TOTP is enabled" : "TOTP is not enabled");
        if (enableTotpBtn != null) enableTotpBtn.setVisible(!p.isTotpEnabled());
        if (disableTotpBtn != null) disableTotpBtn.setVisible(p.isTotpEnabled());
        if (totpLiveCard != null) totpLiveCard.setVisible(p.isTotpEnabled());
        if (p.isTotpEnabled()) loadTotpLiveCode();

        if (p.getBackupCodesRemaining() != null && p.getBackupCodesRemaining() > 0) {
            if (backupCodesStatusLabel != null) backupCodesStatusLabel.setText(p.getBackupCodesRemaining() + " backup codes remaining");
        } else {
            if (backupCodesStatusLabel != null) backupCodesStatusLabel.setText("No backup codes generated yet");
        }

        if (faceStatusLabel != null) faceStatusLabel.setText(p.isFaceEnabled() ? "Face login is enabled" : "Face login is not configured");
        if (enableFaceLoginBtn != null) enableFaceLoginBtn.setVisible(!p.isFaceEnabled());
        if (disableFaceLoginBtn != null) disableFaceLoginBtn.setVisible(p.isFaceEnabled());

        if (checkTotp != null) checkTotp.setSelected(p.isTotpEnabled());
        if (checkFace != null) checkFace.setSelected(p.isFaceEnabled());
        if (checkProfile != null) checkProfile.setSelected(profileCompletionBar != null && profileCompletionBar.getProgress() >= 0.8);
        if (checkAlerts != null) checkAlerts.setSelected(p.isLoginAlerts());

        if (p.getPasswordChangedAt() != null && passwordChangedLabel != null) {
            passwordChangedLabel.setText("Last changed: " + formatDateTime(p.getPasswordChangedAt()));
        }
        if (accountStatusLabel != null) accountStatusLabel.setText(p.isEnabled() ? "Active" : "Disabled");
        if (roleLabel != null) roleLabel.setText("Role: " + (p.getRole() != null ? p.getRole() : "USER"));
    }

    private void updateSettingsTab(UserProfileResponse p) {
        if (p == null) return;
        if (usernameLabel != null) usernameLabel.setText(p.getUsername());
        if (settingsEmailLabel != null) settingsEmailLabel.setText(p.getEmail());
        if (fullNameLabel != null) fullNameLabel.setText(p.getFullName());
        if (phoneLabel != null) phoneLabel.setText(p.getPhone() != null ? p.getPhone() : "N/A");
        if (emailNotificationsCheck != null) emailNotificationsCheck.setSelected(p.isEmailNotifications());
        if (loginAlertsCheck != null) loginAlertsCheck.setSelected(p.isLoginAlerts());
    }

    private void loadLoginHistory() {
        String token = JwtTokenUtil.getToken();
        if (token == null) {
            System.err.println("[Dashboard] No token available for loadLoginHistory");
            return;
        }
        new Thread(() -> {
            try {
                ApiResponse<List<LoginHistoryDto>> response = userService.getLoginHistory(token);
                Platform.runLater(() -> {
                    try {
                        if (response.isSuccess() && response.getData() != null) {
                            if (historyTable != null) historyTable.getItems().setAll(response.getData());
                            if (totalLoginsLabel != null) totalLoginsLabel.setText(String.valueOf(response.getData().size()));
                            if (recentActivityList != null) {
                                recentActivityList.getItems().clear();
                                response.getData().stream().limit(5).forEach(h -> {
                                    String msg = (h.isSuccess() ? "Login" : "Failed login") + " from " + h.getIpAddress()
                                            + " at " + formatDateTime(h.getLoginTime());
                                    recentActivityList.getItems().add(msg);
                                });
                            }
                        } else {
                            System.err.println("[Dashboard] getLoginHistory failed: " + response.getErrorMessage());
                        }
                    } catch (Exception e) {
                        System.err.println("[Dashboard] Error in loadLoginHistory UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in loadLoginHistory thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSessions() {
        String token = JwtTokenUtil.getToken();
        if (token == null) {
            System.err.println("[Dashboard] No token available for loadSessions");
            return;
        }
        new Thread(() -> {
            try {
                ApiResponse<List<SessionInfo>> response = userService.getSessions(token);
                Platform.runLater(() -> {
                    try {
                        if (response.isSuccess() && response.getData() != null) {
                            if (sessionsTable != null) sessionsTable.getItems().setAll(response.getData());
                            if (totalSessionsLabel != null) totalSessionsLabel.setText(String.valueOf(response.getData().size()));
                        } else {
                            System.err.println("[Dashboard] getSessions failed: " + response.getErrorMessage());
                        }
                    } catch (Exception e) {
                        System.err.println("[Dashboard] Error in loadSessions UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in loadSessions thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void loadNotifications() {
        String token = JwtTokenUtil.getToken();
        if (token == null) {
            System.err.println("[Dashboard] No token available for loadNotifications");
            return;
        }
        new Thread(() -> {
            try {
                ApiResponse<List<NotificationDto>> response = notificationService.getNotifications(token);
                ApiResponse<Long> unreadResponse = notificationService.getUnreadCount(token);
                Platform.runLater(() -> {
                    try {
                        if (response.isSuccess() && response.getData() != null) {
                            if (notificationsTable != null) notificationsTable.getItems().setAll(response.getData());
                        } else {
                            System.err.println("[Dashboard] getNotifications failed: " + response.getErrorMessage());
                        }
                        if (unreadResponse.isSuccess() && unreadResponse.getData() != null) {
                            if (unreadAlertsLabel != null) unreadAlertsLabel.setText("Alerts: " + unreadResponse.getData());
                            if (unreadStatsLabel != null) unreadStatsLabel.setText(String.valueOf(unreadResponse.getData()));
                        } else {
                            System.err.println("[Dashboard] getUnreadCount failed: " + unreadResponse.getErrorMessage());
                        }
                    } catch (Exception e) {
                        System.err.println("[Dashboard] Error in loadNotifications UI: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in loadNotifications thread: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void loadAuthenticatorCodes() {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        if (authenticatorRefreshInFlight) return;
        authenticatorRefreshInFlight = true;
        new Thread(() -> {
            ApiResponse<List<AuthenticatorAccountDto>> response = authenticatorService.refreshCodes(token);
            Platform.runLater(() -> {
                authenticatorRefreshInFlight = false;
                if (response.isSuccess() && response.getData() != null) {
                    if (authenticatorTable != null) {
                        authenticatorTable.getItems().setAll(response.getData());
                    }
                    int remaining = response.getData().stream()
                            .filter(item -> item.getRemainingSeconds() != null)
                            .findFirst()
                            .map(AuthenticatorAccountDto::getRemainingSeconds)
                            .orElse(-1);
                    if (authenticatorStatusLabel != null) {
                        authenticatorStatusLabel.setText(response.getData().isEmpty()
                                ? "No accounts added yet"
                                : response.getData().size() + " account(s) connected");
                    }
                    if (sessionAuthenticatorLabel != null) {
                        sessionAuthenticatorLabel.setText(response.getData().isEmpty()
                                ? "Authenticator: no connected accounts"
                                : "Authenticator: " + response.getData().size() + " connected account(s)");
                    }
                    if (authenticatorCountdownLabel != null) {
                        setAuthenticatorCooldown(remaining);
                    }
                } else if (authenticatorStatusLabel != null) {
                    authenticatorStatusLabel.setText(response.getErrorMessage());
                    if (sessionAuthenticatorLabel != null) {
                        sessionAuthenticatorLabel.setText("Authenticator: " + response.getErrorMessage());
                    }
                    setAuthenticatorCooldown(-1);
                }
            });
        }).start();
    }

    private void loadTotpLiveCode() {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        if (totpRefreshInFlight) return;
        totpRefreshInFlight = true;
        new Thread(() -> {
            ApiResponse<AuthResponse> response = userService.getTotpCode(token);
            Platform.runLater(() -> {
                totpRefreshInFlight = false;
                if (response.isSuccess() && response.getData() != null) {
                    AuthResponse data = response.getData();
                    if (totpLiveCodeLabel != null) {
                        totpLiveCodeLabel.setText(data.getTotpCode() != null ? data.getTotpCode() : "------");
                    }
                    if (totpLiveCountdownLabel != null) {
                        Integer seconds = data.getTotpRemainingSeconds();
                        setTotpCooldown(seconds != null ? seconds : -1);
                    }
                } else if (totpLiveCodeLabel != null) {
                    totpLiveCodeLabel.setText("------");
                    setTotpCooldown(-1);
                }
            });
        }).start();
    }

    private void startCooldownAnimation() {
        if (cooldownTimeline != null) {
            return;
        }
        cooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> tickCooldowns()));
        cooldownTimeline.setCycleCount(Timeline.INDEFINITE);
        cooldownTimeline.play();
    }

    private void tickCooldowns() {
        if (totpCooldownSeconds > 0) {
            setTotpCooldown(totpCooldownSeconds - 1);
        } else if (totpCooldownSeconds == 0 && profile != null && profile.isTotpEnabled()) {
            loadTotpLiveCode();
        }

        if (authenticatorCooldownSeconds > 0) {
            setAuthenticatorCooldown(authenticatorCooldownSeconds - 1);
        } else if (authenticatorCooldownSeconds == 0) {
            loadAuthenticatorCodes();
        }
    }

    private void setTotpCooldown(int seconds) {
        totpCooldownSeconds = seconds;
        updateCooldown(totpLiveCountdownLabel, totpCooldownBar, seconds);
    }

    private void setAuthenticatorCooldown(int seconds) {
        authenticatorCooldownSeconds = seconds;
        updateCooldown(authenticatorCountdownLabel, authenticatorCooldownBar, seconds);
    }

    private void updateCooldown(Label label, ProgressBar bar, int seconds) {
        if (label != null) {
            label.setText(seconds >= 0 ? "Refreshes in: " + seconds + "s" : "Refreshes in: --s");
            if (seconds >= 0 && seconds <= 5) {
                pulse(label);
            }
        }
        if (bar != null) {
            bar.setProgress(seconds >= 0 ? Math.max(0, Math.min(1, seconds / 30.0)) : 0);
            bar.getStyleClass().remove("cooldown-low");
            if (seconds >= 0 && seconds <= 5) {
                bar.getStyleClass().add("cooldown-low");
            }
        }
    }

    private void pulse(Label label) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(180), label);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    private void promptAndEnableTotp(TotpSetupResponse setup, String token) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Enable TOTP");
        dialog.setHeaderText("Add this account to your authenticator app, then enter the current 6-digit code.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Image qrImage = QrCodeService.createQrCode(setup.getOtpauthUri(), 220);
        ImageView qrImageView = new ImageView(qrImage);
        qrImageView.setFitWidth(220);
        qrImageView.setFitHeight(220);
        qrImageView.setPreserveRatio(true);

        TextArea secretArea = new TextArea("Manual key:\n" + setup.getSecret());
        secretArea.setEditable(false);
        secretArea.setWrapText(true);
        secretArea.setPrefRowCount(3);
        TextField codeField = new TextField();
        codeField.setPromptText("123456");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Scan QR Code:"), 0, 0);
        grid.add(qrImageView, 0, 1);
        grid.add(secretArea, 0, 2);
        grid.add(new Label("Code:"), 0, 3);
        grid.add(codeField, 0, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == ButtonType.OK ? codeField.getText().trim() : null);

        Optional<String> code = dialog.showAndWait();
        code.filter(value -> !value.isBlank()).ifPresent(value -> new Thread(() -> {
            ApiResponse<AuthResponse> response = userService.enableTotp(setup.getSecret(), value, token);
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    DialogUtil.showInfo("TOTP Enabled", "Two-factor authentication is now enabled.");
                    loadProfileData();
                    loadTotpLiveCode();
                } else {
                    DialogUtil.showError("Enable TOTP", response.getErrorMessage());
                }
            });
        }).start());
    }

    private Optional<AuthenticatorAccountRequest> showAuthenticatorAccountDialog() {
        Dialog<AuthenticatorAccountRequest> dialog = new Dialog<>();
        dialog.setTitle("Add Authenticator Account");
        dialog.setHeaderText("Enter the issuer, account name, and Base32 secret.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField issuerField = new TextField();
        issuerField.setPromptText("Google, GitHub, Microsoft");
        TextField accountField = new TextField();
        accountField.setPromptText("name@example.com");
        TextField secretField = new TextField();
        secretField.setPromptText("JBSWY3DPEHPK3PXP");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Issuer"), 0, 0);
        grid.add(issuerField, 1, 0);
        grid.add(new Label("Account"), 0, 1);
        grid.add(accountField, 1, 1);
        grid.add(new Label("Secret"), 0, 2);
        grid.add(secretField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            return new AuthenticatorAccountRequest(
                    issuerField.getText().trim(),
                    accountField.getText().trim(),
                    secretField.getText().trim()
            );
        });
        return dialog.showAndWait();
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @FXML private void handleOpenAdminPanel(ActionEvent event) {
        AuthSystemApplication.showAdminPanelView(currentUsername, currentEmail, currentFullName);
        System.out.println("Opening admin panel as role: " + currentRole);
    }

    @FXML private void handleThemeToggle(ActionEvent event) {
        boolean newMode = !AuthSystemApplication.isDarkModeEnabled();
        AuthSystemApplication.setDarkModeEnabled(newMode);
        themeToggleBtn.setText(newMode ? "Light Mode" : "Dark Mode");
    }

    @FXML private void handleLogout(ActionEvent event) {
        JwtTokenUtil.clearToken();
        AuthSystemApplication.showLoginView();
    }

    @FXML private void handleEditProfile(ActionEvent event) {
        if (tabPane != null) tabPane.getSelectionModel().selectLast();
    }

    @FXML private void handleChangePassword(ActionEvent event) {
        if (tabPane != null) tabPane.getSelectionModel().select(3);
    }

    @FXML private void handleRegisterFace(ActionEvent event) {
        AuthSystemApplication.showFaceView("register");
    }

    @FXML private void handleRefresh(ActionEvent event) {
        loadProfileData();
        loadLoginHistory();
        loadSessions();
        loadNotifications();
        loadAuthenticatorCodes();
    }

    @FXML private void handleExportHistory(ActionEvent event) {
    }

    @FXML private void handleRefreshSessions(ActionEvent event) {
        loadSessions();
    }

    @FXML private void handleLogoutAllDevices(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                userService.logoutAllDevices(token);
                Platform.runLater(() -> {
                    JwtTokenUtil.clearToken();
                    AuthSystemApplication.showLoginView();
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleLogoutAllDevices: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleRevokeSelectedSession(ActionEvent event) {
        SessionInfo selected = sessionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                userService.revokeSession(selected.getSessionId(), token);
                Platform.runLater(() -> loadSessions());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleRevokeSelectedSession: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleEnableTotp(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                ApiResponse<TotpSetupResponse> setupResponse = userService.setupTotp(token);
                Platform.runLater(() -> {
                    if (!setupResponse.isSuccess() || setupResponse.getData() == null) {
                        DialogUtil.showError("Enable TOTP", setupResponse.getErrorMessage());
                        return;
                    }
                    promptAndEnableTotp(setupResponse.getData(), token);
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleEnableTotp: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleDisableTotp(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                userService.disableTotp(token);
                Platform.runLater(() -> loadProfileData());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleDisableTotp: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleRefreshTotpLive(ActionEvent event) {
        loadTotpLiveCode();
    }

    @FXML private void handleGenerateBackupCodes(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                ApiResponse<BackupCodesResponse> response = userService.generateBackupCodes(token);
                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        List<String> codes = response.getData().getCodes();
                        if (codes != null && !codes.isEmpty()) {
                            DialogUtil.showBackupCodes("Backup Codes", codes);
                        }
                        loadProfileData();
                    } else {
                        DialogUtil.showError("Backup Codes", response.getErrorMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleGenerateBackupCodes: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleViewFaceStatus(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                ApiResponse<AuthResponse> response = faceService.getFaceStatus(token);
                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        faceStatusLabel.setText(response.getData().getMessage());
                    } else {
                        System.err.println("[Dashboard] getFaceStatus failed: " + response.getErrorMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleViewFaceStatus: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleEnableFaceLogin(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                faceService.enableFaceLogin(token);
                Platform.runLater(() -> loadProfileData());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleEnableFaceLogin: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleDisableFaceLogin(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                faceService.disableFaceLogin(token);
                Platform.runLater(() -> loadProfileData());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleDisableFaceLogin: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleTrustDevice(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                userService.trustDevice(token);
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleTrustDevice: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleAddAuthenticatorAccount(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        Optional<AuthenticatorAccountRequest> request = showAuthenticatorAccountDialog();
        request.ifPresent(value -> new Thread(() -> {
            ApiResponse<AuthenticatorAccountDto> response = authenticatorService.addAccount(value, token);
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    loadAuthenticatorCodes();
                } else {
                    DialogUtil.showError("Authenticator", response.getErrorMessage());
                }
            });
        }).start());
    }

    @FXML private void handleDeleteAuthenticatorAccount(ActionEvent event) {
        AuthenticatorAccountDto selected = authenticatorTable != null
                ? authenticatorTable.getSelectionModel().getSelectedItem()
                : null;
        String token = JwtTokenUtil.getToken();
        if (selected == null || token == null) return;
        new Thread(() -> {
            ApiResponse<Void> response = authenticatorService.deleteAccount(selected.getId(), token);
            Platform.runLater(() -> {
                if (response.isSuccess()) {
                    loadAuthenticatorCodes();
                } else {
                    DialogUtil.showError("Authenticator", response.getErrorMessage());
                }
            });
        }).start();
    }

    @FXML private void handleRefreshAuthenticatorCodes(ActionEvent event) {
        loadAuthenticatorCodes();
    }

    @FXML private void handleRefreshNotifications(ActionEvent event) {
        loadNotifications();
    }

    @FXML private void handleMarkNotificationRead(ActionEvent event) {
        NotificationDto selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                notificationService.markAsRead(selected.getId(), token);
                Platform.runLater(() -> loadNotifications());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleMarkNotificationRead: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleMarkAllNotificationsRead(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                notificationService.markAllAsRead(token);
                Platform.runLater(() -> loadNotifications());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleMarkAllNotificationsRead: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleDeleteNotification(ActionEvent event) {
        NotificationDto selected = notificationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            try {
                notificationService.deleteNotification(selected.getId(), token);
                Platform.runLater(() -> loadNotifications());
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleDeleteNotification: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void handleDesignPresetChange(ActionEvent event) {
        String preset = designPresetCombo.getValue();
        if (preset != null) {
            AuthSystemApplication.setDesignPreset(preset);
            designStatusLabel.setText("Preset applied: " + preset);
        }
    }

    @FXML private void handleDarkModeToggle(ActionEvent event) {
        AuthSystemApplication.setDarkModeEnabled(darkModeCheck.isSelected());
    }

    @FXML private void handleLoadDesignTemplate(ActionEvent event) {
        String template = designTemplateCombo != null ? designTemplateCombo.getValue() : null;
        if (template == null) {
            return;
        }
        customCssArea.setText(DESIGN_TEMPLATES.getOrDefault(template, ""));
        designStatusLabel.setText("Starter CSS loaded: " + template);
    }

    @FXML private void handleApplyCustomDesign(ActionEvent event) {
        String css = customCssArea.getText();
        AuthSystemApplication.setCustomDesignCss(css);
        designStatusLabel.setText("Custom design applied.");
    }

    @FXML private void handleResetCustomDesign(ActionEvent event) {
        AuthSystemApplication.clearCustomDesignCss();
        customCssArea.clear();
        designStatusLabel.setText("Custom design cleared. Preset styling is active.");
    }

    @FXML private void handleNotificationToggle(ActionEvent event) {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        boolean emailNotif = emailNotificationsCheck.isSelected();
        boolean loginAlerts = loginAlertsCheck.isSelected();
        new Thread(() -> {
            try {
                userService.updateNotifications(emailNotif, loginAlerts, token);
            } catch (Exception e) {
                System.err.println("[Dashboard] Error in handleNotificationToggle: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
