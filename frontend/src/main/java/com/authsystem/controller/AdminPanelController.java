package com.authsystem.controller;

import java.util.List;

import com.authsystem.AuthSystemApplication;
import com.authsystem.model.AdminUserDto;
import com.authsystem.service.AdminService;
import com.authsystem.service.ApiService.ApiResponse;
import com.authsystem.util.JwtTokenUtil;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminPanelController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private TextField searchField;
    @FXML private TableView<AdminUserDto> usersTable;
    @FXML private Button backButton;

    private final AdminService adminService = new AdminService();

    public void setUserInfo(String username, String email, String fullName) {
        // Admin info can be displayed if needed
    }

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            ApiResponse<List<AdminUserDto>> response = adminService.getAllUsers(token);
            Platform.runLater(() -> {
                if (response.isSuccess() && response.getData() != null) {
                    usersTable.getItems().setAll(response.getData());
                    totalUsersLabel.setText(String.valueOf(response.getData().size()));
                    long active = response.getData().stream().filter(AdminUserDto::isEnabled).count();
                    activeUsersLabel.setText(String.valueOf(active));
                }
            });
        }).start();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AuthSystemApplication.showDashboardView(
            JwtTokenUtil.getCurrentUsername(),
            null, null, "ADMIN"
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        JwtTokenUtil.clearToken();
        AuthSystemApplication.showLoginView();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadUsers();
            return;
        }
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            ApiResponse<List<AdminUserDto>> response = adminService.getAllUsers(token);
            Platform.runLater(() -> {
                if (response.isSuccess() && response.getData() != null) {
                    List<AdminUserDto> filtered = response.getData().stream()
                        .filter(u -> u.getUsername().toLowerCase().contains(query)
                            || u.getEmail().toLowerCase().contains(query)
                            || (u.getFullName() != null && u.getFullName().toLowerCase().contains(query)))
                        .toList();
                    usersTable.getItems().setAll(filtered);
                }
            });
        }).start();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUsers();
    }

    @FXML
    private void handleEnableUser(ActionEvent event) {
        AdminUserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            adminService.enableUser(selected.getId(), token);
            Platform.runLater(() -> loadUsers());
        }).start();
    }

    @FXML
    private void handleDisableUser(ActionEvent event) {
        AdminUserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            adminService.disableUser(selected.getId(), token);
            Platform.runLater(() -> loadUsers());
        }).start();
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        AdminUserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            adminService.deleteUser(selected.getId(), token);
            Platform.runLater(() -> loadUsers());
        }).start();
    }

    @FXML
    private void handleMakeAdmin(ActionEvent event) {
        AdminUserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            adminService.updateUserRole(selected.getId(), "ADMIN", token);
            Platform.runLater(() -> loadUsers());
        }).start();
    }

    @FXML
    private void handleRemoveAdmin(ActionEvent event) {
        AdminUserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String token = JwtTokenUtil.getToken();
        if (token == null) return;
        new Thread(() -> {
            adminService.updateUserRole(selected.getId(), "USER", token);
            Platform.runLater(() -> loadUsers());
        }).start();
    }
}
