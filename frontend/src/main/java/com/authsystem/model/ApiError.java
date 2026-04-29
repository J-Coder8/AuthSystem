package com.authsystem.model;

public class ApiError {
    private String message;
    private int statusCode;

    public ApiError() {}

    public ApiError(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() { return message != null ? message : "Unknown error"; }
    public void setMessage(String message) { this.message = message; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
}
