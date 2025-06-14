package com.example.clinicapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginChoiceController {
    public static final String[] USER_TYPES = {"Lekarz", "Pacjent"};

    @FXML
    private ComboBox<String> userComboBox;

    @FXML
    public void initialize() {
        userComboBox.getItems().clear();
        userComboBox.getItems().addAll(USER_TYPES);
    }

    @FXML
    void handleUserSelection(ActionEvent event) throws IOException {
        String selectedUser = userComboBox.getValue();

        String fxmlToLoad = switch (selectedUser) {
            case "Lekarz" -> "/com/example/clinicapp/DoctorPage.fxml";
            case "Pacjent" -> "/com/example/clinicapp/PatientPage.fxml";
            default -> null;
        };

        if (fxmlToLoad != null) {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlToLoad));
            Stage stage = (Stage) userComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        }
    }
}