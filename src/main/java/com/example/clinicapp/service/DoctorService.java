package com.example.clinicapp.service;

import com.example.clinicapp.database.DatabaseConnector;
import com.example.clinicapp.model.Doctor;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Date;

public class DoctorService {

    public Doctor login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM doctor WHERE email = ? AND password = ?";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, email);
            prepare.setString(2, password);

            try (ResultSet result = prepare.executeQuery()) {
                if (result.next()) {
                    return new Doctor(
                            result.getInt("id"),
                            result.getString("fullname"),
                            result.getString("email"),
                            result.getString("password"),
                            result.getDate("date")
                    );
                }
            }
        }
        return null;
    }

    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM doctor WHERE email = ?";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, email);

            try (ResultSet result = prepare.executeQuery()) {
                return result.next();
            }
        }
    }

    public void register(Doctor doctor) throws SQLException {
        String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(insertData)) {

            prepare.setString(1, doctor.getFullname());
            prepare.setString(2, doctor.getEmail());
            prepare.setString(3, doctor.getPassword());
            prepare.setDate(4, new java.sql.Date(new Date().getTime()));

            prepare.executeUpdate();
        }
    }

    public Map<String, Integer> getPatientsLast7Days() {
        Map<String, Integer> patientsPerDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            patientsPerDay.put(date.format(formatter), 0);
        }

        String sql = """
            SELECT d.date, 
                   (SELECT COUNT(*) FROM patient p2 WHERE p2.date <= d.date) AS cumulative_count 
            FROM (SELECT DISTINCT date FROM patient WHERE date >= CURDATE() - INTERVAL 6 DAY) d 
            ORDER BY d.date
            """;

        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("cumulative_count");
                patientsPerDay.put(date, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Można też rzucić wyjątek lub logować
        }

        return patientsPerDay;
    }

    public boolean updatePassword(int doctorId, String newPassword) throws SQLException {
        String sql = "UPDATE doctor SET password = ? WHERE id = ?";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setString(1, newPassword);
            prepare.setInt(2, doctorId);

            int affectedRows = prepare.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteDoctor(int doctorId) throws SQLException {
        String sql = "DELETE FROM doctor WHERE id = ?";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setInt(1, doctorId);

            int affectedRows = prepare.executeUpdate();
            return affectedRows > 0;
        }
    }
}
