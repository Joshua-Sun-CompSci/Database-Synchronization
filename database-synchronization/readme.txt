Steps to run this program:
1. Install Maven
2. Go to terminal and cd to this folder
3. Type "mvn clean package" to re-compile this program
4. Type "mvn exec:java" to run this program
5. Enter the information needed to connect to the benchmark/target databse. Format: 
url = "jdbc:oracle:thin:@//YourHost:YourPort/YourServiceName"
username = "YourUsername"
password = "YourPassword"

Steps to clean up:
1. Go to terminal, cd to this folder, then type "mvn clean"

Todo: 
1. change SaveTable's path, change clearFile's path, change fileToString's path(if needed)
2. seperate main file