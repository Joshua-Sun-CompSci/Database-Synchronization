package week1;
import java.sql.*;
import java.util.Map;

public class ModifyTable {
    public static void createTable(String tableName, String PK, Map<String, String> columns, String url, String username, String password) {

        // SQL statement to create a table
        String createTableSQL = "CREATE TABLE " + tableName + "(" +
                                "id INT PRIMARY KEY, name VARCHAR(100), salary DECIMAL(10, 2))";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // Execute the create table SQL statement
            boolean isTableCreated = stmt.execute(createTableSQL);
            if (isTableCreated) {
                System.out.println("Table \"" + tableName + "\"created successfully.");
            } else {
                System.out.println("Table \"" + tableName + "\"creation failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTable(String tableName, String url, String username, String password){
        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // This deletes the target table
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
            
            // Execute the create table SQL statement
            int rowsAffected = stmt.executeUpdate(dropTableSQL);
            if (rowsAffected != 0) {
                System.out.println("Table \"" + tableName + "\"deleted successfully.");
            } else {
                System.out.println("Table \"" + tableName + "\"deletion failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
