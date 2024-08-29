package com.joshua.sun.main;

import java.io.File;
import java.util.Scanner;

import com.joshua.sun.chinese.CompareDataZh;
import com.joshua.sun.chinese.SaveDataZh;
import com.joshua.sun.english.CompareDataEn;
import com.joshua.sun.english.SaveDataEn;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0){ // use Chinese as defualt language 用中文作为默认语言
            chinesePrompt();
        } else if (args.length == 1) {
            String language = args[0];
            switch (language) {
                case "en":
                    englishPrompt();
                    break;
                case "zh":
                    chinesePrompt();
                    break;
                default:
                    System.out.println("Unknown language: " + language);
                    System.out.println("未知语言: " + language);
                    System.out.println("Usage: java MainProgram [en|zh]");
                    System.out.println("格式: java MainProgram [en|zh]");
                    break;
            }
        } else {
            System.out.println("Wrong formatting");
            System.out.println("错误格式");
            System.out.println("Usage: java MainProgram [en|zh]");
            System.out.println("格式: java MainProgram [en|zh]");
        }
    }

    private static void chinesePrompt() {
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("您好！今天您想做什么？请输入相应的数字。");
        System.out.println("1. 获取基准数据库的配置\n" +
                           "2. 同步目标数据库\n" +
                           "3. 退出程序");
        String option = scanner.nextLine();
    
        // 初始化字符串以防输入无效
        String url = "";
        String username = "";
        String password = "";
    
        switch (option) {
            case "1":
                System.out.println("请输入以下信息以连接到基准数据库：");
                System.out.println("输入网址：");
                url = scanner.nextLine();  // 读取网址输入
            
                System.out.println("输入你的用户名：");
                username = scanner.nextLine();
            
                System.out.println("输入你的密码：");
                password = scanner.nextLine();
            
                // 将数据保存到 benchmarkDatabase.txt
                Boolean success = SaveDataZh.saveData("benchmarkDatabase.txt", url, username, password);
                if (success){
                    System.out.println("完成获取配置。详细信息已保存在 benchmarkDatabase.txt 中");
                }
                
                chinesePrompt();
    
            case "2":
                File file = new File("benchmarkDatabase.txt");
                if (file.exists()){
                    System.out.println("请输入以下信息以连接到目标数据库：");
                    System.out.println("输入网址：");
                    url = scanner.nextLine();
                
                    System.out.println("输入你的用户名：");
                    username = scanner.nextLine();
                
                    System.out.println("输入你的密码：");
                    password = scanner.nextLine();
                    System.out.println();
                
                    CompareDataZh.compareData(url, username, password);
                } else {
                    System.out.println("未找到基准数据库配置。");
                }
                chinesePrompt();
    
            case "3":
                System.out.println("正在退出程序...");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("请输入有效的数字选项。");
                chinesePrompt();
        }
    }

    private static void englishPrompt() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Hello! What do you want to do today? Please enter the corresponding number.");
        System.out.println("1. Retrieve the configuration of the benchmark database\n" +
                           "2. Synchronize the target database\n" +
                           "3. Exit the program");
        String option = scanner.nextLine();

        // initialize string in case of invalid input
        String url = "";
        String username = "";
        String password = "";

        switch (option) {
            case "1":
                System.out.println("Please enter the following information to connect to the benchmark database:");
                System.out.println("Enter url: ");
                url = scanner.nextLine();  // Read username input
        
                System.out.println("Enter your usernamename: ");
                username = scanner.nextLine();
        
                System.out.println("Enter your password: ");
                password = scanner.nextLine();
        
                // Save the first data to benchamarkDatabse.txt
                Boolean success = SaveDataEn.saveData("benchmarkDatabase.txt", url, username, password);
                if (success){
                    System.out.println("Done retrieving configuration. Details are saved in benchmarkDatabase.txt\n");
                }
                
                englishPrompt();

            case "2":
                File file = new File("benchmarkDatabase.txt");
                if (file.exists()){
                    System.out.println("Please enter the following information to connect to the target database:");
                    System.out.println("Enter url: ");
                    url = scanner.nextLine();
            
                    System.out.println("Enter your usernamename: ");
                    username = scanner.nextLine();
            
                    System.out.println("Enter your password: ");
                    password = scanner.nextLine();
                    System.out.println();
            
                    CompareDataEn.compareData(url, username, password);
                } else {
                    System.out.println("Benchmark database configuration not found.\n");
                }
                englishPrompt();

            case "3":
                System.out.println("Exiting the program...\n");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Please enter a valid number option.\n");
                englishPrompt();
        }
    }
    
}

