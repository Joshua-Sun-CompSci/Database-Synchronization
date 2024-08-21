# Database-Synchronization
This is a Java-based tool to compare and synchronize tables between a benchmark database and a target Oracle database

## Steps to run this program:
1. Download Java_Program to your computer and open the folder
2. Download Oracle Instant Client to your computer
3. Go to Makefile and change CLASSPATH on the 2nd line to your path to the Oracle Instant Client folder
4. Add the table names you want to synchronize to tables.txt
5. Go to terminal, cd to Java_Program, then type "make" and then "make run"
6. Enter the information needed to connect to the benchmark/target databse. Format: 
url = "jdbc:oracle:thin:@//YourHost:YourPort/YourServiceName"
username = "YourUsername"
password = "YourPassword"

## Steps to clean up:
1. Go to terminal, cd to this folder, then type "make clean"
