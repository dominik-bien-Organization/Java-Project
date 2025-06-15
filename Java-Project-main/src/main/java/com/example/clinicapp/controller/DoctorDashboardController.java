package com.example.clinicapp.controller;

import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.model.Patient;
import com.example.clinicapp.network.*;
import com.example.clinicapp.service.DoctorService;
import com.example.clinicapp.service.PatientService;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class DoctorDashboardController implements Initializable {

    @FXML private Label labelDoctorname;
    @FXML private BarChart<String, Number> barChartPatients;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button logoutButton;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colHour;

    @FXML private ComboBox<Patient> comboPatients;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePickerIssueDate;

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Doctor doctor;
    private ClinicClient clinicClient;

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        Platform.runLater(() -> labelDoctorname.setText(doctor.getFullname()));
        sendGetAppointmentsIfReady();
    }

    public void setClinicClient(ClinicClient clinicClient) {
        this.clinicClient = clinicClient;
        clinicClient.setMessageListener(this::handleNetworkMessage);
        sendGetAppointmentsIfReady();
    }

    private void sendGetAppointmentsIfReady() {
        if (doctor != null && clinicClient != null) {
            Callable<Boolean> task = () -> {
                try {
                    clinicClient.sendMessage(new NetworkMessage(MessageType.GET_APPOINTMENTS_FOR_DOCTOR, doctor.getId()));
                    return true;
                } catch (IOException e) {
                    Platform.runLater(() -> new AlertMessage().errorMessage("Błąd połączenia z serwerem."));
                    return false;
                }
            };

            Future<Boolean> future = executor.submit(task);

            executor.submit(() -> {
                try {
                    Boolean success = future.get(3, TimeUnit.SECONDS);
                    System.out.println("Wysłano zapytanie o wizyty: " + success);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> new AlertMessage().errorMessage("Nie udało się pobrać wizyt."));
                }
            });
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (doctor != null && clinicClient != null) {
                clinicClient.sendMessage(new NetworkMessage(MessageType.LOGOUT, doctor.getEmail()));
                Thread.sleep(200);
                clinicClient.close();
            }

            executor.shutdownNow(); // zakończenie executor

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/DoctorPage.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Logowanie - Lekarz");
            stage.centerOnScreen();
            stage.show();

            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveRecipe() {
        Patient selectedPatient = comboPatients.getSelectionModel().getSelectedItem();
        String description = txtDescription.getText();
        LocalDate issueDate = datePickerIssueDate.getValue();

        if (selectedPatient == null || description == null || description.isEmpty() || issueDate == null) {
            new AlertMessage().errorMessage("Wypełnij wszystkie pola!");
            return;
        }

        Recipe recipe = new Recipe();
        recipe.setPatientId(selectedPatient.getId());
        recipe.setPatientName(selectedPatient.getUsername());
        recipe.setDescription(description);
        recipe.setIssueDate(issueDate);
        recipe.setDoctorId(doctor.getId());

        try {
            clinicClient.sendMessage(new NetworkMessage(MessageType.SAVE_RECIPE, recipe));
        } catch (IOException e) {
            e.printStackTrace();
            new AlertMessage().errorMessage("Błąd wysłania recepty do serwera");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Map<String, Integer> patientsPerDay = doctorService.getPatientsLast7Days();

        xAxis.setLabel("Data");
        yAxis.setLabel("Ilość");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pacjenci na ostatnie 7 dni");

        for (Map.Entry<String, Integer> entry : patientsPerDay.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChartPatients.getData().clear();
        barChartPatients.getData().add(series);

        colPatient.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        colHour.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toString()));

        appointmentTable.setItems(appointmentList);
        loadPatients();
    }

    private void loadPatients() {
        List<Patient> patientsList = patientService.getAllPatients();
        ObservableList<Patient> observablePatients = FXCollections.observableArrayList(patientsList);
        comboPatients.setItems(observablePatients);
    }

    public void handleNetworkMessage(NetworkMessage message) {
        switch (message.getType()) {
            case RECIPE_SAVED:
                Platform.runLater(() -> new AlertMessage().successMessage("Recepta została zapisana."));
                break;
            case RECIPE_SAVE_FAILED:
                Platform.runLater(() -> new AlertMessage().errorMessage("Nie udało się zapisać recepty."));
                break;
            case APPOINTMENT_LIST:
                List<Appointment> list = (List<Appointment>) message.getPayload();
                Platform.runLater(() -> {
                    appointmentList.clear();
                    appointmentList.addAll(list);
                });
                break;
            default:
                Platform.runLater(() -> new AlertMessage().errorMessage(
                        "Nieznany typ wiadomości: " + message.getType()
                ));
        }
    }
}
