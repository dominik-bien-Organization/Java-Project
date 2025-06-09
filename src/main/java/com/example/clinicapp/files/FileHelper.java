package com.example.clinicapp.files;


import com.example.clinicapp.network.Recipe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileHelper {

    public static void saveRecipesToFile(String patientName, List<Recipe> recipes) throws IOException {
        File folder = new File("files");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Znajdź najwyższy numer pliku receptaX.txt
        int maxIndex = 0;
        File[] files = folder.listFiles((dir, name) -> name.matches("recepta\\d+\\.txt"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName(); // np. "recepta3.txt"
                String numberStr = name.replaceAll("[^0-9]", ""); // "3"
                try {
                    int number = Integer.parseInt(numberStr);
                    if (number > maxIndex) maxIndex = number;
                } catch (NumberFormatException ignored) {}
            }
        }

        int newIndex = maxIndex + 1;
        String fileName = "recepta" + newIndex + ".txt";
        File file = new File(folder, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Recepty pacjenta: " + patientName);
            writer.newLine();
            writer.write("Data pobrania: " + java.time.LocalDateTime.now());
            writer.newLine();
            writer.write("====================================");
            writer.newLine();

            for (Recipe r : recipes) {
                writer.write("Lekarz: " + r.getDoctorName());
                writer.newLine();
                writer.write("Opis: " + r.getDescription());
                writer.newLine();
                writer.write("Data wystawienia: " + r.getIssueDate());
                writer.newLine();
                writer.write("------------------------------------");
                writer.newLine();
            }
        }
    }
}