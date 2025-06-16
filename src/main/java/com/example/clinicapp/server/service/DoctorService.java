package com.example.clinicapp.server.service;

import com.example.clinicapp.server.database.DatabaseConnector;
import com.example.clinicapp.common.model.Doctor;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
                    return new Doctor.Builder()
                            .id(result.getInt("doctorId"))
                            .fullname(result.getString("fullname"))
                            .email(result.getString("email"))
                            .password(result.getString("password"))
                            .date(result.getDate("date"))
                            .build();
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

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctor";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(new Doctor.Builder()
                        .id(rs.getInt("doctorId"))
                        .fullname(rs.getString("fullname"))
                        .email(rs.getString("email"))
                        .password(rs.getString("password"))
                        .date(rs.getDate("date"))
                        .build()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Możesz też użyć logowania
        }

        return doctors;
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

    public int getDoctorIdByFullName(String fullname) {
        String sql = "SELECT doctorId FROM doctor WHERE fullname = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullname);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("doctorId");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // albo logger
        }

        return -1; // lub rzucić wyjątek jeśli doktor nie został znaleziony
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
        String sql = "DELETE FROM doctor WHERE doctorId = ?";

        try (Connection connect = DatabaseConnector.getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {

            prepare.setInt(1, doctorId);

            int affectedRows = prepare.executeUpdate();
            return affectedRows > 0;
        }
    }
}
