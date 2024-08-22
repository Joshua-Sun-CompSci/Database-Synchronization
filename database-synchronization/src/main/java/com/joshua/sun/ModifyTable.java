package com.joshua.sun;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;

public class ModifyTable {

    // the methods below modifies the whole table
    public static void createTable(String tableName, HashSet<String> PKs, Map<String, Map<String, Object>> table, String url, String username, String password) {

        // process starts 
        System.out.println("Creating table \"" + tableName + "\"...");

        // write SQL code to create a table
        String createTableSQL = "CREATE TABLE \"" + tableName + "\"(\n";
        for (String column : table.keySet()){
            String typeName = table.get(column).get("TYPE_NAME").toString();
            String colSize = String.valueOf((Integer)table.get(column).get("COLUMN_SIZE"));
            String decDigits = String.valueOf((Integer)table.get(column).get("DECIMAL_DIGITS"));
            
            String columnStr = "";
            if (typeName.equals("NUMBER")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + "," + decDigits + "),\n"; 
            } else if (typeName.equals("CHAR")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + "),\n";
            } else if (typeName.equals("VARCHAR2")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + " CHAR),\n";
            } else{ // column is date, integer, etc.
                columnStr = "\"" + column + "\" " + typeName + ",\n";
            }
            createTableSQL += columnStr;
        }
        
        // if the table has PKs, synchronize the PKs
        if (!PKs.isEmpty()) {
            String PKString = "CONSTRAINT " + tableName + "_PK PRIMARY KEY (";
            for (String PK: PKs) {
                PKString += "\"" + PK + "\",";
            }
            // this removes the last comma and finished the string
            PKString = PKString.substring(0, PKString.length() - 1) + ")";
            createTableSQL += PKString + "\n)";
        }

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(createTableSQL);
            System.out.println("Table \"" + tableName + "\" created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Table \"" + tableName + "\" creation failed.");
        }
    }

    // todo: test drop Table
    public static void dropTable(String tableName, String url, String username, String password){
        
        // process starts 
        System.out.println("Dropping table \"" + tableName + "\"...");

        // write SQL code to drop a table
        String dropTableSQL = "DROP TABLE " + tableName;

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(dropTableSQL);
            System.out.println("Table \"" + tableName + "\" dropped successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Table \"" + tableName + "\" failed to drop.");
        }
    }





    // the methods below modifies column individually
    public static void modifyColumn(String tableName, String column, String typeName, String colSize, String decDigits, String url, String username, String password) {
        
        // process starts 
        System.out.println("Modifying column \"" + column + "\" for table \"" + tableName + "\"...");

        // write SQL code to modify a column
        String modifyColumnSQL = "ALTER TABLE " + tableName + "\nMODIFY \"" + column + "\" " + typeName;

        if (typeName.equals("NUMBER")) {
            modifyColumnSQL += "(" + colSize + "," + decDigits + ")"; 
        } else if (typeName.equals("CHAR")) {
            modifyColumnSQL += "(" + colSize + ")";
        } else if (typeName.equals("VARCHAR2")) {
            modifyColumnSQL += "(" + colSize + " CHAR)";
        } 

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(modifyColumnSQL);
            System.out.println("Column \"" + column + "\" in table \"" + tableName + "\" modified successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Column \"" + column + "\" in table \"" + tableName + "\" failed to modify.");
        }
    }

    public static void addColumn(String tableName, String column, String typeName, String colSize, String decDigits, String url, String username, String password) {
        
        // process starts 
        System.out.println("Adding column \"" + column + "\" for table \"" + tableName + "\"...");

        // write SQL code to add a column
        String addColumnSQL = "ALTER TABLE " + tableName + "\nADD (\"" + column + "\" " + typeName;

        if (typeName.equals("NUMBER")) {
            addColumnSQL += "(" + colSize + "," + decDigits + "))"; 
        } else if (typeName.equals("CHAR")) {
            addColumnSQL += "(" + colSize + "))";
        } else if (typeName.equals("VARCHAR2")) {
            addColumnSQL += "(" + colSize + " CHAR))";
        } else{ // column is date, integer, etc.
            addColumnSQL += ")";
        }

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(addColumnSQL);
            System.out.println("Column \"" + column + "\" added to table \"" + tableName + "\" successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Column \"" + column + "\" failed to add to table \"" + tableName + "\".");
        }
    }

    public static void dropColumn(String tableName, String column, String url, String username, String password) {

        // process starts 
        System.out.println("Dropping column \"" + column + "\" for table \"" + tableName + "\"...");

        // write SQL code to drop a column
        String dropColumnSQL = "ALTER TABLE "  + tableName + "\nDROP COLUMN \"" + column + "\"";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(dropColumnSQL);
            System.out.println("Column \"" + column + "\" in table \"" + tableName + "\" dropped successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Column \"" + column + "\" in table \"" + tableName + "\" failed to drop.");
        }
    }





    // the methods below modifies the primary key
    public static void modifyPKs(String tableName, HashSet<String> PKs, String url, String username, String password) {

        // process starts 
        System.out.println("Modifying the primary key(s) for table \"" + tableName + "\"...");

        dropPKs(tableName, url, username, password);
        addPKs(tableName, PKs, url, username, password);
    }

    public static void addPKs(String tableName, HashSet<String> PKs, String url, String username, String password) {

        // process starts 
        System.out.println("Adding the primary key(s) for table \"" + tableName + "\"...");

        // write SQL code to add PKs
        String addPKsSQL = "ALTER TABLE "  + tableName + "\nADD CONSTRAINT " + tableName + "_PK PRIMARY KEY (";

        // this adds all PKs to the SQL code
        for (String PK: PKs) {
            addPKsSQL += "\"" + PK + "\",";
        }

        // this removes the last comma and finished the string
        addPKsSQL = addPKsSQL.substring(0, addPKsSQL.length() - 1) + ")";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(addPKsSQL);
            System.out.println("Primary key(s) in table \"" + tableName + "\" added successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Primary key(s) in table \"" + tableName + "\" failed to add.");
        }
    }

    public static void dropPKs(String tableName, String url, String username, String password) {
        
        // process starts 
        System.out.println("Dropping the primary key(s) for table \"" + tableName + "\"...");

        // write SQL code to drop PKs
        String dropPKsSQL = "ALTER TABLE "  + tableName + "\nDROP CONSTRAINT " + tableName + "_PK";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            stmt.execute(dropPKsSQL);
            System.out.println("Primary key(s) in table \"" + tableName + "\" dropped successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Primary key(s) in table \"" + tableName + "\" failed to drop.");
        }
    }
}
