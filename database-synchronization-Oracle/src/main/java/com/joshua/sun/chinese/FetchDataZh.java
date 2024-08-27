package com.joshua.sun.chinese;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FetchDataZh {

    // 从数据库中获取表结构
    public static Map<String, Map<String, Object>> fetchTableStructure(Connection connection, String tableName) throws SQLException {
        Map<String, Map<String, Object>> tableStructure = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        
        // 获取指定表的所有列信息
        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {

                // 从每一列中获取必要的数据
                String columnName = columns.getString("COLUMN_NAME");
                String typeName = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                
                // 保存详细数据
                Map<String, Object> columnDetails = new HashMap<>();
                columnDetails.put("数据类型", typeName);
                columnDetails.put("列大小", columnSize);
                columnDetails.put("小数位数", decimalDigits);
    
                tableStructure.put(columnName, columnDetails);
            }
        }
    
        return tableStructure;
    }

    // 从数据库中获取主键列
    public static HashSet<String> fetchPrimaryKeys(Connection connection, String tableName) throws SQLException {
        HashSet<String> primaryKeysSet = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();
    
        // 获取指定表的主键列信息
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
            while (primaryKeys.next()) {
                // 获取主键列名并添加到集合中
                String columnName = primaryKeys.getString("COLUMN_NAME");
                primaryKeysSet.add(columnName);
            }
        }
    
        // 返回主键列名的集合
        return primaryKeysSet;
    }
}
