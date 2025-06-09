package com.example.clinicapp.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMainApp extends Application {
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("server-view.fxml"));

        Scene scene = new Scene((Parent)fxmlLoader.load(), (double)600.0F, (double)400.0F);
        stage.setTitle("Panel Serwera Kliniki");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((event) -> {
            ServerController controller = (ServerController)fxmlLoader.getController();
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