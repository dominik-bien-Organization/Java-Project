package com.example.clinicapp.controller;

import com.example.clinicapp.model.Patient;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.service.PatientService;
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
    private PatientService patientService = new PatientService();
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

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<Optional<Patient>> loginTask = () -> patientService.login(username, password);

        Future<Optional<Patient>> future = executor.submit(loginTask);

        // Uruchamiamy wątek obsługujący asynchroniczne logowanie
        new Thread(() -> {
            try {
                Optional<Patient> patientOpt = future.get(); // blokuje, ale to w osobnym wątku

                Platform.runLater(() -> {
                    if (patientOpt.isPresent()) {
                        alert.successMessage("Logowanie wykonano pomyślnie!");

                        if (clinicClient != null && clinicClient.isConnected()) {
                            try {
                                clinicClient.sendMessage(new NetworkMessage(MessageType.LOGIN,
                                        username + ":" + password + ":"));
                            } catch (IOException e) {
                                alert.errorMessage("Nie udało się wysłać wiadomości do serwera: " + e.getMessage());
                            }
                        } else {
                            alert.errorMessage("Brak połączenia z serwerem.");
                        }

                        openClinicSystem(patientOpt.get()); // otwarcie GUI po logowaniu
                    } else {
                        alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
                    }
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Platform.runLater(() -> alert.errorMessage("Błąd podczas logowania: " + e.getMessage()));
            } finally {
                executor.shutdown();
            }
        }).start();
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


            controller.setClient(clinicClient);

            controller.setCurrentUser(loggedPatient);
            controller.setOutputStream(clinicClient.getOut()); // jeśli masz dostęp do tego strumienia
            controller.setClient(clinicClient);

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
            clinicClient.startListening();
        } catch (IOException e) {
            alert.errorMessage("Błąd połączenia z serwerem: " + e.getMessage());
            clinicClient = null;
        }
    }
}
