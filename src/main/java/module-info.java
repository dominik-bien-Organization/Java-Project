module com.example.clinicapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    exports com.example.clinicapp.common.model;
    opens com.example.clinicapp.common.model to javafx.fxml;
    exports com.example.clinicapp.client.util;
    opens com.example.clinicapp.client.util to javafx.fxml;

    exports com.example.clinicapp.server;
    opens com.example.clinicapp.server to javafx.fxml;
	exports com.example.clinicapp.client.controller;
	opens com.example.clinicapp.client.controller to javafx.fxml;
    exports com.example.clinicapp.server.controller;
    opens com.example.clinicapp.server.controller to javafx.fxml;
    exports com.example.clinicapp.client;
    opens com.example.clinicapp.client to javafx.fxml;
    exports com.example.clinicapp.server.handler;
    opens com.example.clinicapp.server.handler to javafx.fxml;

}