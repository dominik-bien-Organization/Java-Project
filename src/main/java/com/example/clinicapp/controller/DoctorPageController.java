package com.example.clinicapp.controller;

import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.client.service.DoctorService;
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
import java.util.ResourceBundle;

public class DoctorPageController implements Initializable {

    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_email;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_fullName;

    private AlertMessage alert = new AlertMessage();
    private DoctorService doctorService;
    private ClinicClient clinicClient;

    public void loginAccount() {
        String email = login_email.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę wypełnić wszystkie pola");
            return;
        }

        if (!Doctor.isValidEmail(email)) {
            alert.errorMessage("Nieprawidłowy format emaila");
            return;
        }

        if (!Doctor.isValidPassword(password)) {
            alert.errorMessage("Niepoprawne hasło");
            return;
        }

        if (clinicClient == null || !clinicClient.isConnected()) {
            alert.errorMessage("Brak połączenia z serwerem.");
            return;
        }

        try {
            Doctor doctor = doctorService.login(email, password);
            if (doctor != null) {
                alert.successMessage("Logowanie wykonano pomyślnie!");
                openDoctorDashboard(doctor, clinicClient);
            } else {
                alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
            }
        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Nie udało się wysłać wiadomości do serwera: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            alert.errorMessage("Operacja została przerwana: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Błąd podczas logowania: " + e.getMessage());
        }
    }

    private void openDoctorDashboard(Doctor doctor, ClinicClient clinicClient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/DoctorDashboard.fxml"));
            Parent root = loader.load();

            DoctorDashboardController controller = loader.getController();
            controller.setDoctor(doctor);
            controller.setClinicClient(clinicClient);

            Stage stage = (Stage) login_button.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("System kliniki-Panel lekarza");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Błąd ładowania panelu lekarza: " + e.getMessage());
        }
    }

    public void setClinicClient(ClinicClient clinicClient) {
        this.clinicClient = clinicClient;
    }

    public void registerAccount() {
        String fullname = register_fullName.getText();
        String email = register_email.getText();
        String password = register_showPassword.isVisible() ? register_showPassword.getText() : register_password.getText();

        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie wymagane pola");
            return;
        }

        if (fullname.isBlank()) {
            alert.errorMessage("Imię i nazwisko nie może być puste");
            return;
        }

        if (!Doctor.isValidEmail(email)) {
            alert.errorMessage("Nieprawidłowy format emaila");
            return;
        }

        if (!Doctor.isValidPassword(password)) {
            alert.errorMessage("Hasło musi mieć przynajmniej 8 znaków");
            return;
        }

        try {
            if (doctorService.isEmailExists(email)) {
                alert.errorMessage("Lekarz z takim emailem już istnieje w bazie danych.");
                return;
            }

            Doctor newDoctor = new Doctor.Builder().id(0).fullname(fullname).email(email).password(password).build();
            doctorService.register(newDoctor);

            alert.successMessage("Rejestracja wykonana pomyślnie!");
            registerClear();

            login_form.setVisible(true);
            register_form.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Błąd rejestracji: " + e.getMessage());
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
        if(register_checkbox.isSelected()) {
            register_showPassword.setText(register_password.getText());
            register_showPassword.setVisible(true);
            register_password.setVisible(false);
        }
        else {
            register_password.setText(register_showPassword.getText());
            register_showPassword.setVisible(false);
            register_password.setVisible(true);
        }
    }

    public void registerClear() {
        register_email.clear();
        register_fullName.clear();
        register_password.clear();
        register_showPassword.clear();
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if(event.getSource() == login_registerHere) {
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
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

    private void onServerMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            System.out.println("Otrzymano wiadomość: " + msg.getType() + " - " + msg.getPayload());

            switch (msg.getType()) {
                case LOGIN:
                    alert.successMessage("Zalogowano pomyślnie!");
                    break;

                default:
                    alert.errorMessage("Nieznany typ wiadomości.");
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            clinicClient = new ClinicClient("localhost", 12345, this::onServerMessage);
            // No need to call startListening() as it will be called automatically by sendMessageAndWaitForResponse()

            // Initialize the client-side service with the clinic client
            doctorService = new DoctorService(clinicClient);
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
