package com.example.clinicapp.model;

import com.example.clinicapp.interfaces.IDoctor;

public class Doctor extends User implements IDoctor {
    private String fullname;

    public Doctor(int id, String fullname, String email, String password, java.sql.Date date) {
        super(id, email, password, date);
        this.fullname = fullname;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && fullname != null && !fullname.isBlank();
    }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
}
