package com.joshua.sun.english;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CompareDataEn {

    // global variables
    private static Boolean backToMenu;
    private static Boolean syncAll;

    public static void compareData(String url, String username, String password) { 

        // set initial value to false
        backToMenu = false;
        syncAll = false;
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
                System.out.println("Checking table: " + tableName + "\n");

                // get the content of each benchmark Tables
                String benchmarkString = benchmarkTables[i].trim();

                // for each table exists in benchmark DB, fetch PK and the content within each table
                HashSet<String> targetPKs = FetchDataEn.fetchPrimaryKeys(connection, tableName);
                Map<String, Map<String, Object>> targetTable = FetchDataEn.fetchTableStructure(connection, tableName);
                
                // if table DNE in both benchmark and target database, print the message to warn the user
                if (benchmarkString.equals("Table DNE") && targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("Warning: Table \"" + tableName + "\" does not exist in either database.");
                    System.out.println("Done checking table \"" + tableName + "\".\n");
                    continue;
                } else if (benchmarkString.equals("Table DNE")) { // if table DNE in the benchmark database, skip it 
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
                    System.out.println("Done checking table \"" + tableName + "\".\n");
                    String choice = "";
                    if (syncAll) {
                        choice = "YES";
                    } else {
                        System.out.println("Do you want to synchronize this table?\n" + 
                                        "YES: synchronize all columns and primary keys for this table\n" +
                                        "NO: skip this table and start the comparison of the next table\n" + 
                                        "YES-ALL: synchronize all the rest of tables\n" +
                                        "NO-ALL: skip all the rest of tables\n" +
                                        "EXIT: exit to main menu");
                        choice = scanner.nextLine();
                    }
                    switch (choice) {
                        case "YES":
                            System.out.println("Synchronizing table \"" + tableName + "\"...");
                            ModifyTableEn.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                            System.out.println("Done synchronizing.\n");
                            break;

                        case "NO":
                            System.out.println("Skipping table\"" + tableName + "\"...\n");
                            break;

                        case "YES-ALL":
                            System.out.println("Are you sure you want to synchronize all tables? You will not be able to stop it until all synchronization is done. Y for yes and N for no.");
                            String confirm = scanner.nextLine();
                            if (confirm.equals("Y")) {
                                System.out.println("Synchronizing all tables...");
                                syncAll = true;
                                System.out.println("Synchronizing table \"" + tableName + "\"...");
                                ModifyTableEn.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                                System.out.println("Done synchronizing.\n");
                                break;
                            } else if (confirm.equals("N")) {
                                System.out.println("OK, no tables were not synchronized.\n");
                            } else {
                                System.out.println("Invalid input, table was not synchronized.\n");
                            }

                        case "NO-ALL":
                            System.out.println("Skipping all tables...\n");
                            backToMenu = true;
                            break;

                        case "EXIT":
                            System.out.println("Exiting to the main menu...\n");
                            backToMenu = true;
                            break;

                        default:
                            System.out.println("Invalid input, table was not synchronized.\n");
                            break;
                    }
                    if (backToMenu) {
                        break;
                    }
                    continue; // skip the mismatch table
                }
                
                compareTable(benchmarkTable, benchmarkPKs, targetTable, targetPKs, tableName, url, username, password);
                
                // if user enters "EXIT", exit to main menu
                if (backToMenu) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareTable(Map<String, Map<String, Object>> benchmarkTable, HashSet<String> benchmarkPKs, Map<String, Map<String, Object>> targetTable, HashSet<String> targetPKs, String tableName, String url, String username, String password){  
        
        // stores all the commands here
        ArrayList<String> commands = new ArrayList<>();

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
                            commands.add("modifyColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
                            break;
                        }
                    } else {
                        System.out.println("Warning: Column \"" + column + "\" in the target databse has formatting error in table  \"" + tableName + "\".");
                        commands.add("modifyColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
                        break;
                    }
                }
            } else{
                System.out.println("Warning: Column \"" + column + "\" is missing in the target table \"" + tableName + "\".");
                commands.add("addColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
            }
        }
        for (String column : targetTable.keySet()) {
            
            if (!benchmarkTable.containsKey(column)){
                System.out.println("Warning: Column \"" + column + "\" does not exist in the benchmark table \"" + tableName + "\".");
                commands.add("dropColumn," + column);
            }
        }
        comparePK(benchmarkPKs, targetPKs, tableName, url, username, password, commands);
    }

    private static void comparePK(HashSet<String> benchmarkPKs, HashSet<String> targetPKs, String tableName, String url, String username, String password, ArrayList<String> commands) {
        Scanner scanner = new Scanner(System.in);

        // prints out the warning first (if applicable)
        if (!(benchmarkPKs.size() == targetPKs.size() && benchmarkPKs.containsAll(targetPKs))) { // PKs do not match
            System.out.println("Warning: Primary Key does not match between the two databases in table \"" + tableName + "\".");
        }
        System.out.println("Done checking table \"" + tableName + "\".\n");

        if (!commands.isEmpty()) {
            String choice = "";
                if (syncAll) {
                    choice = "YES";
                } else {
                    System.out.println("Do you want to synchronize this table?\n" + 
                                    "YES: synchronize all columns and primary keys for this table\n" +
                                    "NO: skip this table and start the comparison of the next table\n" + 
                                    "YES-ALL: synchronize all the rest of tables\n" +
                                    "NO-ALL: skip all the rest of tables\n" +
                                    "EXIT: exit to main menu");
                    choice = scanner.nextLine();
                }
            switch (choice) {
                case "YES":
                    System.out.println("Synchronizing table \"" + tableName + "\"...");

                    // update columns
                    executeCommands(commands, tableName, url, username, password);

                    // updates pks
                    if (benchmarkPKs.isEmpty()) { // if benchmarkPKs DNE
                        ModifyTableEn.dropPKs(tableName, url, username, password);
                    } else if (targetPKs.isEmpty()) { // if targetPKs DNE
                        ModifyTableEn.addPKs(tableName, benchmarkPKs, url, username, password);
                    } else{ // if PKs do not match
                        ModifyTableEn.modifyPKs(tableName, benchmarkPKs, url, username, password);
                    }
                    System.out.println("Done synchronizing.\n");
                    break;

                case "NO":
                    System.out.println("Skipping table\"" + tableName + "\"...\n");
                    break;

                case "YES-ALL":
                    System.out.println("Are you sure you want to synchronize all tables? You will not be able to stop it until all synchronization is done. Y for yes and N for no.");
                    String confirm = scanner.nextLine();
                    if (confirm.equals("Y")) {
                        System.out.println("Synchronizing all tables...");
                        syncAll = true;
                        System.out.println("Synchronizing table \"" + tableName + "\"...");

                        // update columns
                        executeCommands(commands, tableName, url, username, password);

                        // updates pks
                        if (benchmarkPKs.isEmpty()) { // if benchmarkPKs DNE
                            ModifyTableEn.dropPKs(tableName, url, username, password);
                        } else if (targetPKs.isEmpty()) { // if targetPKs DNE
                            ModifyTableEn.addPKs(tableName, benchmarkPKs, url, username, password);
                        } else{ // if PKs do not match
                            ModifyTableEn.modifyPKs(tableName, benchmarkPKs, url, username, password);
                        }
                        System.out.println("Done synchronizing.\n");
                        break;
                    } else if (confirm.equals("N")) {
                        System.out.println("OK, no tables were not synchronized.\n");
                    } else {
                        System.out.println("Invalid input, table was not synchronized.\n");
                    }

                case "NO-ALL":
                    System.out.println("Skipping all tables...\n");
                    backToMenu = true;
                    break;

                case "EXIT":
                    System.out.println("Exiting to the main menu...\n");
                    backToMenu = true;
                    break;

                default:
                    System.out.println("Invalid input, table was not synchronized.\n");
                    break;
            }
        }
    }

    public static void executeCommands(ArrayList<String> commands, String tableName, String url, String username, String password) {
        for (String command : commands) {
            String[] parts = command.split(",");
            String method = parts[0];
            String column = parts[1];
            if (method.equals("dropColumn")) {
                ModifyTableEn.dropColumn(tableName, column, url, username, password);
            } else {
                String typeName = parts[2];
                String colSize = parts[3];
                String decDigits = parts[4];
                if (method.equals("addColumn")) {
                    ModifyTableEn.addColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                } else if (method.equals("modifyColumn")) {
                    ModifyTableEn.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                }
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
