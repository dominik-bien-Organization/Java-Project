package com.example.clinicapp;

import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class PatientDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TextField doctorIdField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeComboBox;
    @FXML private Button bookButton;
    @FXML private Label statusLabel;

    private ClinicClient client;
    private UserData currentUser; // Przechowuje dane zalogowanego pacjenta
    private AlertMessage alert = new AlertMessage();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicjalizuj listę dostępnych godzin
        timeComboBox.setItems(FXCollections.observableArrayList(
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0),
                LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30)
        ));
    }

    // Ta metoda jest wywoływana z kontrolera logowania
    public void initData(UserData userData) {
        this.currentUser = userData;
        welcomeLabel.setText("Witaj, " + currentUser.getUsername() + "!");
        connectToServer();
    }

    private void connectToServer() {
        try {
            // ZMIEŃ "localhost" na IP serwera, jeśli uruchamiasz na innym komputerze
            client = new ClinicClient("localhost", 12345, this::handleServerMessage);
            client.startListening();

            // Zaloguj się na serwerze, wysyłając obiekt
            String loginPayload = "PACJENT:" + currentUser.getId();
            client.sendMessage(new NetworkMessage(MessageType.LOGIN, loginPayload));
            statusLabel.setText("Status: Połączono z serwerem");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Błąd połączenia z serwerem.");
            alert.errorMessage("Nie udało się połączyć z serwerem. Sprawdź czy serwer jest uruchomiony.");
        }
    }

    @FXML
    private void handleBookAppointment() {
        try {
            int doctorId = Integer.parseInt(doctorIdField.getText());
            LocalDate date = datePicker.getValue();
            LocalTime time = timeComboBox.getValue();

            if (date == null || time == null) {
                alert.errorMessage("Proszę wybrać datę i godzinę wizyty.");
                return;
            }

            // Tworzymy obiekt wizyty
            Appointment appointment = new Appointment(doctorId, currentUser.getId(), currentUser.getUsername(), date, time);

            // Wysyłamy wiadomość-obiekt do serwera
            client.sendMessage(new NetworkMessage(MessageType.BOOK_APPOINTMENT, appointment));

        } catch (NumberFormatException e) {
            alert.errorMessage("ID lekarza musi być liczbą.");
        }
    }

    // Metoda, która obsługuje wiadomości-obiekty od serwera
    private void handleServerMessage(NetworkMessage message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case BOOKING_CONFIRMED:
                    String confirmationText = (String) message.getPayload();
                    alert.successMessage(confirmationText);
                    statusLabel.setText("Status: " + confirmationText);
                    break;
                case ERROR:
                    String errorText = (String) message.getPayload();
                    alert.errorMessage(errorText);
                    statusLabel.setText("Błąd: " + errorText);
                    break;
                default:
                    System.out.println("Odebrano nieobsługiwany typ wiadomości: " + message.getType());
            }
        });
    }

    // Metoda do zamknięcia połączenia, wywoływana przy zamykaniu okna
    public void closeClientConnection() {
        try {
            if (client != null) {
                client.close();
                System.out.println("Połączenie klienta zostało zamknięte.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}