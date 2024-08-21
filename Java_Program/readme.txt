Steps to run this program:
1. Go to Makefile and change the path on the 2nd line to your path to the oracle folder
2. Make sure all necessary oracle files are downloaded and in the correct path
3. Add the table names you want to compare to tables.txt
4. Go to terminal, cd to this folder, then type "make" and then "make run"
5. Enter the information needed to connect to the benchmark/target databse. Format: 
url = "jdbc:oracle:thin:@//YourHost:YourPort/YourServiceName"
username = "YourUsername"
password = "YourPassword"

Steps to clean up:
1. Go to terminal, cd to this folder, then type "make clean"

Note: I am learning how to use maven instead of makefile, but in the meanwhile, I will still use makefile for simplicity.   