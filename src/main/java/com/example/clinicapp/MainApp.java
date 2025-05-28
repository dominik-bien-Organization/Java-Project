package com.example.clinicapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/com/example/clinicapp/FXMLDocument.fxml"));

        Database.initializeDatabase();

        Scene scene = new Scene(root);
        stage.setMinHeight(550);
        stage.setMinWidth(330);

        stage.setTitle("System Kliniki");


        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}