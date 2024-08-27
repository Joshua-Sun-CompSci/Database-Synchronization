package com.joshua.sun.english;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TableLoaderEn {

    // this reads the file and returns a list of names in the table
    public static List<String> loadTableNames(String fileName) {
        List<String> tableNames = new ArrayList<>();
        try (InputStream inputStream = TableLoaderEn.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            
            if (inputStream == null) {
                System.err.println("File \"" + fileName + "\" is empty.");
                System.exit(1); // exits the program with error
            }

            String line;
            while ((line = br.readLine()) != null) {

                // If the line doesn't start with "//", assume it is the table name
                if (!(line.startsWith("//"))) {
                    tableNames.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableNames;
    }
}
