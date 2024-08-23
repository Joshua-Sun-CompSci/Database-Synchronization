package com.joshua.sun;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SaveData {
    public static void saveData(String fileName, String url, String username, String password) {

        // clears the output file first
        clearFile(fileName);

        // get all tables from tables.txt and put names into a list
        List<String> tableNames = TableLoader.loadTableNames("tables.txt");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // loop through all tables
            for (String tableName : tableNames) {
                Map<String, Map<String, Object>> table = FetchData.fetchTableStructure(connection, tableName);
                HashSet<String> PKs = FetchData.fetchPrimaryKeys(connection, tableName);
                saveTable(PKs, table, tableName, fileName); // write table to fileName
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // this clears the file
    private static void clearFile(String fileName) {
        // The file is cleared by opening in write mode without append
        // Writing an empty string effectively clears the file content
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // this write the content of the table to fileName
    private static void saveTable(HashSet<String> PKs, Map<String, Map<String, Object>> table, String tableName, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            if (PKs.isEmpty() && table.isEmpty()) {
                writer.write("Table DNE\n\n");
                return;
            } else {
                writer.write("Table Name: " + tableName + "\n");
            }

            // starts to write all PKs to file
            writer.write("Primary Key(s): ");
            for (String PK: PKs){
                writer.write(PK + " ");
            }
            writer.write("\n"); // done writing all PKs
            
            // starts to write details to file
            for (Map.Entry<String, Map<String, Object>> columnEntry : table.entrySet()) {
                String name = columnEntry.getKey();
                Map<String, Object> details = columnEntry.getValue();
                writer.write("COLUMN_NAME: " + name + ", ");
                writer.write("TYPE_NAME: " + details.get("TYPE_NAME") + ", ");
                writer.write("COLUMN_SIZE: " + details.get("COLUMN_SIZE") + ", ");
                writer.write("DECIMAL_DIGITS: " + details.get("DECIMAL_DIGITS") + "\n");
            }
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
