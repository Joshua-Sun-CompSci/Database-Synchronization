# Database-Synchronization
This is a Java-based tool to compare and synchronize tables between a benchmark database and a target Oracle/PostgreSQL database

## Requirements
- [Maven](https://maven.apache.org/download.cgi)

## Steps to run
1. Install Maven for [Mac/Linux](https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz) or [Windows](https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip)
2. Clone the repository:
   ```bash
   git clone https://github.com/Joshua-Sun-CompSci/Database-Synchronization.git
3. Navigate to the program directory depends on your database management system:
   ```bash
   cd Database-Synchronization/Database-Synchronization-Oracle
   ```
   or
   ```bash
   cd Database-Synchronization/Database-Synchronization-PostgreSQL
   ```
5. Compile the program:
   ```bash
   mvn clean package
6. Add the tables you want to compare between the benchmark&target database to tables.txt in ./src/main/resources folder
6. Run the program:
   ```bash
   mvn exec:java
7. Enter the information needed to connect to the benchmark/target databse. Format:
```
url = "jdbc:oracle:thin:@//YourHost:YourPort/YourServiceName"(Oracle) or "jdbc:postgresql://hostname:port/dbname"(PostgreSQL)
username = "YourUsername"
password = "YourPassword"
```
