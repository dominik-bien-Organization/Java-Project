package com.example.clinicapp.service;

import com.example.clinicapp.model.Patient;
import com.example.clinicapp.database.DatabaseConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientService {

    public Optional<Patient> login(String username, String password) {
        String sql = "SELECT * FROM patient WHERE username = ? AND password = ?";
        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, username);
            prepare.setString(2, password);

            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) {
                    Patient patient = new Patient(
                            result.getInt("id"),
                            result.getString("email"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getDate("date")
                    );
                    return Optional.of(patient);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT id, username, email, password, date FROM patient";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getDate("date")
                );
                patients.add(patient);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return patients;
    }

    public boolean register(String email, String username, String password) throws Exception {
        if (isUsernameTaken(username)) {
            return false;
        }

        String insertData = "INSERT INTO patient (email, username, password, date) VALUES (?, ?, ?, ?)";
        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(insertData)) {

            Date sqlDate = new Date(System.currentTimeMillis());

            prepare.setString(1, email);
            prepare.setString(2, username);
            prepare.setString(3, password);
            prepare.setDate(4, sqlDate);

            prepare.executeUpdate();
            return true;
        }
    }

    private boolean isUsernameTaken(String username) throws Exception {
        String checkUsername = "SELECT * FROM patient WHERE username = ?";
        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(checkUsername)) {
            prepare.setString(1, username);

            try (ResultSet result = prepare.executeQuery()) {
                return result.next();
            }
        }
    }
}
