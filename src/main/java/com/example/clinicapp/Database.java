package com.example.clinicapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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

    public static void initializeDatabase() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS clinic");
            stmt.close();
            conn.close();


            Connection connect = connectDb();
            Statement tableStmt = connect.createStatement();
            tableStmt.executeUpdate("CREATE TABLE IF NOT EXISTS admin ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "email VARCHAR(255) NOT NULL,"
                    + "username VARCHAR(255) NOT NULL UNIQUE,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "date DATE NOT NULL"
                    + ")");
            tableStmt.close();
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
