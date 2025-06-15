package com.example.clinicapp.controller;

import com.example.clinicapp.model.Patient;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.client.service.PatientService;
import com.example.clinicapp.util.AlertMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class PatientPageController implements Initializable {

    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_username;
    @FXML private TextField login_showPassword;

    @FXML private AnchorPane register_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_username;

    private AlertMessage alert = new AlertMessage();
    private PatientService patientService;
    private ClinicClient clinicClient;

    public void setClinicClient(ClinicClient clinicClient) {
        this.clinicClient = clinicClient;
    }

    public void loginAccount() {
        String username = login_username.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie pola");
            return;
        }

        if (!Patient.isValidPassword(password)) {
            alert.errorMessage("Niepoprawne hasło");
            return;
        }

        if (clinicClient == null || !clinicClient.isConnected()) {
            alert.errorMessage("Brak połączenia z serwerem.");
            return;
        }

        // Use Platform.runLater to avoid blocking the UI thread
        Platform.runLater(() -> {
            try {
                Optional<Patient> patientOpt = patientService.login(username, password);

                if (patientOpt.isPresent()) {
                    alert.successMessage("Logowanie wykonano pomyślnie!");
                    openClinicSystem(patientOpt.get()); // otwarcie GUI po logowaniu
                } else {
                    alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
                }
            } catch (Exception e) {
                e.printStackTrace();
                alert.errorMessage("Błąd podczas logowania: " + e.getMessage());
            }
        });
    }

    public void registerAccount() {
        String email = register_email.getText();
        String username = register_username.getText();
        String password = register_showPassword.isVisible() ? register_showPassword.getText() : register_password.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie wymagane pola");
            return;
        }

        if (!Patient.isValidEmail(email)) {
            alert.errorMessage("Nieprawidłowy format emaila");
            return;
        }

        if (!Patient.isValidPassword(password)) {
            alert.errorMessage("Hasło musi mieć przynajmniej 8 znaków");
            return;
        }

        try {
            if (!patientService.register(email, username, password)) {
                alert.errorMessage("Użytkownik " + username + " już istnieje w bazie danych.");
                return;
            }
            alert.successMessage("Rejestracja wykonana pomyślnie!");
            registerClear();
            login_form.setVisible(true);
            register_form.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Błąd rejestracji: " + e.getMessage());
        }
    }

    private void onServerMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            System.out.println("Otrzymano wiadomość: " + msg.getType() + " - " + msg.getPayload());

            switch (msg.getType()) {
                case LOGIN:
                    alert.successMessage("Zalogowano pomyślnie!");
                    break;
                case BOOKING_CONFIRMED:
                    alert.successMessage("Twoja wizyta została pomyślnie zarezerwowana!");
                    break;

                case BOOKING_CANCELLED:
                    alert.errorMessage("Rezerwacja wizyty nie powiodła się.");
                    break;

                default:
                    alert.errorMessage("Nieznany typ wiadomości.");
            }
        });
    }

    private void openClinicSystem(Patient loggedPatient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/PatientDashboard.fxml"));
            Parent root = loader.load();


            // Pobierz kontroler i ustaw zalogowanego pacjenta
            PatientDashboardController controller = loader.getController();

            // Set the client first (initializes services and loads doctors list)
            controller.setClient(clinicClient);

            // Then set the current user (loads recipes)
            controller.setCurrentUser(loggedPatient);

            // This is deprecated and redundant since we're using setClient
            // controller.setOutputStream(clinicClient.getOut());

            Stage stage = (Stage) login_button.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("System Kliniki zdrowia");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Błąd ładowania systemu: " + e.getMessage());
        }
    }

    public void loginShowPassword() {
        if (login_checkbox.isSelected()) {
            login_showPassword.setText(login_password.getText());
            login_showPassword.setVisible(true);
            login_password.setVisible(false);
        } else {
            login_password.setText(login_showPassword.getText());
            login_showPassword.setVisible(false);
            login_password.setVisible(true);
        }
    }

    public void registerShowPassword() {
        if (register_checkbox.isSelected()) {
            register_showPassword.setText(register_password.getText());
            register_showPassword.setVisible(true);
            register_password.setVisible(false);
        } else {
            register_password.setText(register_showPassword.getText());
            register_showPassword.setVisible(false);
            register_password.setVisible(true);
        }
    }

    public void registerClear() {
        register_email.clear();
        register_username.clear();
        register_password.clear();
        register_showPassword.clear();
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == login_registerHere) {
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if (event.getSource() == register_loginHere) {
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/clinicapp/LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        stage.setScene(new Scene(root));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            clinicClient = new ClinicClient("localhost", 12345, this::onServerMessage);
            // No need to call startListening() as it will be called automatically by sendMessageAndWaitForResponse()

            // Initialize the client-side service with the clinic client
            patientService = new PatientService(clinicClient);
        } catch (IOException e) {
            alert.errorMessage("Błąd połączenia z serwerem: " + e.getMessage() + 
                              "\nAplikacja wymaga połączenia z serwerem do działania.");
            clinicClient = null;

            // Disable login and register buttons
            login_button.setDisable(true);
            register_button.setDisable(true);
        }
    }
}
