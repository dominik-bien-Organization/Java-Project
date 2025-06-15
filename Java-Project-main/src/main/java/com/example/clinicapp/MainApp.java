package com.example.clinicapp;

import com.example.clinicapp.database.DatabaseInitializer;
//import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.service.AppointmentService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;




public class MainApp extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initializeDatabase();



        Parent root = FXMLLoader.load(getClass().getResource("/com/example/clinicapp/LoginChoice.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Wybór użytkownika");
        stage.show();
        stage.centerOnScreen();
    }


    public static void main(String[] args) {

        launch(args);
    }
}