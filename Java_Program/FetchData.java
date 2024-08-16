package week1;
import java.sql.*;
import java.util.*;

public class FetchData {
    // private static final String url = "jdbc:oracle:thin:@//20.0.0.239:1521/ORCL";
    // private static final String username = "wanet_666";
    // private static final String password = "666666";

    public static Map<String, String> fetchTableStructure(Connection connection, String tableName) throws SQLException {
        Map<String, String> tableStructure = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        // gets all data from one specific table
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {

                // stores <name, type>
                tableStructure.put(columns.getString("COLUMN_NAME"), columns.getString("TYPE_NAME"));
            }
        }

        return tableStructure;
    }

    public static String fetchPrimaryKey(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
            if (primaryKeys.next()) {

                // Fetch the primary key column name
                return primaryKeys.getString("COLUMN_NAME");
            } else {
                
                // No primary key found for the table
                return null;
            }
        }
    }
}
