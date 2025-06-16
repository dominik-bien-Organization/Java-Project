package com.example.clinicapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/clinicapp/client/LoginChoice.fxml"));

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