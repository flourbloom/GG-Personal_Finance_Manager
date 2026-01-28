# Personal Finance Manager

Currently this project is a simple JavaFX app, but currently CLI is being worked on.

## Prerequisites
Install maven binaries on your system.
Add maven bin folder to your system path.

## Running the project
To run the project, execute the following command in the terminal:

### For JavaFX application
```
mvn clean javafx:run
```
or
```
mvn javafx:run
```

The first command will clear any prebuilt binaries before running the application, while the second command will run the application without cleaning.

### For CLI application
```
mvn clean exec:java -Dexec.mainClass="gitgud.pfm.cli.CliMain"
```
or
```
mvn exec:java
```

## Building the project
To build the project, execute the following command in the terminal:

```
mvn clean package
```

This command will clean the project and package it into a JAR file located in the `target` directory.

The output will be be two jar files:
- pfm-gui.jar : JavaFX application
- pfm-cli.jar : CLI application

### Running the built JAR files
To run the built JAR files, use the following commands:
For JavaFX application:
```
java -jar target/pfm-gui.jar
```
For CLI application:
```
java -jar target/pfm-cli.jar
```

### difference between clean and without clean

The `clean` command in Maven is used to remove the `target` directory, which contains all the compiled files and build artifacts from previous builds. This ensures that the next build starts from a fresh state without any leftover files that could potentially cause issues.

When you run a Maven command with `clean`, it first deletes the `target` directory before executing the specified goal (like `package` or `javafx:run`). This is useful when you want to ensure that your build is not affected by any previous builds.


### How javafx communicates with fxml
For reference: fxml is similar to an html file

```
<Button fx:id="primaryButton" onAction="#switchToSecondary" text="Switch to Secondary View" />
<Button fx:id="primaryButton1" onAction="#exitProgram" text="Exit" />
```

the second button: `Exit` click will call the method `exitProgram` in the controller class `PrimaryController.java`

the `fx:id` is used to identify the button in the controller class if needed.