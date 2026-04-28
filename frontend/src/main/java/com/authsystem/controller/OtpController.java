package com.authsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class OtpController {

    private String username;
    private String deliveryEmail;
    private boolean totpMode;
    private String initialMessage;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDeliveryEmail(String deliveryEmail) {
        this.deliveryEmail = deliveryEmail;
    }

    public void setTotpMode(boolean totpMode) {
        this.totpMode = totpMode;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    @FXML
    private void handleBack(ActionEvent event) {
    }

    @FXML
    private void handleVerify(ActionEvent event) {
    }

    @FXML
    private void handleResend(ActionEvent event) {
    }
}
