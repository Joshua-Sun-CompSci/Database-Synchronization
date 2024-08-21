import java.io.*;
import java.sql.*;
import java.util.*;

public class SaveData {
    public static void saveData(String saveAs, String url, String username, String password) {

        // clears the output file first
        clearFile(saveAs);

        // get all tables from tables.txt and put names into a list
        List<String> tableNames = loadTableNames("tables.txt");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // loop through all tables
            for (String tableName : tableNames) {
                Map<String, Map<String, Object>> table = FetchData.fetchTableStructure(connection, tableName);
                HashSet<String> PKs = FetchData.fetchPrimaryKeys(connection, tableName);
                saveTable(PKs, table, tableName, saveAs); // write table to saveAs
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

    // this write the content of the table to saveAs
    private static void saveTable(HashSet<String> PKs, Map<String, Map<String, Object>> table, String tableName, String saveAs) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveAs, true))) {
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

    // this reads the file and returns a list of names in the table
    public static List<String> loadTableNames(String fileName) {
        List<String> tableNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {

                // if the line doesn't start with "//", assume it is the table name
                // we do this because we want to allow comments on tables.txt
                if (!(line.charAt(0) == '/' && line.charAt(1) == '/')){
                    tableNames.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableNames;
    }
}
