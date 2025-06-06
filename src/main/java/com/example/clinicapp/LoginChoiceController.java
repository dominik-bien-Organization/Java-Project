package com.example.clinicapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginChoiceController {

    @FXML
    private ComboBox<String> userComboBox;

    @FXML
    public void initialize() {
        userComboBox.getItems().clear();
        userComboBox.getItems().addAll(Users.user);
    }

    @FXML
    void handleUserSelection(ActionEvent event) throws IOException {
        String selectedUser = userComboBox.getValue();

        String fxmlToLoad = switch (selectedUser) {
            case "Lekarz" -> "DoctorPage.fxml";
            case "Pacjent" -> "PatientPage.fxml";
            default -> null;
        };

        if (fxmlToLoad != null) {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlToLoad));
            Stage stage = (Stage) userComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }
}