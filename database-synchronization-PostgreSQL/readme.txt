Steps to run this program:
1. Install Maven
2. Go to terminal and cd to this folder
3. Type "mvn clean package" to re-compile this program
4. Type "mvn exec:java" to run this program
5. Enter the information needed to connect to the benchmark/target database. Format: 
url = "jdbc:postgresql://hostname:port/dbname"
username = "YourUsername"
password = "YourPassword"

Steps to clean up:
1. Go to terminal, cd to this folder, then type "mvn clean"