module com.example.clinicapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.clinicapp to javafx.fxml;
    opens com.example.clinicapp.server to javafx.fxml;

    exports com.example.clinicapp;
}