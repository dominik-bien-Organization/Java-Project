package com.example.clinicapp.controller;

import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.service.DoctorService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DoctorDashboardController implements Initializable {

    @FXML private Label labelDoctorname;
    @FXML private BarChart<String, Number> barChartPatients;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Button logoutButton;

    private Doctor doctor;
    private final DoctorService doctorService = new DoctorService();

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        Platform.runLater(() -> labelDoctorname.setText(doctor.getFullname()));
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/DoctorPage.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Logowanie - Lekarz");
            stage.centerOnScreen();
            stage.show();

            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
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
    }
}
