package com.example.clinicapp.service;

import com.example.clinicapp.database.DatabaseConnector;
import com.example.clinicapp.network.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {


    public List<Appointment> getAppointmentsForDoctor(int doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.date, a.time, p.username AS patient_name, a.patient_id, a.doctor_id " +
                "FROM appointment a " +
                "JOIN patient p ON a.patient_id = p.id " +
                "WHERE a.doctor_id = ? " +
                "ORDER BY a.date, a.time";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setDate(rs.getDate("date").toLocalDate());
                    appt.setTime(rs.getTime("time").toLocalTime());
                    appt.setPatientName(rs.getString("patient_name"));
                    appt.setPatientId(rs.getInt("patient_id"));
                    appt.setDoctorId(rs.getInt("doctor_id"));

                    appointments.add(appt);
                }
            }
        }
        return appointments;
    }


    public boolean saveAppointment(Appointment appointment) {
        System.out.println("Próba zapisania wizyty: " + appointment);
        String sql = "INSERT INTO appointment (patient_id, doctor_id, date, time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(true); // wymusz auto-commit

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, java.sql.Date.valueOf(appointment.getDate()));
            ps.setTime(4, java.sql.Time.valueOf(appointment.getTime()));

            int rowsAffected = ps.executeUpdate();
            System.out.println("Zapisano wizytę, zmieniono wierszy: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}