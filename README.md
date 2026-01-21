# GG-Personal_Finance_Manager
An application that helps track expenses, income, and savings goals with charts and reports

Download java 17 
https://adoptium.net/download?link=https%3A%2F%2Fgithub.com%2Fadoptium%2Ftemurin17-binaries%2Freleases%2Fdownload%2Fjdk-17.0.17%252B10%2FOpenJDK17U-jdk_x64_windows_hotspot_17.0.17_10.msi&vendor=Adoptium

Download Maven
https://dlcdn.apache.org/maven/maven-3/3.9.12/binaries/apache-maven-3.9.12-bin.zip

Extract the maven zip to C:\Program Files\Apache\
create Apache Folder if there isnt one

Edit Environment variables

Add this in the JAVA_HOME
C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot

Add this in PATH
C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin
C:\Program Files\Apache\apache-maven-3.9.12\bin

run this if u cant do run the program
export PATH="/c/Program Files/Apache/apache-maven-3.9.12/bin:$PATH"

# GG-Personal_Finance_Manager

An application to track expenses, income, and savings goals with charts and reports.

## Requirements

- Java JDK 17 (or later)
- Apache Maven 3.9.x (or later)

## Downloads (examples)

- Temurin (Adoptium) JDK 17: https://adoptium.net/
- Apache Maven: https://maven.apache.org/download.cgi

> The links above are examples. Download the installer/zip for your OS and note the install path.

## Set environment variables (Windows examples)

Replace the paths below with your actual install locations and versions.

- Using Command Prompt (persistent via `setx`) — reopen terminal after running:

```cmd (recommended)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
setx PATH "%PATH%;C:\Program Files\Apache\apache-maven-3.9.12\bin"
```

- Using PowerShell (session-only):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'
$env:Path += ';C:\Program Files\Apache\apache-maven-3.9.12\bin'
```

- Using Git Bash / MSYS (session-only):

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.17.10-hotspot"
export PATH="/c/Program Files/Apache/apache-maven-3.9.12/bin:$PATH"
```

## Verify your setup

Run these commands to confirm Java and Maven are available:

```bash
java -version
mvn -v
```

If `java -version` reports a Java version lower than 17, set `JAVA_HOME` to a JDK 17+ installation.

## Run the application

From the project root (where `pom.xml` lives) run:

```bash
mvn javafx:run
```

This project uses the `javafx-maven-plugin` and JavaFX dependencies declared in `pom.xml`. If the run fails, check that `JAVA_HOME` points to a JDK (not a JRE) and that the `mainClass` in the plugin configuration matches your main class (the POM currently sets `com.example.App`).

## Notes

- Paths shown are examples — substitute your installed versions and paths.
- `setx` makes persistent changes but requires starting a new terminal to take effect.
- On Windows, use PowerShell or CMD examples rather than the Git Bash `export` line unless you're running in a Unix-like shell.

