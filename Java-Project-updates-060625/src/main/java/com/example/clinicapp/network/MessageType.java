// package com.example.clinicapp.network;
package com.example.clinicapp.network;

public enum MessageType {
    LOGIN,
    BOOK_APPOINTMENT, // Klient -> Serwer
    APPOINTMENT_NOTIFICATION, // Serwer -> Lekarz
    BOOKING_CONFIRMED, // Serwer -> Pacjent
    ERROR
}