package com.restaurant.controller;

import com.restaurant.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label lblWelcome;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, " + user.getFirstName() + " " + user.getLastName() + "!");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/restaurant/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root, 360, 320));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
