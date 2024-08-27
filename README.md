# Database-Synchronization
This is a Java-based tool to compare and synchronize tables between a benchmark database and a target Oracle/PostgreSQL database

## Requirements
- [Maven](https://maven.apache.org/download.cgi)
- [Oracle Instant Client](https://www.oracle.com/database/technologies/instant-client/downloads.html) or [PostgreSQL](https://www.postgresql.org/download/)

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
4. Navigate to the resources folder:
   ```bash
   cd src/main/resources
5. Add the tables you want to synchronize to table.txt:
   ```bash
   vi table.txt
6. Compile the program:
   ```bash
   mvn clean package
7. Run the program (change "en" to "zh" or simply remove it to run the program in Chinese):
   ```bash
   java -jar .\target\database-synchronization-1.0-SNAPSHOT.jar en
8. Enter the information needed to connect to the benchmark/target databse. Format:
   ```
   url = "jdbc:oracle:thin:@//YourHost:YourPort/YourServiceName"(Oracle) or "jdbc:postgresql://hostname:port/dbname"(PostgreSQL)
   username = "YourUsername"
   password = "YourPassword"
   ```
