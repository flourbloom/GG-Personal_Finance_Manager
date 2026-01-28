package gitgud.pfm.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.Models.*;
import gitgud.pfm.services.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CLI Controller - Manages the command-line interface and user interactions
 * Handles menu flow and delegates business logic to services
 */
public class CliController {
    private Scanner scanner;
    private boolean running = true;

    public CliController() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Start the CLI application
     */
    public void start() {
        printWelcomeMessage();
        mainMenuLoop();
        shutdown();
    }

    /**
     * Main menu loop - handles user input and navigation
     */
    private void mainMenuLoop() {
        String defaultAccountID = "default-account-id";
        AccountDataLoader.DataHolder accountData = AccountDataLoader.AccountDataLoader(defaultAccountID);
        // Read Data from Database using id form Wallet,Transaction,Budget,Goal Services
        while (running) {

            printMainMenu();
            System.out.println();
            System.out.print("Please select an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleViewAccounts();
                    break;
                case "2":
                    handleAddTransaction(defaultAccountID);
                    break;
                case "3":
                    handleAddBudget(defaultAccountID);
                    break;
                case "4":
                    handleAddGoal(defaultAccountID);
                    break;
                case "5":
                    handleViewReports();
                    break;
                case "6":
                    handleViewBudgets();
                    break;
                case "7":
                    handleViewGoals();
                    break;
                case "8":
                    // looks for users input then call exit program
                    // then changes running to false to exit loop
                    handleExit();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

            System.out.println();
            pauseConsole();
            clearConsole();
        }
    }

    // "handle" prefix refers to dealing with user input rather than logic

    /**
     * Handle View Accounts menu option
     */
    private void handleViewAccounts() {
        System.out.println("View Accounts feature is not implemented yet.");
    }

    /**
     * Handle View Reports menu option
     */
    private void handleViewReports() {
        System.out.println("View Reports feature is not implemented yet.");
    }

    /**
     * Handle View Budgets menu option
     */
    private void handleViewBudgets() {
        System.out.println("View Budgets feature is not implemented yet.");
    }

    /**
     * Handle View Goals menu option
     */
    private void handleViewGoals() {
        System.out.println("View Goals feature is not implemented yet.");
    }

    /**
     * Handle Exit menu option
     */
    private void handleExit() {
        System.out.println("Exiting the Personal Finance Manager CLI. Goodbye!");
        exitProgram();
    }

    private void printWelcomeMessage() {
        System.out.println("Welcome to the Personal Finance Manager CLI!");
        System.out.println("-------------------------------------------");
    }

    private void printMainMenu() {
        System.out.println("Main Menu: ");
        System.out.println("1. Select Accounts");
        System.out.println("2. Add Transaction");
        System.out.println("3. Add Budget");
        System.out.println("4. Add Goal");
        System.out.println("-------------------------------------------");
        System.out.println("5. View Transaction Reports");
        System.out.println("6. View Budgets");
        System.out.println("7. View Goals");
        System.out.println("-------------------------------------------");
        System.out.println("8. Exit");

    }

    /**
     * Handle Add Transaction menu option
     */
    private void handleAddTransaction(String accountID) {
        System.out.println("=== Add Transaction ===");

        System.out.print("Enter transaction ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter transaction name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter categories (comma-separated): ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter income amount (0 if expense): ");
        double income = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter creation time (YYYY-MM-DD or leave blank for now): ");
        String createTimeInput = scanner.nextLine().trim();
        if (createTimeInput.isEmpty()) {
            createTimeInput = LocalDateTime.now().toString();
        }

        String timestamp = createTimeInput.isEmpty() ? java.time.LocalDateTime.now().toString() : createTimeInput;

        // Construct Transaction; constructor persists to DB like Budget
        Transaction transaction = new Transaction(id, category, amount, name, income, accountID, timestamp);

        System.out.println("Transaction created: " + transaction.getName());
    }

    /**
     * Handle Add Budget menu option
     */
    private void handleAddBudget(String accountID) {
        System.out.println("=== Add Budget ===");

        System.out.print("Enter budget ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter budget name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter limit amount: ");
        double limits = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter starting balance: ");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter start date (YYYY-MM-DD leave blank for now): ");
        String startDate = scanner.nextLine().trim();
        if (startDate.isEmpty()) {
            startDate = LocalDateTime.now().toString();
        }

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();

        System.out.print("Enter tracked categories (comma-separated): ");
        String tracked = scanner.nextLine().trim();

        Budget budget = new Budget(id, name, limits, balance, startDate, endDate, tracked);

        System.out.println("Budget created: " + budget.getName());
    }

    /**
     * Handle Add Goal menu option
     */
    private void handleAddGoal(String accountID) {
        System.out.println("=== Add Goal ===");

        System.out.print("Enter goal ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Enter goal name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter target amount: ");
        double target = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter current amount: ");
        double current = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter deadline (YYYY-MM-DD): ");
        String deadline = scanner.nextLine().trim();

        System.out.print("Enter priority (numeric): ");
        double priority = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter creation time (YYYY-MM-DD or leave blank for now): ");
        String createAt = scanner.nextLine().trim();
        if (createAt.isEmpty()) {
            createAt = LocalDateTime.now().toString();
        }

        Goal goal = new Goal(id, name, target, current, deadline, priority, createAt);

        System.out.println("Goal created: " + goal.getName());
    }

    private void exitProgram() {
        running = false;
    }

    private void pauseConsole() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    // Source - https://stackoverflow.com/a
    // Posted by Muhammed Gül
    // Retrieved 2026-01-27, License - CC BY-SA 4.0
    private void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ex) {
            // Ignore errors in clearing console
        }
    }

    private void shutdown() {
        scanner.close();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
