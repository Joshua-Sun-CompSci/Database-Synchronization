package week1;
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
                Map<String, String> table = FetchData.fetchTableStructure(connection, tableName);
                String PK = FetchData.fetchPrimaryKey(connection, tableName);
                saveTable(PK, table, saveAs); // write table to saveAs
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
    private static void saveTable(String PK, Map<String, String> table, String saveAs){

        // saves all <name, type> from a table to saveAs
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveAs, true))) {

            // if everything is null, the table DNE
            if (PK == null && table.isEmpty()){
                writer.write("Table DNE\n\n");
                return;
            }
            writer.write("Primary Key: " + PK + "\n");

            // for each pair values in the table, write it to saveAs
            for (Map.Entry<String, String> pair : table.entrySet()) {
                String name = pair.getKey();
                String type = pair.getValue();
                writer.write("\t" + name + ": " + type + "\n");
            }

            // Add a blank line between tables
            writer.write("\n");
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
