package com.restaurant.controller;

import com.restaurant.dao.UserDAO;
import com.restaurant.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please fill in all fields.");
            return;
        }

        User user = userDAO.authenticateUser(email, password);
        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/restaurant/view/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController controller = loader.getController();
                controller.initData(user);

                Stage stage = (Stage) txtEmail.getScene().getWindow();
                stage.setScene(new Scene(root, 850, 720));
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
                lblStatus.setText("Error loading main dashboard.");
            }
        } else {
            lblStatus.setText("Invalid email or password.");
        }
    }
}