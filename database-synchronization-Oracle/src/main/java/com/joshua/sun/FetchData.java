package com.joshua.sun;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FetchData {

    public static Map<String, Map<String, Object>> fetchTableStructure(Connection connection, String tableName) throws SQLException {
        Map<String, Map<String, Object>> tableStructure = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {

                // gets all necessary data from a column
                // Check is not implemented yet. I will figure it out soon(hopefully)
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                
                // save detailed data
                Map<String, Object> columnDetails = new HashMap<>();
                columnDetails.put("TYPE_NAME", typeName);
                columnDetails.put("COLUMN_SIZE", columnSize);
                columnDetails.put("DECIMAL_DIGITS", decimalDigits);
    
                tableStructure.put(columnName, columnDetails);
            }
        }
    
        return tableStructure;
    }

    public static HashSet<String> fetchPrimaryKeys(Connection connection, String tableName) throws SQLException {
        HashSet<String> primaryKeysSet = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();
    
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
            while (primaryKeys.next()) {
                // Fetch the primary key column name and add it to the set
                String columnName = primaryKeys.getString("COLUMN_NAME");
                primaryKeysSet.add(columnName);
            }
        }
    
        // Return the set of primary key column names
        return primaryKeysSet;
    }
}
