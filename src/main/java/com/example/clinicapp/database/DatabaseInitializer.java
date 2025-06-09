package com.example.clinicapp.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS clinic");
            }

            try (Connection connect = DatabaseConnector.getConnection();
                 Statement tableStmt = connect.createStatement()) {

                tableStmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS doctor (
                        doctorID INT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(255) NOT NULL,
                        fullname VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        date DATE NOT NULL
                    )
                """);

                tableStmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS patient (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(255) NOT NULL,
                        username VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        date DATE NOT NULL
                    )
                """);
                tableStmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS appointment (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT NOT NULL,
                        doctor_id INT NOT NULL,
                        date DATE NOT NULL,
                        time TIME NOT NULL,
                        FOREIGN KEY (patient_id) REFERENCES patient(id),
                        FOREIGN KEY (doctor_id) REFERENCES doctor(doctorID)
                    )
                """);

                tableStmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS recipe (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT NOT NULL,
                    doctor_id INT NOT NULL,
                    description TEXT NOT NULL,
                    issue_date DATE NOT NULL,
                    FOREIGN KEY (patient_id) REFERENCES patient(id),
                    FOREIGN KEY (doctor_id) REFERENCES doctor(doctorID)
    )
""");
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
