package com.joshua.sun.main;

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
        // 首先获取连接到数据库所需的数据
        // 初始化字符串以防输入无效
        String url = "";
        String username = "";
        String password = "";
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入以下信息以连接到基准数据库：");
        System.out.println("输入 url: ");
        url = scanner.nextLine();  // 读取 url 输入

        System.out.println("输入用户名: ");
        username = scanner.nextLine();

        System.out.println("输入密码: ");
        password = scanner.nextLine();

        // 将第一个数据保存到 benchmarkDatabase.txt
        SaveDataZh.saveData("benchmarkDatabase.txt", url, username, password);
        System.out.println(); // 打印换行符以进行格式化
        
        System.out.println("请输入以下信息以连接到目标数据库：");
        System.out.println("输入 url: ");
        url = scanner.nextLine();

        System.out.println("输入用户名: ");
        username = scanner.nextLine();

        System.out.println("输入密码: ");
        password = scanner.nextLine();
        System.out.println();

        CompareDataZh.compareData(url, username, password);
        scanner.close();
    }

    private static void englishPrompt() {
        // gets necessary data to connect to the database first
        // initialize strings just incase of invalid input
        String url = "";
        String username = "";
        String password = "";
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the following information to connect to the benchmark database:");
        System.out.println("Enter url: ");
        url = scanner.nextLine();  // Read username input

        System.out.println("Enter your usernamename: ");
        username = scanner.nextLine();

        System.out.println("Enter your password: ");
        password = scanner.nextLine();

        // Save the first data to benchamarkDatabse.txt
        SaveDataEn.saveData("benchmarkDatabase.txt", url, username, password);
        System.out.println(); // prints \n for formatting
        
        System.out.println("Please enter the following information to connect to the target database:");
        System.out.println("Enter url: ");
        url = scanner.nextLine();

        System.out.println("Enter your usernamename: ");
        username = scanner.nextLine();

        System.out.println("Enter your password: ");
        password = scanner.nextLine();
        System.out.println();

        CompareDataEn.compareData(url, username, password);
        scanner.close();
    }
}

