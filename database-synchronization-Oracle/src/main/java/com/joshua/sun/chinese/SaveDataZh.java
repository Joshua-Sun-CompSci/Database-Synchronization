package com.joshua.sun.chinese;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SaveDataZh {
    public static Boolean saveData(String fileName, String url, String username, String password) {
        Boolean success = true;
        // 首先清空输出文件
        clearFile(fileName);

        // 从 tables.txt 中获取所有表名并将其放入列表
        List<String> tableNames = TableLoaderZh.loadTableNames("tables.txt");
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // 遍历所有表
            for (String tableName : tableNames) {
                Map<String, Map<String, Object>> table = FetchDataZh.fetchTableStructure(connection, tableName);
                HashSet<String> PKs = FetchDataZh.fetchPrimaryKeys(connection, tableName);
                saveTable(PKs, table, tableName, fileName); // 将表写入 fileName
            }
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    // 清空文件
    private static void clearFile(String fileName) {
        // 通过以写入模式打开文件并不追加内容来清空文件
        // 写入一个空字符串有效地清除文件内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将表的内容写入 fileName
    private static void saveTable(HashSet<String> PKs, Map<String, Map<String, Object>> table, String tableName, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            if (PKs.isEmpty() && table.isEmpty()) {
                writer.write("表名称不存在\n\n");
                return;
            } else {
                writer.write("表名称: " + tableName + "\n");
            }

            // 开始将所有主键写入文件
            writer.write("主键: ");
            for (String PK: PKs){
                writer.write(PK + " ");
            }
            writer.write("\n"); // 完成写入所有主键
            
            // 开始将详细信息写入文件
            for (Map.Entry<String, Map<String, Object>> columnEntry : table.entrySet()) {
                String name = columnEntry.getKey();
                Map<String, Object> details = columnEntry.getValue();
                writer.write("列名: " + name + ", ");
                writer.write("数据类型: " + details.get("数据类型") + ", ");
                writer.write("列大小: " + details.get("列大小") + ", ");
                writer.write("小数位数: " + details.get("小数位数") + "\n");
            }
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
