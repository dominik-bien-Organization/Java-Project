package com.example.clinicapp;

import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DoctorDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private ListView<String> notificationListView;
    @FXML private Label statusLabel;

    // To są pola, które chciałeś przenieść - ich miejsce jest TUTAJ
    private ClinicClient client;
    private UserData currentUser;

    private AlertMessage alert = new AlertMessage();
    private ObservableList<String> notifications = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notificationListView.setItems(notifications);
    }

    // Ta metoda jest wywoływana z DoctorPageController
    public void initData(UserData userData) {
        this.currentUser = userData;
        welcomeLabel.setText("Witaj, " + currentUser.getUsername() + "!");
        connectToServer(); // Łączymy się z serwerem DOPIERO gdy mamy dane lekarza
    }

    // Ta metoda łączy się z serwerem i zaczyna nasłuchiwać
    private void connectToServer() {
        try {
            // ZMIEŃ "localhost" na IP serwera, jeśli uruchamiasz na innym komputerze
            client = new ClinicClient("localhost", 12345, this::handleServerMessage);
            client.startListening();

            String loginPayload = "LEKARZ:" + currentUser.getId();
            client.sendMessage(new NetworkMessage(MessageType.LOGIN, loginPayload));
            statusLabel.setText("Status: Nasłuchiwanie na powiadomienia...");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Błąd połączenia z serwerem.");
            alert.errorMessage("Nie udało się połączyć z serwerem. Sprawdź czy serwer jest uruchomiony.");
        }
    }

    // Ta metoda obsługuje przychodzące wiadomości od serwera
    private void handleServerMessage(NetworkMessage message) {
        Platform.runLater(() -> {
            if (message.getType() == MessageType.APPOINTMENT_NOTIFICATION) {
                Appointment appointment = (Appointment) message.getPayload();
                String notificationText = "Nowa wizyta od pacjenta: " + appointment.getPatientUsername()
                        + " (" + appointment.getPatientId() + ") na dzień " + appointment.getDate()
                        + " o godz. " + appointment.getTime();

                notifications.add(0, notificationText);
                alert.successMessage("Nowe powiadomienie o wizycie!");
            }
        });
    }

    // Ta metoda zamyka połączenie
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