package com.joshua.sun;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CompareData {
    public static void main(String[] args) { 
        // String url = "jdbc:postgresql://192.168.1.4:5432/benchmark";
        // String username = "postgres";
        // String password = "20041116";
        // SaveData.saveData("benchmarkDatabase.txt", url, username, password);
        // url = "jdbc:postgresql://192.168.1.4:5432/target";
        // username = "postgres";
        // password = "20041116";
        
        // // initialize them just incase of invalid input
        String url = "";
        String username = "";
        String password = "";
        try (Scanner myObj = new Scanner(System.in)) {

            // loop twice to compare two data bases
            for (int i = 0; i < 2; i++){
                if (i == 0){
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
            
            List<String> tableNames = TableLoader.loadTableNames("tables.txt");

            // loop through each table
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = tableNames.get(i);
                System.out.println("Checking table: " + tableName);

                // get the content of each benchmark Tables
                String benchmarkString = benchmarkTables[i];

                // for each table exists in benchmark DB, fetch PK and the content within each table
                HashSet<String> targetPKs = FetchData.fetchPrimaryKeys(connection, tableName);
                Map<String, Map<String, Object>> targetTable = FetchData.fetchTableStructure(connection, tableName);
                
                // if table DNE in both benchmark and target database, print the message to warn the user
                if (benchmarkString.equals("Table DNE") && targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("Warning: Table \"" + tableName + "\" does not exist in either database.");
                    System.out.println("Done checking table " + tableName + ".\n");
                    continue;
                } else if (benchmarkString.equals("Table DNE")){ // else if table DNE in the benchmark database, delete the table from the target database
                    System.out.println("Warning: Table \"" + tableName + "\" does not exist in the benchmark database.");
                    ModifyTable.dropTable(tableName, url, username, password);
                    System.out.println("Done checking table " + tableName + ".\n");
                    continue;
                }
                
                String[] benchmarkArray = benchmarkString.split("\n");
                String benchmarkPKString = benchmarkArray[1];
                String[] benchmarkPKArray = benchmarkPKString.split("\\s+");

                // filters out "Primary Keys: " and only saves what's after it
                HashSet<String> benchmarkPKs = new HashSet<>();
                for (int j = 2; j < benchmarkPKArray.length; j++) {
                    String key = benchmarkPKArray[j].trim();
                    benchmarkPKs.add(key);
                }
                
                // this deletes the first&second element (the name of the table and PK)
                String[] benchmarkStringArray = Arrays.copyOfRange(benchmarkArray, 2, benchmarkArray.length);
                Map<String, Map<String, Object>> benchmarkTable = parseFields(benchmarkStringArray);

                // if table DNE in the target database, create the table in the target DB
                if (targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("Warning: Table \"" + tableName + "\" is missing in the target database.");
                    ModifyTable.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                    System.out.println("Done checking table " + tableName + ".\n");
                    continue; // skip the mismatch table
                }

                // compare PK
                comparePK(benchmarkPKs, targetPKs, tableName, url, username, password);
                compareData(benchmarkTable, targetTable, tableName, url, username, password);
                System.out.println("Done checking table " + tableName + ".\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareData(Map<String, Map<String, Object>> benchmarkTable, Map<String, Map<String, Object>> targetTable, String tableName, String url, String username, String password){
        for (String column : benchmarkTable.keySet()) {

        // gets all the information in case of modifying the column
        String typeName = benchmarkTable.get(column).get("TYPE_NAME").toString();
        String colSize = String.valueOf((Integer)benchmarkTable.get(column).get("COLUMN_SIZE"));
        String decDigits = String.valueOf((Integer)benchmarkTable.get(column).get("DECIMAL_DIGITS"));

            // if both tables have the column
            if (targetTable.containsKey(column)){

                // check if all detail information matches
                for (String detailKey: benchmarkTable.get(column).keySet()) {

                    // if targetTable also has the key (type name, column size, decimal digits)
                    if (targetTable.get(column).containsKey(detailKey)) {

                        // if value does not match
                        if (!benchmarkTable.get(column).get(detailKey).equals(targetTable.get(column).get(detailKey))) {
                            System.out.println("Warning: Column \"" + column + "\" in the target database mismatches the benchmark database in table \"" + tableName + "\".");
                            ModifyTable.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                        }
                    } else {
                        System.out.println("Warning: Column \"" + column + "\" in the target databse has formatting error in table  \"" + tableName + "\".");
                        ModifyTable.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                    }
                }
            } else{
                System.out.println("Warning: Column \"" + column + "\" is missing in the target table \"" + tableName + "\".");
                ModifyTable.addColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
            }
        }
        for (String column : targetTable.keySet()) {
            
            if (!benchmarkTable.containsKey(column)){
                System.out.println("Warning: Column \"" + column + "\" does not exist in the benchmark table \"" + tableName + "\".");
                ModifyTable.dropColumn(tableName, column, url, username, password);
            }
        }
    }

    private static void comparePK(HashSet<String> benchmarkPKs, HashSet<String> targetPKs, String tableName, String url, String username, String password) {
        if (!(benchmarkPKs.size() == targetPKs.size() && benchmarkPKs.containsAll(targetPKs))) { // PKs do not match
            System.out.println("Warning: Primary Key does not match between the two databases in table \"" + tableName + "\".");
            if (benchmarkPKs.isEmpty()) { // if benchmarkPKs DNE
                ModifyTable.dropPKs(tableName, url, username, password);
            } else if (targetPKs.isEmpty()) { // if targetPKs DNE
                ModifyTable.addPKs(tableName, benchmarkPKs, url, username, password);
            } else{ // if PKs do not match
                ModifyTable.modifyPKs(tableName, benchmarkPKs, url, username, password);
            }
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
    
    private static Map<String, Map<String, Object>> parseFields(String[] fields) {
        final String eMsg = "Warning: benchmark database formatting error, unable to parse.";
        Map<String, Map<String, Object>> fieldMap = new HashMap<>();
        for (String field : fields) {
            String[] parts = field.split(",");

            //i.e. Column Name: UPDATETIME, it only takes "UPDATETIME"
            String colName = parts[0].split(":")[1].trim();
            if (parts.length == 4) {
                Map<String, Object> detailMap = new HashMap<>();
                for (int i = 1; i < 4; i++) {
                    String[] detailParts = parts[i].split(":");
                    if (detailParts.length == 2){
                        try {
                            int number = Integer.parseInt(detailParts[1].trim());
                            detailMap.put(detailParts[0].trim(), number);
                        } catch (NumberFormatException e) {
                            detailMap.put(detailParts[0].trim(), detailParts[1].trim());
                        }
                    } else{
                        System.out.println(eMsg);
                    }
                }
            fieldMap.put(colName, detailMap);
            } else{
                System.out.println(eMsg);
            }
        }
        return fieldMap;
    }
}
