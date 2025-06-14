package com.example.clinicapp.service;

import com.example.clinicapp.database.DatabaseConnector;
import com.example.clinicapp.network.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    /**
     * Obiekt 'lock' używany do synchronizacji dostępu do operacji zapisu wizyt.
     * Jest to obiekt statyczny, co oznacza, że jest on współdzielony przez wszystkie
     * instancje ClientHandler na serwerze, gwarantując, że tylko jeden wątek
     * na raz może wykonywać operację w bloku synchronized.
     */
    private static final Object APPOINTMENT_LOCK = new Object();


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

    /**
     * Metoda sprawdzająca, czy dany termin wizyty jest już zajęty.
     */
    private boolean isAppointmentSlotTaken(int doctorId, LocalDate date, LocalTime time) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointment WHERE doctor_id = ? AND date = ? AND time = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setTime(3, java.sql.Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }


    /**
     * Zapisuje nową wizytę w sposób bezpieczny wątkowo.
     * Używa bloku synchronized, aby zapobiec warunkom wyścigu, gdy dwóch
     * użytkowników próbuje zarezerwować ten sam termin w tym samym czasie.
     */
    public boolean saveAppointment(Appointment appointment) {
        // --- POCZĄTEK SEKCJI KRYTYCZNEJ ---
        // Blok synchronized gwarantuje, że tylko jeden wątek na raz może
        // wykonywać ten fragment kodu. Zapobiega to sytuacji, w której
        // dwa wątki jednocześnie sprawdzą, że termin jest wolny, a następnie
        // oba go zarezerwują.
        synchronized (APPOINTMENT_LOCK) {
            System.out.println("Wątek " + Thread.currentThread().getName() + " wszedł do sekcji krytycznej.");
            try {
                // Krok 1: Sprawdź, czy termin jest już zajęty
                if (isAppointmentSlotTaken(appointment.getDoctorId(), appointment.getDate(), appointment.getTime())) {
                    System.out.println("Termin jest już zajęty, zapis anulowany: " + appointment);
                    return false; // Zwróć false, jeśli termin jest zajęty
                }

                // Krok 2: Jeśli termin jest wolny, dokonaj zapisu
                System.out.println("Termin wolny. Próba zapisania wizyty: " + appointment);
                String sql = "INSERT INTO appointment (patient_id, doctor_id, date, time) VALUES (?, ?, ?, ?)";
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    conn.setAutoCommit(true);

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
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                System.out.println("Wątek " + Thread.currentThread().getName() + " opuścił sekcję krytyczną.");
            }
        }
        // --- KONIEC SEKCJI KRYTYCZNEJ ---
    }
}
