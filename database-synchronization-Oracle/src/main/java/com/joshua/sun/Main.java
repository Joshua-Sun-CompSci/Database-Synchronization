package com.joshua.sun;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        
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
        SaveData.saveData("benchmarkDatabase.txt", url, username, password);
        System.out.println(); // prints \n for formatting
        
        System.out.println("Please enter the following information to connect to the target database:");
        System.out.println("Enter url: ");
        url = scanner.nextLine();

        System.out.println("Enter your usernamename: ");
        username = scanner.nextLine();

        System.out.println("Enter your password: ");
        password = scanner.nextLine();

        CompareData.compareData(url, username, password);
        scanner.close();
    }
}
