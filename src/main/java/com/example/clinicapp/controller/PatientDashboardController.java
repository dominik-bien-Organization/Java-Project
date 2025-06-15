package com.example.clinicapp.controller;

import com.example.clinicapp.files.FileHelper;
import com.example.clinicapp.interfaces.IPatient;
import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.network.*;
import com.example.clinicapp.client.service.DoctorService;
import com.example.clinicapp.client.service.RecipeService;
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
import javafx.stage.Stage;

import java.io.IOException;
// Removed ObjectOutputStream import as we're using ClinicClient directly
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class PatientDashboardController implements Initializable {

    @FXML private ComboBox<String> doctorComboBox;
    @FXML private ComboBox<String> hourComboBox;
    @FXML private DatePicker visitDatePicker;
    @FXML private Button logoutButton;

    @FXML private TableView<Recipe> RecipeTableView;
    @FXML private TableColumn<Recipe, String> doctorColumn;
    @FXML private TableColumn<Recipe, String> descriptionColumn;
    @FXML private Button buttonDownloadRecipes;
    @FXML private TableColumn<Recipe, String> issueDateColumn;

    private RecipeService recipeService;
    private DoctorService doctorService;

    private IPatient currentUser;
    private ClinicClient client;
    private String patientName;
    private volatile boolean running = true;
    private Thread reloaderThread;

    public void setCurrentUser(IPatient currentUser) {
        this.currentUser = currentUser;
        this.patientName = currentUser.getUsername();
        loadRecipesForPatient();
    }

    /**
     * @deprecated Use setClient(ClinicClient) instead
     */
    @Deprecated
    public void setOutputStream(Object out) {
        System.out.println("setOutputStream is deprecated, use setClient(ClinicClient) instead");
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAvailableHours();
        initializeRecipeTable();
        buttonDownloadRecipes.setOnAction(e -> handleDownloadRecipes());

        reloaderThread = new Thread(this::reloadRecipies);
        reloaderThread.setDaemon(true);
        reloaderThread.start();
    }

    public void reloadRecipies() {
        while (running) {
            try {
                Thread.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException e) {
                // Thread was interrupted, exit the loop
                break;
            }

            if (running) {
                loadRecipesForPatient();
            }
        }
        System.out.println("Recipe reloader thread stopped");
    }

    private void loadAvailableHours() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 8; i <= 16; i++) {
            hours.add(String.format("%02d:00", i));
        }
        hourComboBox.setItems(hours);
    }

    public void loadRecipesForPatient() {
        if (currentUser == null || recipeService == null) return;

        List<Recipe> recipes = recipeService.getRecipesByPatientId(currentUser.getId());
        ObservableList<Recipe> observableRecipes = FXCollections.observableArrayList(recipes);

        RecipeTableView.setItems(observableRecipes);
    }

    @FXML
    private void handleLogout() {
        try {
            // Stop the recipe reloader thread
            running = false;
            if (reloaderThread != null) {
                reloaderThread.interrupt();
            }

            if (currentUser != null && client != null) {
                // Wyślij wiadomość LOGOUT (załóżmy, że pacjent ma metodę getUsername())
                NetworkMessage logoutMsg = new NetworkMessage(MessageType.LOGOUT, currentUser.getUsername());

                // No need to wait for a response when logging out
                client.sendMessageAndWaitForResponse(logoutMsg, MessageType.LOGOUT, 1000);

                // Close the client connection
                client.close();
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

        if (selectedDoctorName != null && selectedHour != null && selectedDate != null) {
            int doctorId = extractDoctorIdFromName(selectedDoctorName);

            if (doctorId == -1) {
                new AlertMessage().errorMessage("Nie udało się odczytać ID lekarza.");
                return;
            }

            int patientId = currentUser.getId();
            String patientUsername = currentUser.getUsername();

            LocalTime time = LocalTime.parse(selectedHour);

            System.out.println("Tworzenie wizyty: doctorId=" + doctorId +
                    ", patientId=" + patientId +
                    ", date=" + selectedDate +
                    ", time=" + time);

            Appointment appointment = new Appointment(doctorId, patientId, patientUsername, selectedDate, time);
            NetworkMessage message = new NetworkMessage(MessageType.BOOK_APPOINTMENT, appointment);

            try {
                System.out.println("Wysyłanie wiadomości BOOK_APPOINTMENT do serwera");

                NetworkMessage response = client.sendMessageAndWaitForResponse(
                        message,
                        new MessageType[]{MessageType.BOOKING_CONFIRMED, MessageType.BOOKING_CANCELLED},
                        5000); // 5 seconds timeout

                if (response != null) {
                    handleServerResponse(response);
                    System.out.println("Wiadomość wysłana pomyślnie i otrzymano odpowiedź");
                } else {
                    System.out.println("Nie otrzymano odpowiedzi od serwera w wyznaczonym czasie");
                    new AlertMessage().errorMessage("Nie otrzymano odpowiedzi od serwera w wyznaczonym czasie.");
                }

            } catch (IOException e) {
                System.err.println("Błąd podczas wysyłania wiadomości:");
                e.printStackTrace();
                new AlertMessage().errorMessage("Błąd podczas wysyłania wiadomości: " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("Operacja została przerwana:");
                e.printStackTrace();
                new AlertMessage().errorMessage("Operacja została przerwana: " + e.getMessage());
            }

        } else {
            System.out.println("Uzupełnij wszystkie pola");
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
        if (doctorService != null) {
            List<Doctor> doctors = doctorService.getAllDoctors();
            ObservableList<String> doctorNames = FXCollections.observableArrayList();

            for (Doctor doc : doctors) {
                doctorNames.add(doc.getFullname() + " (id: " + doc.getId() + ")");
            }

            doctorComboBox.setItems(doctorNames);
        } else {
            Platform.runLater(() -> new AlertMessage().errorMessage("Nie można załadować listy lekarzy - brak połączenia z serwerem."));
        }
    }
    public void setClient(ClinicClient client) {
        this.client = client;
        client.setMessageListener(this::handleServerResponse);

        // Initialize the client-side services with the clinic client
        this.doctorService = new DoctorService(client);
        this.recipeService = new RecipeService(client);

        // Load doctors list now that we have a client connection
        loadDoctorsToComboBox();

        // Reload recipes if we have a current user
        if (currentUser != null) {
            loadRecipesForPatient();
        }
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
