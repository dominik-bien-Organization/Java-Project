package com.example.clinicapp.interfaces;

import java.sql.Date;

public interface IUser {
    int getId();
    String getEmail();
    String getPassword();
    Date getDate();

    void setId(int id);
    void setEmail(String email);
    void setPassword(String password);
    void setDate(Date date);

    boolean isValid();
}
