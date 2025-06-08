// package com.example.clinicapp.server;
package com.example.clinicapp.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerMainApp.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Panel Serwera Kliniki");
        stage.setScene(scene);
        stage.show();

        // Upewnij się, że serwer zostanie zamknięty razem z oknem
        stage.setOnCloseRequest(event -> {
            ServerController controller = fxmlLoader.getController();
            controller.shutdownServer();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}