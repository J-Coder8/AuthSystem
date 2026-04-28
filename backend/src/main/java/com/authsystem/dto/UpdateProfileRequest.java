package com.authsystem.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String bio;
    private String address;
    private String city;
    private String country;
    private Boolean emailNotifications;
    private Boolean loginAlerts;
    private Boolean darkMode;
}


