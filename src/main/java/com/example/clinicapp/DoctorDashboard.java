package com.example.clinicapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;




public class DoctorDashboard implements Initializable {

    @FXML
    private Label labelDoctorname;

    private String doctorname;

    @FXML
    private BarChart<String, Number> barChartPatients;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private javafx.scene.control.Button logoutButton;


    @FXML
    private void handleLogout() {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("DoctorPage.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(scene);
            stage.setTitle("Logowanie - Lekarz");
            stage.show();
            stage.centerOnScreen();

            // Zamknij bieżące okno (dashboard)
            javafx.stage.Stage currentStage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    public void setDoctor(String doctor) {
        this.doctorname = doctor;

        Platform.runLater(() -> {
            if (labelDoctorname != null) {
                labelDoctorname.setText(doctorname);
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Map<String, Integer> patientsPerDay = getPatient();

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

    public Map<String, Integer> getPatient() {
        Map<String, Integer> patientsPerDay = new LinkedHashMap<>();

        String url = "jdbc:mysql://localhost:3306/clinic";
        String user = "root";
        String password = "";

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            patientsPerDay.put(date.format(formatter), 0);
        }


        String sql = "SELECT d.date, " +
                "(SELECT COUNT(*) FROM patient p2 WHERE p2.date <= d.date) AS cumulative_count " +
                "FROM (SELECT DISTINCT date FROM patient WHERE date >= CURDATE() - INTERVAL 6 DAY) d " +
                "ORDER BY d.date";


        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("cumulative_count");
                patientsPerDay.put(date, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patientsPerDay;
    }

}
