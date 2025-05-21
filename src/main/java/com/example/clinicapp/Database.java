package com.example.clinicapp;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    public static Connection connectDb() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connect = DriverManager.getConnection
                    ("jdbc:mysql://localhost:3306/clinic", "root", ""); //default root connection
            return connect;
        } catch (Exception e) {
            {e.printStackTrace();}

        }
        return null;
    }
}
