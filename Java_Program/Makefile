# To do: change the path to your oracle folder
CLASSPATH = .:/usr/local/oracle/instantclient_21_3/ojdbc8.jar

# Define the source files
SOURCES = FetchData.java SaveData.java CompareData.java

# Define the class files that will be generated from the source files
CLASSES = $(SOURCES:.java=.class)

# Default target
all: $(CLASSES)

# Compile the Java source files into class files
$(CLASSES): %.class: %.java
	javac -cp $(CLASSPATH) $<

# Run the CompareData program
run:
	java -cp $(CLASSPATH) CompareData

# Clean up the generated class files
clean:
	rm -f *.class benchmarkDatabase.txt