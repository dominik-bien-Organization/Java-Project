package com.example.clinicapp.server;

import com.example.clinicapp.server.controller.ServerController;
import com.example.clinicapp.server.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initializeDatabase();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/clinicapp/server/server-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 600.0F, 400.0F);
        stage.setTitle("Panel Serwera Kliniki");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((_) -> {
            ServerController controller = fxmlLoader.getController();
            controller.shutdownServer();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}