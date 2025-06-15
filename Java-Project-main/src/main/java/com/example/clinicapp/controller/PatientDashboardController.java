package com.example.clinicapp.controller;

import com.example.clinicapp.files.FileHelper;
import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.network.*;
import com.example.clinicapp.service.DoctorService;

import com.example.clinicapp.service.RecipeService;
import com.example.clinicapp.util.AlertMessage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

import com.example.clinicapp.interfaces.IPatient;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

public class PatientDashboardController implements Initializable {

    @FXML
    private ComboBox<String> doctorComboBox;

    @FXML private ComboBox<String> hourComboBox;
    @FXML private DatePicker visitDatePicker;


    @FXML
    private Button logoutButton;

    @FXML
    private TableView<Recipe> RecipeTableView;

    @FXML
    private TableColumn<Recipe, String> doctorColumn;

    @FXML
    private TableColumn<Recipe, String> descriptionColumn;

    @FXML
    private Button buttonDownloadRecipes;

    @FXML
    private TableColumn<Recipe, String> issueDateColumn;

    private RecipeService recipeService = new RecipeService();

    private final DoctorService doctorService = new DoctorService();

    private IPatient currentUser;
    private ObjectOutputStream out;

    private String patientName;


    public void setCurrentUser(IPatient currentUser) {
        this.currentUser = currentUser;
        this.patientName = currentUser.getUsername();
        loadRecipesForPatient();
    }

    public void setOutputStream(ObjectOutputStream out) {
        this.out = out;
        System.out.println("setOutputStream called, out=" + out);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDoctorsToComboBox();
        loadAvailableHours();
        initializeRecipeTable();
        buttonDownloadRecipes.setOnAction(e -> handleDownloadRecipes());
    }

    private void loadAvailableHours() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 8; i <= 16; i++) {
            hours.add(String.format("%02d:00", i));
        }
        hourComboBox.setItems(hours);
    }

    public void loadRecipesForPatient() {
        if (currentUser == null) return;

        List<Recipe> recipes = recipeService.getRecipesByPatientId(currentUser.getId());
        ObservableList<Recipe> observableRecipes = FXCollections.observableArrayList(recipes);

        RecipeTableView.setItems(observableRecipes);
    }

    @FXML
    private void handleLogout() {
        try {
            if (currentUser != null && out != null) {
                // Wyślij wiadomość LOGOUT (załóżmy, że pacjent ma metodę getUsername())
                NetworkMessage logoutMsg = new NetworkMessage(MessageType.LOGOUT, currentUser.getUsername());
                out.writeObject(logoutMsg);
                out.flush();

                // Odczekaj krótko (jeśli potrzeba, np. 200 ms)
                Thread.sleep(200);

                // Tu można zamknąć połączenie, jeśli masz referencję do klienta sieciowego (ClinicClient)
                // np. clinicClient.close(); (w zależności od implementacji)
            }

            // Załaduj scenę logowania pacjenta (zamień na właściwą ścieżkę FXML)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/PatientPage.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Logowanie - Pacjent");
            stage.centerOnScreen();
            stage.show();

            // Zamknij obecne okno
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initializeRecipeTable() {
        doctorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDoctorName()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        issueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));
    }



    @FXML
    private void handleConfirmVisit() {
        System.out.println("handleConfirmVisit called");

        String selectedDoctorName = doctorComboBox.getValue();
        String selectedHour = hourComboBox.getValue();
        LocalDate selectedDate = visitDatePicker.getValue();

        if (selectedDoctorName == null || selectedHour == null || selectedDate == null) {
            // Zmieniamy komunikat na bardziej precyzyjny
            new AlertMessage().errorMessage("Proszę wybrać lekarza, datę oraz godzinę wizyty.");
            return;
        }

        // === POCZĄTEK NOWEGO FRAGMENTU WALIDACYJNEGO ===

        LocalTime selectedTime = LocalTime.parse(selectedHour);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Sprawdzenie, czy wybrany dzień jest dniem z przeszłości
        if (selectedDate.isBefore(today)) {
            new AlertMessage().errorMessage("Nie można rezerwować wizyt na daty, które już minęły.");
            return; // Zakończ działanie metody
        }

        // Sprawdzenie, czy wybrany dzień to dzisiaj, a godzina już minęła
        if (selectedDate.isEqual(today) && selectedTime.isBefore(now)) {
            new AlertMessage().errorMessage("Nie można rezerwować wizyt na godzinę, która już minęła w dniu dzisiejszym.");
            return; // Zakończ działanie metody
        }

        // === KONIEC NOWEGO FRAGMENTU WALIDACYJNEGO ===


        // Reszta metody pozostaje bez zmian
        int doctorId = extractDoctorIdFromName(selectedDoctorName);

        if (doctorId == -1) {
            new AlertMessage().errorMessage("Nie udało się odczytać ID lekarza.");
            return;
        }

        int patientId = currentUser.getId();
        String patientUsername = currentUser.getUsername();

        // LocalTime time = LocalTime.parse(selectedHour); // To już mamy wyżej

        System.out.println("Tworzenie wizyty: doctorId=" + doctorId +
                ", patientId=" + patientId +
                ", date=" + selectedDate +
                ", time=" + selectedTime);

        Appointment appointment = new Appointment(doctorId, patientId, patientUsername, selectedDate, selectedTime);
        NetworkMessage message = new NetworkMessage(MessageType.BOOK_APPOINTMENT, appointment);

        try {
            System.out.println("Wysyłanie wiadomości BOOK_APPOINTMENT do serwera");

            out.writeObject(message);
            out.flush();
            System.out.println("Wiadomość wysłana pomyślnie");

        } catch (IOException e) {
            System.err.println("Błąd podczas wysyłania wiadomości:");
            e.printStackTrace();
        }
    }

    private int extractDoctorIdFromName(String doctorName) {
        try {
            int start = doctorName.indexOf("(id: ") + 5;
            int end = doctorName.indexOf(")", start);
            String idString = doctorName.substring(start, end);
            return Integer.parseInt(idString);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void loadDoctorsToComboBox() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        ObservableList<String> doctorNames = FXCollections.observableArrayList();

        for (Doctor doc : doctors) {
            doctorNames.add(doc.getFullname() + " (id: " + doc.getId() + ")");
        }

        doctorComboBox.setItems(doctorNames);
    }
    public void setClient(ClinicClient client) {
        client.setMessageListener(this::handleServerResponse);
    }

    private void handleServerResponse(NetworkMessage message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case BOOKING_CONFIRMED:
                    new AlertMessage().successMessage("Wizyta została pomyślnie zapisana.");
                    break;
                case  BOOKING_CANCELLED:
                    new AlertMessage().errorMessage("Nie udało się zapisać wizyty. Spróbuj ponownie.");
                    break;
            }
        });
    }


    @FXML
    private void handleDownloadRecipes() {
        try {
            List<Recipe> recipes = RecipeTableView.getItems();
            if (recipes == null || recipes.isEmpty()) {
                System.out.println("Brak recept do pobrania.");
                return;
            }
            if (currentUser == null) {
                System.out.println("Brak aktualnie zalogowanego użytkownika.");
                return;
            }

            String username = currentUser.getUsername(); // pobieramy username z bazy (lub z zalogowanego użytkownika)
            FileHelper.saveRecipesToFile(patientName, recipes);
            System.out.println("Recepty zostały zapisane do pliku.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    }

