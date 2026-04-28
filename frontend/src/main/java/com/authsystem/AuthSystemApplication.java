package com.authsystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.authsystem.controller.DashboardController;
import com.authsystem.controller.OtpController;

import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthSystemApplication extends Application {

    private static Stage primaryStage;
    private static String currentUsername;
    private static String faceMode;
    private static final String PREF_DARK_MODE = "darkMode";
    private static final String PREF_DESIGN_PRESET = "designPreset";
    private static final String DEFAULT_DESIGN_PRESET = "design-warm-sand";
    private static final List<String> DESIGN_PRESETS = List.of(
            "design-warm-sand",
            "design-ocean-breeze",
            "design-emerald-glow",
            "design-ink-slate",
            "design-sunset-pulse",
            "design-nordic-frost",
            "design-aurora-pop",
            "design-rose-quartz",
            "design-citrus-glow",
            "design-cherry-noir",
            "design-lavender-mist",
            "design-golden-hour",
            "design-arctic-neon",
            "design-forest-ember",
            "design-coral-reef",
            "design-monochrome-luxe",
            "design-royal-velvet",
            "design-peach-bloom",
            "design-cyber-lime",
            "design-terracotta-ink",
            "design-skyline",
            "design-obsidian-rose",
            "design-mint-paper",
            "design-solar-flare",
            "design-cocoa-mint",
            "design-sapphire-sand",
            "design-crimson-tide"
    );
    private static final Path CUSTOM_DESIGN_PATH = Paths.get(
            System.getProperty("user.home"),
            ".authsystem",
            "custom-design.css"
    );
    private static boolean darkModeEnabled = false;
    private static String designPreset = DEFAULT_DESIGN_PRESET;
    private static String customDesignCss = "";
    private static Preferences prefs;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        prefs = Preferences.userNodeForPackage(AuthSystemApplication.class);
        darkModeEnabled = prefs.getBoolean(PREF_DARK_MODE, false);
        designPreset = normalizeDesignPreset(prefs.get(PREF_DESIGN_PRESET, DEFAULT_DESIGN_PRESET));
        customDesignCss = loadCustomDesignCss();
        showLoginView();
    }

    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/LoginView.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 600);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/RegisterView.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 700);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Register");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showOtpView(String username) {
        showOtpView(username, false);
    }

    public static void showOtpView(String username, boolean isTotpMode) {
        showOtpView(username, null, isTotpMode, null);
    }

    public static void showOtpView(String username, String deliveryEmail, boolean isTotpMode) {
        showOtpView(username, deliveryEmail, isTotpMode, null);
    }

    public static void showOtpView(String username, String deliveryEmail, boolean isTotpMode, String initialMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/OtpView.fxml")
            );
            Parent root = loader.load();
            OtpController controller = loader.getController();
            controller.setUsername(username);
            controller.setDeliveryEmail(deliveryEmail);
            controller.setTotpMode(isTotpMode);
            controller.setInitialMessage(initialMessage);
            Scene scene = new Scene(root, 500, 500);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - OTP Verification");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

public static void showDashboardView(String username, String email, String fullName) {
        showDashboardView(username, email, fullName, "USER");
    }

    public static void showDashboardView(String username, String email, String fullName, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/DashboardView.fxml")
            );
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUserInfo(username, email, fullName, role);
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showForgotPasswordView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/ForgotPasswordView.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 550);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Forgot Password");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showAdminPanelView(String username, String email, String fullName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/AdminPanelView.fxml")
            );
            Parent root = loader.load();
            com.authsystem.controller.AdminPanelController controller = loader.getController();
            controller.setUserInfo(username, email, fullName);
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Admin Panel");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showFaceView(String mode) {
        try {
            faceMode = mode;
            FXMLLoader loader = new FXMLLoader(
                AuthSystemApplication.class.getResource("/fxml/FaceView.fxml")
            );
            Parent root = loader.load();
            com.authsystem.controller.FaceController controller = loader.getController();
            controller.setMode(mode);
            Scene scene = new Scene(root, 500, 700);
            scene.getStylesheets().add(
                AuthSystemApplication.class.getResource("/css/styles.css").toExternalForm()
            );
            applyTheme(scene);
            primaryStage.setTitle("Authentication System - Face Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static String getFaceMode() {
        return faceMode;
    }

    public static boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public static void setDarkModeEnabled(boolean enabled) {
        darkModeEnabled = enabled;
        if (prefs != null) {
            prefs.putBoolean(PREF_DARK_MODE, enabled);
        }
        refreshActiveScene();
    }

    public static List<String> getDesignPresets() {
        return DESIGN_PRESETS;
    }

    public static String getDesignPreset() {
        return designPreset;
    }

    public static void setDesignPreset(String preset) {
        designPreset = normalizeDesignPreset(preset);
        if (prefs != null) {
            prefs.put(PREF_DESIGN_PRESET, designPreset);
        }
        refreshActiveScene();
    }

    public static String getCustomDesignCss() {
        return customDesignCss != null ? customDesignCss : "";
    }

    public static boolean hasCustomDesignCss() {
        return customDesignCss != null && !customDesignCss.isBlank();
    }

    public static void setCustomDesignCss(String css) {
        customDesignCss = css != null ? css.strip() : "";
        persistCustomDesignCss(customDesignCss);
        refreshActiveScene();
    }

    public static void clearCustomDesignCss() {
        customDesignCss = "";
        persistCustomDesignCss(customDesignCss);
        refreshActiveScene();
    }

    private static void applyTheme(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        scene.getRoot().getStyleClass().removeAll(DESIGN_PRESETS);
        scene.getRoot().getStyleClass().add(designPreset);
        if (darkModeEnabled) {
            if (!scene.getRoot().getStyleClass().contains("dark-mode")) {
                scene.getRoot().getStyleClass().add("dark-mode");
            }
        } else {
            scene.getRoot().getStyleClass().remove("dark-mode");
        }
        applyCustomDesign(scene);
    }

    private static void refreshActiveScene() {
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    private static String normalizeDesignPreset(String preset) {
        if (preset == null || !DESIGN_PRESETS.contains(preset)) {
            return DEFAULT_DESIGN_PRESET;
        }
        return preset;
    }

    private static String loadCustomDesignCss() {
        if (!Files.exists(CUSTOM_DESIGN_PATH)) {
            return "";
        }
        try {
            return Files.readString(CUSTOM_DESIGN_PATH, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void persistCustomDesignCss(String css) {
        try {
            Files.createDirectories(CUSTOM_DESIGN_PATH.getParent());
            if (css == null || css.isBlank()) {
                Files.deleteIfExists(CUSTOM_DESIGN_PATH);
            } else {
                Files.writeString(CUSTOM_DESIGN_PATH, css, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyCustomDesign(Scene scene) {
        String baseUri = CUSTOM_DESIGN_PATH.toUri().toString();
        scene.getStylesheets().removeIf(uri -> uri != null && uri.startsWith(baseUri));
        if (customDesignCss == null || customDesignCss.isBlank()) {
            return;
        }
        try {
            if (!Files.exists(CUSTOM_DESIGN_PATH)) {
                persistCustomDesignCss(customDesignCss);
            }
            long version = Files.getLastModifiedTime(CUSTOM_DESIGN_PATH).toMillis();
            scene.getStylesheets().add(baseUri + "?v=" + version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
