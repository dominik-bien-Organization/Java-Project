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

            }

            System.out.println("Database and tables have been initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
