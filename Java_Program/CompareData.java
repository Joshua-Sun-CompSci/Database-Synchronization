package week1;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class CompareData {
    public static void main(String[] args) {

        // initialize them just incase of invalid input
        String url = "";
        String username = "";
        String password = "";
        try (Scanner myObj = new Scanner(System.in)) {

            // loop twice to compare two data bases
            for (int i = 0; i < 2; i++){
                if (i == 1){
                    System.out.println("Please enter the following information to connect to the benchmark database:");
                } else{
                    System.out.println("Please enter the following information to connect to the target database:");
                }
                System.out.println("Enter url: ");
                url = myObj.nextLine();  // Read username input
    
                System.out.println("Enter your usernamename: ");
                username = myObj.nextLine();
    
                System.out.println("Enter your password: ");
                password = myObj.nextLine();
                
                // Save the first data to benchamarkDatabse.txt
                if (i == 0){
                    SaveData.saveData("benchmarkDatabase.txt", url, username, password);
                }
                System.out.println(); // prints \n for formatting
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // get the data from the target database
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // this lines reads the benchmarkDatabase file and turn it into a string
            String benchmarkDatabase = fileToString("benchmarkDatabase.txt");

            // split the String to different tables
            String[] benchmarkTables = benchmarkDatabase.split("\n[ \\t\\n]*\n");
            
            List<String> tableNames = SaveData.loadTableNames("tables.txt");

            // loop through each table
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = tableNames.get(i);
                System.out.println("Checking table: " + tableName);

                // get the content of each benchmark Tables
                String benchmarkString = benchmarkTables[i];

                // if table DNE in the benchmark database, delete the table from the target database
                if (benchmarkString.equals("Table DNE")){
                    System.out.println("Warning: Table name \"" + tableName + "\" does not exist in the benchmark database.");
                    ModifyTable.deleteTable(tableName, url, username, password);
                    System.out.println("Done checking table " + tableName + ".\n");
                    continue;
                }

                // for each table exists in benchmark DB, fetch PK and the content within each table
                String targetPK = FetchData.fetchPrimaryKey(connection, tableName);
                Map<String, String> targetTable = FetchData.fetchTableStructure(connection, tableName);

                String[] benchmarkStringArray = benchmarkString.split("\n");
                
                Map<String, String> benchmarkTable = parseFields(benchmarkStringArray);
                String benchmarkPK = benchmarkTable.get("Primary Key");

                // remove the primary key to have the same structure as the targetTable
                benchmarkTable.remove("Primary Key");

                // if everything is null(table DNE), create the table in the target DB
                if (targetPK == null && targetTable.isEmpty()){
                    System.out.println("Warning: Table name \"" + tableName + "\" does not exist in the target database.");
                    ModifyTable.createTable(tableName, benchmarkPK, benchmarkTable, url, username, password);
                    System.out.println("Done checking table " + tableName + ".\n");
                    continue; // skip the mismatch table
                }

                // compare PK
                comparePK(benchmarkPK, targetPK, tableName);
                compareData(benchmarkTable, targetTable, tableName);
                System.out.println("Done checking table " + tableName + ".\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareData(Map<String, String> benchmarkTable, Map<String, String> targetTable, String tableName){
        for (String key : benchmarkTable.keySet()) {

            // if both tables have the key
            if (targetTable.containsKey(key)){

                // if the key does not have the same value in two tables
                if (!targetTable.get(key).equals(benchmarkTable.get(key))){
                    System.out.println("Warning: Key \"" + key + "\" in the target databse does not match the type in the benchmark database in table \"" + tableName + "\".");
                }
            } else{
                System.out.println("Warning: Key \"" + key + "\" is missing in the target table \"" + tableName + "\".");
            }
        }
        for (String key : targetTable.keySet()) {
            if (!benchmarkTable.containsKey(key)){
                System.out.println("Warning: Key \"" + key + "\" is missing in the benchmark table \"" + tableName + "\".");
            }
        }
    }

    private static void comparePK(String PK1, String PK2, String tableName){
        if (PK1.equals("null") && PK2 == null){
            System.out.println("Warning: Primary Key does not exist in the benchmark or target dabase tables \"" + tableName + "\".");
        } else if (!PK1.equalsIgnoreCase(PK2)){
            System.out.println("Warning: Primary Key does not match between the benchmark and target dabase in table \"" + tableName + "\".");
        }
    }

    // read from a file and return a string
    private static String fileToString(String fileName){
        Path path = Paths.get("benchmarkDatabase.txt");
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Map<String, String> parseFields(String[] fields) {
        Map<String, String> fieldMap = new HashMap<>();
        for (String field : fields) {
            String[] parts = field.split(":");
            if (parts.length == 2) {
                fieldMap.put(parts[0].trim(), parts[1].trim());
            }
        }
        return fieldMap;
    }
}
