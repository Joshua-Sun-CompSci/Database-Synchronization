package com.joshua.sun.chinese;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TableLoaderZh {

    // 读取文件并返回表名列表
    public static List<String> loadTableNames(String fileName) {
        List<String> tableNames = new ArrayList<>();
        try (InputStream inputStream = TableLoaderZh.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            
            if (inputStream == null) {
                System.err.println("文件 \"" + fileName + "\" 为空。");
                System.exit(1); // 带错误退出程序
            }

            String line;
            while ((line = br.readLine()) != null) {

                // 如果行不以 "//" 开头，假设它是表名
                if (!(line.startsWith("//"))) {
                    tableNames.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableNames;
    }
}