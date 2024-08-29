package com.joshua.sun.chinese;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;

public class ModifyTableZh {

    // 以下方法用于修改整个表
    public static void createTable(String tableName, HashSet<String> PKs, Map<String, Map<String, Object>> table, String url, String username, String password) {

        // 处理开始
        System.out.println("正在创建表 \"" + tableName + "\"...");

        // 编写创建表的 SQL 代码
        String createTableSQL = "CREATE TABLE \"" + tableName + "\"(\n";
        for (String column : table.keySet()){
            String typeName = table.get(column).get("TYPE_NAME").toString();
            String colSize = String.valueOf((Integer)table.get(column).get("COLUMN_SIZE"));
            String decDigits = String.valueOf((Integer)table.get(column).get("DECIMAL_DIGITS"));
            
            String columnStr = "";
            if (typeName.equals("number")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + "," + decDigits + "),\n"; 
            } else if (typeName.equals("char")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + "),\n";
            } else if (typeName.equals("varchar")) {
                columnStr = "\"" + column + "\" " + typeName + "(" + colSize + "),\n";
            } else{ // 列是日期、整数等
                columnStr = "\"" + column + "\" " + typeName + ",\n";
            }
            createTableSQL += columnStr;
        }
        
        // 如果表有主键，设置主键
        if (!PKs.isEmpty()) {
            String PKString = "CONSTRAINT " + tableName + "_PK PRIMARY KEY (";
            for (String PK: PKs) {
                PKString += "\"" + PK + "\",";
            }
            // 移除最后的逗号并结束字符串
            PKString = PKString.substring(0, PKString.length() - 1) + ")\n";
            createTableSQL += PKString;
        } else {
            // 移除 ,\n 以避免错误
            createTableSQL = createTableSQL.substring(0, createTableSQL.length() - 2) + "\n";
        }
        createTableSQL += ")";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行创建表的 SQL 语句
            stmt.execute(createTableSQL);
            System.out.println("表 \"" + tableName + "\" 创建成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 创建失败。");
        }
    }

    // todo: 测试删除表
    public static void dropTable(String tableName, String url, String username, String password){
        
        // 处理开始
        System.out.println("正在删除表 \"" + tableName + "\"...");

        // 编写删除表的 SQL 代码
        String dropTableSQL = "DROP TABLE " + tableName;

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行删除表的 SQL 语句
            stmt.execute(dropTableSQL);
            System.out.println("表 \"" + tableName + "\" 删除成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 删除失败。");
        }
    }





    // 以下方法用于单独修改列
    public static void modifyColumn(String tableName, String column, String typeName, String colSize, String decDigits, String url, String username, String password) {
        
        // 处理开始
        System.out.println("正在修改表 \"" + tableName + "\" 的列 \"" + column + "\"...");

        // 编写修改列的 SQL 代码
        String modifyColumnSQL = "ALTER TABLE \"" + tableName + "\"\nALTER COLUMN \"" + column + "\" SET DATA TYPE " + typeName;

        if (typeName.equals("number")) {
            modifyColumnSQL += "(" + colSize + "," + decDigits + ")"; 
        } else if (typeName.equals("char")) {
            modifyColumnSQL += "(" + colSize + ")";
        } else if (typeName.equals("varchar")) {
            modifyColumnSQL += "(" + colSize + ")";
        } 

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行修改列的 SQL 语句
            stmt.execute(modifyColumnSQL);
            System.out.println("表 \"" + tableName + "\" 的列 \"" + column + "\" 修改成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 的列 \"" + column + "\" 修改失败。");
        }
    }

    public static void addColumn(String tableName, String column, String typeName, String colSize, String decDigits, String url, String username, String password) {
        
        // 处理开始
        System.out.println("正在向表 \"" + tableName + "\" 添加列 \"" + column + "\"...");

        // 编写添加列的 SQL 代码
        String addColumnSQL = "ALTER TABLE \"" + tableName + "\"\nADD COLUMN (\"" + column + "\" " + typeName;

        if (typeName.equals("number")) {
            addColumnSQL += "(" + colSize + "," + decDigits + "))"; 
        } else if (typeName.equals("char")) {
            addColumnSQL += "(" + colSize + "))";
        } else if (typeName.equals("varchar")) {
            addColumnSQL += "(" + colSize + "))";
        } else{ // 列是日期、整数等
            addColumnSQL += ")";
        }

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行添加列的 SQL 语句
            stmt.execute(addColumnSQL);
            System.out.println("列 \"" + column + "\" 已成功添加到表 \"" + tableName + "\"。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("列 \"" + column + "\" 添加到表 \"" + tableName + "\" 失败。");
        }
    }

    public static void dropColumn(String tableName, String column, String url, String username, String password) {

        // 处理开始
        System.out.println("正在从表 \"" + tableName + "\" 删除列 \"" + column + "\"...");

        // 编写删除列的 SQL 代码
        String dropColumnSQL = "ALTER TABLE \"" + tableName + "\"\nDROP COLUMN \"" + column + "\"";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行删除列的 SQL 语句
            stmt.execute(dropColumnSQL);
            System.out.println("表 \"" + tableName + "\" 的列 \"" + column + "\" 删除成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 的列 \"" + column + "\" 删除失败。");
        }
    }





    // 以下方法用于修改主键
    public static void modifyPKs(String tableName, HashSet<String> PKs, String url, String username, String password) {

        // 处理开始
        System.out.println("正在修改表 \"" + tableName + "\" 的主键...");

        dropPKs(tableName, url, username, password);
        addPKs(tableName, PKs, url, username, password);
    }

    public static void addPKs(String tableName, HashSet<String> PKs, String url, String username, String password) {

        // 处理开始
        System.out.println("正在为表 \"" + tableName + "\" 添加主键...");

        // 编写添加主键的 SQL 代码
        String addPKsSQL = "ALTER TABLE \"" + tableName + "\"\nADD CONSTRAINT " + tableName + "_PK PRIMARY KEY (";

        // 将所有主键添加到 SQL 代码中
        for (String PK: PKs) {
            addPKsSQL += "\"" + PK + "\",";
        }

        // 移除最后的逗号并结束字符串
        addPKsSQL = addPKsSQL.substring(0, addPKsSQL.length() - 1) + ")";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行添加主键的 SQL 语句
            stmt.execute(addPKsSQL);
            System.out.println("表 \"" + tableName + "\" 的主键添加成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 的主键添加失败。");
        }
    }

    public static void dropPKs(String tableName, String url, String username, String password) {
        
        // 处理开始
        System.out.println("正在删除表 \"" + tableName + "\" 的主键...");

        // 编写删除主键的 SQL 代码
        String dropPKsSQL = "ALTER TABLE \"" + tableName + "\"\nDROP CONSTRAINT " + tableName + "_PK";

        try (Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement()) {
            
            // 执行删除主键的 SQL 语句
            stmt.execute(dropPKsSQL);
            System.out.println("表 \"" + tableName + "\" 的主键删除成功。");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("表 \"" + tableName + "\" 的主键删除失败。");
        }
    }
}
