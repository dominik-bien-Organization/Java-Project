module com.example.clinicapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clinicapp to javafx.fxml;
    exports com.example.clinicapp;
}