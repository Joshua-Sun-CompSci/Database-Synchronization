package com.joshua.sun.chinese;

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

public class CompareDataZh {

    // 全局变量
    private static Boolean backToMenu;
    private static Boolean syncAll;

    public static void compareData(String url, String username, String password) {

        // 初始值设为false
        backToMenu = false;
        syncAll = false;
        Scanner scanner = new Scanner(System.in);

        // 获取目标数据库的数据
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            // 读取benchmarkDatabase文件并将其转换为字符串
            String benchmarkDatabase = fileToString("benchmarkDatabase.txt");

            // 将字符串拆分为不同的表
            String[] benchmarkTables = benchmarkDatabase.split("\n[ \\t\\n]*\n");

            List<String> tableNames = TableLoaderZh.loadTableNames("tables.txt");

            // 遍历每个表
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = tableNames.get(i);
                System.out.println("正在检查表: " + tableName + "\n");

                // 获取每个benchmark表的内容
                String benchmarkString = benchmarkTables[i].trim();

                // 对于benchmark数据库中存在的每个表，获取主键和每个表中的内容
                HashSet<String> targetPKs = FetchDataZh.fetchPrimaryKeys(connection, tableName);
                Map<String, Map<String, Object>> targetTable = FetchDataZh.fetchTableStructure(connection, tableName);

                // 如果在benchmark和目标数据库中都不存在该表，打印消息警告用户
                if (benchmarkString.equals("Table DNE") && targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("警告: 表 \"" + tableName + "\" 在两个数据库中都不存在。");
                    System.out.println("完成检查表 \"" + tableName + "\"。\n");
                    continue;
                } else if (benchmarkString.equals("Table DNE")) { // 如果该表在benchmark数据库中不存在，则跳过它
                    continue;
                }

                String[] benchmarkArray = benchmarkString.split("\n");
                String benchmarkPKString = benchmarkArray[1];
                String[] benchmarkPKArray = benchmarkPKString.split("\\s+");

                // 过滤出 "Primary Keys: " 并仅保存其后的内容
                HashSet<String> benchmarkPKs = new HashSet<>();
                for (int j = 2; j < benchmarkPKArray.length; j++) {
                    String key = benchmarkPKArray[j].trim();
                    benchmarkPKs.add(key);
                }

                // 删除第一个和第二个元素（表的名称和主键）
                String[] benchmarkStringArray = Arrays.copyOfRange(benchmarkArray, 2, benchmarkArray.length);
                Map<String, Map<String, Object>> benchmarkTable = parseFields(benchmarkStringArray);

                // 如果该表在目标数据库中不存在，则在目标数据库中创建该表
                if (targetPKs.isEmpty() && targetTable.isEmpty()){
                    System.out.println("警告: 表 \"" + tableName + "\" 在目标数据库中缺失。");
                    System.out.println("完成检查表 \"" + tableName + "\"。\n");
                    String choice = "";
                    if (syncAll) {
                        choice = "YES";
                    } else {
                        System.out.println("是否同步此表?\n" + 
                                        "YES: 同步此表的所有列和主键\n" +
                                        "NO: 不同步，直接进行下一个表的比较\n" + 
                                        "YES-ALL: 同步所有剩余的表\n" +
                                        "NO-ALL: 跳过所有剩余的表\n" +
                                        "EXIT: 返回菜单");
                        choice = scanner.nextLine();
                    }
                    switch (choice) {
                        case "YES":
                            System.out.println("正在同步表 \"" + tableName + "\"...");
                            ModifyTableZh.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                            System.out.println("同步完成。\n");
                            break;

                        case "NO":
                            System.out.println("跳过表 \"" + tableName + "\"...\n");
                            break;

                        case "YES-ALL":
                            System.out.println("你确定要同步所有表吗？同步过程中无法停止。Y表示同意，N表示不同意。");
                            String confirm = scanner.nextLine();
                            if (confirm.equals("Y")) {
                                System.out.println("正在同步所有表...");
                                syncAll = true;
                                System.out.println("正在同步表 \"" + tableName + "\"...");
                                ModifyTableZh.createTable(tableName, benchmarkPKs, benchmarkTable, url, username, password);
                                System.out.println("同步完成。\n");
                                break;
                            } else if (confirm.equals("N")) {
                                System.out.println("好的，没有表被同步。\n");
                            } else {
                                System.out.println("无效输入，表未同步。\n");
                            }

                        case "NO-ALL":
                            System.out.println("跳过所有表...\n");
                            backToMenu = true;
                            break;

                        case "EXIT":
                            System.out.println("返回菜单...\n");
                            backToMenu = true;
                            break;

                        default:
                            System.out.println("无效输入，表未同步。\n");
                            break;
                    }
                    if (backToMenu) {
                        break;
                    }
                    continue; // 跳过不匹配的表
                }

                compareTable(benchmarkTable, benchmarkPKs, targetTable, targetPKs, tableName, url, username, password);

                // 如果用户输入“EXIT”，则返回菜单
                if (backToMenu) {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void compareTable(Map<String, Map<String, Object>> benchmarkTable, HashSet<String> benchmarkPKs, Map<String, Map<String, Object>> targetTable, HashSet<String> targetPKs, String tableName, String url, String username, String password){  

        // 在此存储所有命令
        ArrayList<String> commands = new ArrayList<>();

        for (String column : benchmarkTable.keySet()) {

            // 获取修改列时的所有信息
            String typeName = benchmarkTable.get(column).get("TYPE_NAME").toString();
            String colSize = String.valueOf((Integer)benchmarkTable.get(column).get("COLUMN_SIZE"));
            String decDigits = String.valueOf((Integer)benchmarkTable.get(column).get("DECIMAL_DIGITS"));

            // 如果两个表都有此列
            if (targetTable.containsKey(column)){

                // 检查所有详细信息是否匹配
                for (String detailKey: benchmarkTable.get(column).keySet()) {

                    // 如果targetTable也有该键（类型名称，列大小，小数位数）
                    if (targetTable.get(column).containsKey(detailKey)) {

                        // 如果值不匹配
                        if (!benchmarkTable.get(column).get(detailKey).equals(targetTable.get(column).get(detailKey))) {
                            System.out.println("警告: 目标数据库中表 \"" + tableName + "\" 中的列 \"" + column + "\" 与基准数据库不匹配。");
                            commands.add("modifyColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
                            break;
                        }
                    } else {
                        System.out.println("警告: 目标数据库中表 \"" + tableName + "\" 中的列 \"" + column + "\" 格式错误。");
                        commands.add("modifyColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
                        break;
                    }
                }
            } else{
                System.out.println("警告: 目标表 \"" + tableName + "\" 中缺少列 \"" + column + "\"。");
                commands.add("addColumn," + column + "," + typeName + "," + colSize + "," + decDigits);
            }
        }
        for (String column : targetTable.keySet()) {

            if (!benchmarkTable.containsKey(column)){
                System.out.println("警告: 基准表 \"" + tableName + "\" 中不存在列 \"" + column + "\"。");
                commands.add("dropColumn," + column);
            }
        }
        comparePK(benchmarkPKs, targetPKs, tableName, url, username, password, commands);
    }

    private static void comparePK(HashSet<String> benchmarkPKs, HashSet<String> targetPKs, String tableName, String url, String username, String password, ArrayList<String> commands) {
        Scanner scanner = new Scanner(System.in);
    
        // 首先打印警告（如果适用）
        if (!(benchmarkPKs.size() == targetPKs.size() && benchmarkPKs.containsAll(targetPKs))) { // 主键不匹配
            System.out.println("警告: 表 \"" + tableName + "\" 中的主键在两个数据库之间不匹配。");
        }
        System.out.println("完成检查表 \"" + tableName + "\"。\n");
    
        if (!commands.isEmpty()) {
            String choice = "";
            if (syncAll) {
                choice = "YES";
            } else {
                System.out.println("是否要同步此表？\n" +
                                   "YES: 同步此表的所有列和主键\n" +
                                   "NO: 不同步，直接进行下一个表的比较\n" +
                                   "YES-ALL: 同步所有剩余的表\n" +
                                   "NO-ALL: 跳过所有剩余的表\n" +
                                   "EXIT: 返回菜单");
                choice = scanner.nextLine();
            }
            switch (choice) {
                case "YES":
                    System.out.println("正在同步表 \"" + tableName + "\"...");
    
                    // 更新列
                    executeCommands(commands, tableName, url, username, password);
    
                    // 更新主键
                    if (benchmarkPKs.isEmpty()) { // 如果 benchmarkPKs 不存在
                        ModifyTableZh.dropPKs(tableName, url, username, password);
                    } else if (targetPKs.isEmpty()) { // 如果 targetPKs 不存在
                        ModifyTableZh.addPKs(tableName, benchmarkPKs, url, username, password);
                    } else { // 如果主键不匹配
                        ModifyTableZh.modifyPKs(tableName, benchmarkPKs, url, username, password);
                    }
                    System.out.println("同步完成。\n");
                    break;
    
                case "NO":
                    System.out.println("跳过表 \"" + tableName + "\"...\n");
                    break;
    
                case "YES-ALL":
                    System.out.println("您确定要同步所有表吗？同步过程中无法停止。Y 表示同意，N 表示不同意。");
                    String confirm = scanner.nextLine();
                    if (confirm.equals("Y")) {
                        System.out.println("正在同步所有表...");
                        syncAll = true;
                        System.out.println("正在同步表 \"" + tableName + "\"...");
    
                        // 更新列
                        executeCommands(commands, tableName, url, username, password);
    
                        // 更新主键
                        if (benchmarkPKs.isEmpty()) { // 如果 benchmarkPKs 不存在
                            ModifyTableZh.dropPKs(tableName, url, username, password);
                        } else if (targetPKs.isEmpty()) { // 如果 targetPKs 不存在
                            ModifyTableZh.addPKs(tableName, benchmarkPKs, url, username, password);
                        } else { // 如果主键不匹配
                            ModifyTableZh.modifyPKs(tableName, benchmarkPKs, url, username, password);
                        }
                        System.out.println("同步完成。\n");
                        break;
                    } else if (confirm.equals("N")) {
                        System.out.println("OK，没有同步任何表。\n");
                    } else {
                        System.out.println("无效输入，表未同步。\n");
                    }
    
                case "NO-ALL":
                    System.out.println("跳过所有表...\n");
                    backToMenu = true;
                    break;
    
                case "EXIT":
                    System.out.println("返回菜单...\n");
                    backToMenu = true;
                    break;
    
                default:
                    System.out.println("无效输入，表未同步。\n");
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
                ModifyTableZh.dropColumn(tableName, column, url, username, password);
            } else {
                String typeName = parts[2];
                String colSize = parts[3];
                String decDigits = parts[4];
                if (method.equals("addColumn")) {
                    ModifyTableZh.addColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                } else if (method.equals("modifyColumn")) {
                    ModifyTableZh.modifyColumn(tableName, column, typeName, colSize, decDigits, url, username, password);
                }
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