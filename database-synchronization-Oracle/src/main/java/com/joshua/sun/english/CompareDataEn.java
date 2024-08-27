package com.joshua.sun.english;

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

public class CompareDataEn {
    public static void compareData(String url, String username, String password) { 
        Scanner scanner = new Scanner(System.in);
        
        // get the data from the target database
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // this lines reads the benchmarkDatabase file and turn it into a string
            String benchmarkDatabase = fileToString("benchmarkDatabase.txt");

            // split the String to different tables
            String[] benchmarkTables = benchmarkDatabase.split("\n[ \\t\\n]*\n");
            
            List<String> tableNames = TableLoaderEn.loadTableNames("tables.txt");

            // loop through each table
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = tableNames.get(i);
                System.out.println("Checking table: " + tableName);

                // get the content of each benchmark Tables
                String benchmarkString = benchmarkTables[i];

                // for each table exists in benchmark DB, fetch PK and the content within each table
                HashSet<String> targetPKs = FetchDataEn.fetchPrimaryKeys(connection, tableName);
                Map<String, Map<String, Object>> targetTable = FetchDataEn.fetchTableStructure(connection, tableName);
                
                // if table DNE in both benchmark and target database, print the message to warn the user
                if (benchmarkString.equals("Table DNE") && targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("Warning: Table \"" + tableName + "\" does not exist in either database.");
                    System.out.println("Done checking table \"" + tableName + "\".\n");
                    continue;
                } else if (benchmarkString.equals("Table DNE")){ // else if table DNE in the benchmark database, delete the table from the target database
                    System.out.println("Warning: Table \"" + tableName + "\" does not exist in the benchmark database.");

                    System.out.println("Do you want to drop this table in the target database? Y for yes and N for no.");
                    String action = scanner.nextLine();
                    if (action.equals("Y") || action.equals("y")) {ModifyTableEn.dropTable(tableName, url, username, password);}
                    else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}

                    System.out.println("Done checking table \"" + tableName + "\".\n");
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

                    System.out.println("Do you want to create this table in the target database? Y for yes and N for no.");
                    String action = scanner.nextLine();
                    if (action.equals("Y") || action.equals("y")) {ModifyTableEn.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);}
                    else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}

                    System.out.println("Done checking table \"" + tableName + "\".\n");
                    continue; // skip the mismatch table
                }

                // compare PK
                comparePK(benchmarkPKs, targetPKs, tableName, url, username, password);
                compareData(benchmarkTable, targetTable, tableName, url, username, password);
                System.out.println("Done checking table \"" + tableName + "\".\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareData(Map<String, Map<String, Object>> benchmarkTable, Map<String, Map<String, Object>> targetTable, String tableName, String url, String username, String password){  
        Scanner scanner = new Scanner(System.in);

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
                            System.out.println("Do you want to modify this column in the target database? Y for yes and N for no.");
                            String action = scanner.nextLine();
                            if (action.equals("Y") || action.equals("y")) {ModifyTableEn.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);}
                            else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}
                        }
                    } else {
                        System.out.println("Warning: Column \"" + column + "\" in the target databse has formatting error in table  \"" + tableName + "\".");
                        System.out.println("Do you want to modify this column in the target database? Y for yes and N for no.");
                        String action = scanner.nextLine();
                        if (action.equals("Y") || action.equals("y")) {ModifyTableEn.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);}
                        else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}
                    }
                }
            } else{
                System.out.println("Warning: Column \"" + column + "\" is missing in the target table \"" + tableName + "\".");
                System.out.println("Do you want to add this column to the target database? Y for yes and N for no.");
                String action = scanner.nextLine();
                if (action.equals("Y") || action.equals("y")) {ModifyTableEn.addColumn(tableName, column, typeName, colSize, decDigits, url, username, password);}
                else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}
            }
        }
        for (String column : targetTable.keySet()) {
            
            if (!benchmarkTable.containsKey(column)){
                System.out.println("Warning: Column \"" + column + "\" does not exist in the benchmark table \"" + tableName + "\".");
                System.out.println("Do you want to drop this column from the target database? Y for yes and N for no.");
                String action = scanner.nextLine();
                if (action.equals("Y") || action.equals("y")) {ModifyTableEn.dropColumn(tableName, column, url, username, password);}
                else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}
            }
        }
    }

    private static void comparePK(HashSet<String> benchmarkPKs, HashSet<String> targetPKs, String tableName, String url, String username, String password) {
        Scanner scanner = new Scanner(System.in);
        if (!(benchmarkPKs.size() == targetPKs.size() && benchmarkPKs.containsAll(targetPKs))) { // PKs do not match
            System.out.println("Warning: Primary Key does not match between the two databases in table \"" + tableName + "\".");
            System.out.println("Do you want to synchronize the primary key(s)? Y for yes and N for no.");
            String action = scanner.nextLine();
            if (action.equals("Y") || action.equals("y")) {
                if (benchmarkPKs.isEmpty()) { // if benchmarkPKs DNE
                    ModifyTableEn.dropPKs(tableName, url, username, password);
                } else if (targetPKs.isEmpty()) { // if targetPKs DNE
                    ModifyTableEn.addPKs(tableName, benchmarkPKs, url, username, password);
                } else{ // if PKs do not match
                    ModifyTableEn.modifyPKs(tableName, benchmarkPKs, url, username, password);
                }
            }
            else if (!(action.equals("N") || action.equals("n"))) {System.out.println("Invalid input, no action done.");}
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
