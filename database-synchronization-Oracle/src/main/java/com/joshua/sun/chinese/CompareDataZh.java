package com.joshua.sun.chinese;

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

public class CompareDataZh {
    public static void compareData(String url, String username, String password) { 
        Scanner scanner = new Scanner(System.in);
        
        // 从目标数据库获取数据
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // 读取 benchmarkDatabase 文件并转换为字符串
            String benchmarkDatabase = fileToString("benchmarkDatabase.txt");

            // 按表格拆分字符串
            String[] benchmarkTables = benchmarkDatabase.split("\n[ \\t\\n]*\n");
            
            List<String> tableNames = TableLoaderZh.loadTableNames("tables.txt");

            // 遍历每个表
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = tableNames.get(i);
                System.out.println("开始检查表\"" + tableName + "\"...");

                // 获取每个 benchmark 表的内容
                String benchmarkString = benchmarkTables[i];

                // 对于 benchmark 数据库中存在的每个表，获取主键及表内容
                HashSet<String> targetPKs = FetchDataZh.fetchPrimaryKeys(connection, tableName);
                Map<String, Map<String, Object>> targetTable = FetchDataZh.fetchTableStructure(connection, tableName);
                
                // 如果表在基准数据库和目标数据库中均不存在，打印警告信息
                if (benchmarkString.equals("表名称不存在") && targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("警告: 表\"" + tableName + "\"在任何一个数据库中都不存在。");
                    System.out.println("表\"" + tableName + "\"检查完毕。\n");
                    continue;
                } else if (benchmarkString.equals("表名称不存在")){ // 如果表在基准数据库中不存在，从目标数据库中删除该表
                    System.out.println("警告: 表\"" + tableName + "\"在基准数据库中不存在。");

                    System.out.println("您是否要删除目标数据库中的这个表？输入 y 确认，或输入 n 取消。");
                    String action = scanner.nextLine();
                    if (action.equals("Y") || action.equals("y")) {
                        ModifyTableZh.dropTable(tableName, url, username, password);
                    } else if (!(action.equals("N") || action.equals("n"))) {
                        System.out.println("无效输入，操作已取消。");
                    }

                    System.out.println("表\"" + tableName + "\"检查完毕。\n");
                    continue;
                }
                
                String[] benchmarkArray = benchmarkString.split("\n");
                String benchmarkPKString = benchmarkArray[1];
                String[] benchmarkPKArray = benchmarkPKString.split("\\s+");

                // 过滤掉 "Primary Keys: "，仅保存其后的部分
                HashSet<String> benchmarkPKs = new HashSet<>();
                for (int j = 2; j < benchmarkPKArray.length; j++) {
                    String key = benchmarkPKArray[j].trim();
                    benchmarkPKs.add(key);
                }
                
                // 删除前两个元素（表名和主键）
                String[] benchmarkStringArray = Arrays.copyOfRange(benchmarkArray, 2, benchmarkArray.length);
                Map<String, Map<String, Object>> benchmarkTable = parseFields(benchmarkStringArray);

                // 如果表在目标数据库中不存在，则在目标数据库中创建该表
                if (targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("警告: 表\"" + tableName + "\"在目标数据库中不存在。");

                    System.out.println("您是否要在目标数据库中创建这个表？输入 y 确认，或输入 n 取消。");
                    String action = scanner.nextLine();
                    if (action.equals("Y") || action.equals("y")) {
                        ModifyTableZh.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                    } else if (!(action.equals("N") || action.equals("n"))) {
                        System.out.println("无效输入，操作已取消。");
                    }

                    System.out.println("表\"" + tableName + "\"检查完毕。\n");
                    continue; // 跳过不匹配的表
                }

                // 比较主键
                comparePK(benchmarkPKs, targetPKs, tableName, url, username, password);
                compareData(benchmarkTable, targetTable, tableName, url, username, password);
                System.out.println("表\"" + tableName + "\"检查完毕。\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareData(Map<String, Map<String, Object>> benchmarkTable, Map<String, Map<String, Object>> targetTable, String tableName, String url, String username, String password){  
        Scanner scanner = new Scanner(System.in);

        for (String column : benchmarkTable.keySet()) {

            // 获取修改列时的所有信息
            String typeName = benchmarkTable.get(column).get("数据类型").toString();
            String colSize = String.valueOf((Integer)benchmarkTable.get(column).get("列大小"));
            String decDigits = String.valueOf((Integer)benchmarkTable.get(column).get("小数位数"));
    
            // 如果两个表都有该列
            if (targetTable.containsKey(column)){

                // 检查所有详细信息是否匹配
                for (String detailKey: benchmarkTable.get(column).keySet()) {

                    // 如果目标表也有该键（类型名、列大小、十进制位数）
                    if (targetTable.get(column).containsKey(detailKey)) {

                        // 如果值不匹配
                        if (!benchmarkTable.get(column).get(detailKey).equals(targetTable.get(column).get(detailKey))) {
                            System.out.println("警告: 目标数据库中的列 \"" + column + "\" 与基准数据库中的表 \"" + tableName + "\" 不匹配。");
                            System.out.println("您是否要修改目标数据库中的此列？输入 y 确认，或输入 n 取消。");
                            String action = scanner.nextLine();
                            if (action.equals("Y") || action.equals("y")) {
                                ModifyTableZh.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                            } else if (!(action.equals("N") || action.equals("n"))) {
                                System.out.println("无效输入，操作已取消。");
                            }
                        }
                    } else {
                        System.out.println("警告: 目标数据库中的列 \"" + column + "\" 在表 \"" + tableName + "\" 中格式错误。");
                        System.out.println("您是否要修改目标数据库中的此列？输入 y 确认，或输入 n 取消。");
                        String action = scanner.nextLine();
                        if (action.equals("Y") || action.equals("y")) {
                            ModifyTableZh.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                        } else if (!(action.equals("N") || action.equals("n"))) {
                            System.out.println("无效输入，操作已取消。");
                        }
                    }
                }
            } else{
                System.out.println("警告: 目标表 \"" + tableName + "\" 中缺少列 \"" + column + "\"。");
                System.out.println("您是否要将此列添加到目标数据库中？输入 y 确认，或输入 n 取消。");
                String action = scanner.nextLine();
                if (action.equals("Y") || action.equals("y")) {
                    ModifyTableZh.addColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                } else if (!(action.equals("N") || action.equals("n"))) {
                    System.out.println("无效输入，操作已取消。");
                }
            }
        }
        for (String column : targetTable.keySet()) {
            
            if (!benchmarkTable.containsKey(column)){
                System.out.println("警告: 列 \"" + column + "\" 在基准表 \"" + tableName + "\" 中不存在。");
                System.out.println("您是否要从目标数据库中删除此列？输入 y 确认，或输入 n 取消。");
                String action = scanner.nextLine();
                if (action.equals("Y") || action.equals("y")) {
                    ModifyTableZh.dropColumn(tableName, column, url, username, password);
                } else if (!(action.equals("N") || action.equals("n"))) {
                    System.out.println("无效输入，操作已取消。");
                }
            }
        }
    }

    private static void comparePK(HashSet<String> benchmarkPKs, HashSet<String> targetPKs, String tableName, String url, String username, String password) {
        Scanner scanner = new Scanner(System.in);
        if (!(benchmarkPKs.size() == targetPKs.size() && benchmarkPKs.containsAll(targetPKs))) { // 主键不匹配
            System.out.println("警告: 表 \"" + tableName + "\" 的主键在两个数据库中不匹配。");
            System.out.println("您是否要同步主键？输入 y 确认，或输入 n 取消。");
            String action = scanner.nextLine();
            if (action.equals("Y") || action.equals("y")) {
                if (benchmarkPKs.isEmpty()) { // 如果基准主键不存在
                    ModifyTableZh.dropPKs(tableName, url, username, password);
                } else if (targetPKs.isEmpty()) { // 如果目标主键不存在
                    ModifyTableZh.addPKs(tableName, benchmarkPKs, url, username, password);
                } else { // 如果主键不匹配
                    ModifyTableZh.modifyPKs(tableName, benchmarkPKs, url, username, password);
                }
            } else if (!(action.equals("N") || action.equals("n"))) {
                System.out.println("无效输入，操作已取消。");
            }
        }
    }

    // 从文件中读取并返回字符串
    private static String fileToString(String fileName){
        Path path = Paths.get(fileName);
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Map<String, Map<String, Object>> parseFields(String[] fields) {
        final String eMsg = "警告: 基准数据库格式错误，无法解析。";
        Map<String, Map<String, Object>> fieldMap = new HashMap<>();
        for (String field : fields) {
            String[] parts = field.split(",");

            // 例如：列名: UPDATETIME，只取 "UPDATETIME"
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