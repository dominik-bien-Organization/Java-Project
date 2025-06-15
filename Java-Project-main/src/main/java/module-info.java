module com.example.clinicapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.clinicapp to javafx.fxml;
    exports com.example.clinicapp;
    exports com.example.clinicapp.controller;
    opens com.example.clinicapp.controller to javafx.fxml;
    exports com.example.clinicapp.model;
    opens com.example.clinicapp.model to javafx.fxml;
    exports com.example.clinicapp.util;
    opens com.example.clinicapp.util to javafx.fxml;

    exports com.example.clinicapp.server;
    opens com.example.clinicapp.server to javafx.fxml;

}