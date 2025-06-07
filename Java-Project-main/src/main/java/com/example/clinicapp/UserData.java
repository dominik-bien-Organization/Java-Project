// package com.example.clinicapp;
package com.example.clinicapp;

// Ta klasa nie musi być Serializable, jeśli nie będziemy jej przesyłać w całości przez sieć
public class UserData {
    private final int id;
    private final String username;
    private final String userType; // "Pacjent", "Lekarz", "Admin"

    public UserData(int id, String username, String userType) {
        this.id = id;
        this.username = username;
        this.userType = userType;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getUserType() { return userType; }
}