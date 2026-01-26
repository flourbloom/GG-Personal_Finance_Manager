## Prerequisites
Install maven binaries on your system.
Add maven bin folder to your system path.

### Running the project
To run the project, execute the following command in the terminal:

```
mvn clean javafx:run
```

This command will clean the project and run the JavaFX application.

### Building the project
To build the project, execute the following command in the terminal:

```
mvn clean package
```

This command will clean the project and package it into a JAR file located in the `target` directory.

### difference between clean and without clean

The `clean` command in Maven is used to remove the `target` directory, which contains all the compiled files and build artifacts from previous builds. This ensures that the next build starts from a fresh state without any leftover files that could potentially cause issues.

When you run a Maven command with `clean`, it first deletes the `target` directory before executing the specified goal (like `package` or `javafx:run`). This is useful when you want to ensure that your build is not affected by any previous builds.
