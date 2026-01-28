package gitgud.pfm.cli;

import java.util.UUID;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.Models.*;
import gitgud.pfm.services.*;

import java.io.IOException;
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

        AccountDataLoader.DataHolder accountData = AccountDataLoader.loadAccountData(defaultAccountID);
        // Read Data from Database using id form Wallet,Transaction,Budget,Goal Services
        while (running) {

            printMainMenu(defaultAccountID);
            System.out.println();
            System.out.print("Please select an option: ");
            String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        handleViewAccounts();
                        break;
                    case "2":
                        handleAddTransaction(defaultAccountID, accountData);
                        break;
                    case "3":
                        handleAddBudget(defaultAccountID, accountData);
                        break;
                    case "4":
                        handleAddGoal(defaultAccountID, accountData);
                        break;
                    case "5":
                        handleViewReports(accountData);
                        break;
                    case "6":
                        handleViewBudgets(accountData);
                        break;
                    case "7":
                        handleViewGoals(accountData);
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
     * 
     * this.ID = ID;
     * this.Categories = Categories;
     * this.Amount = Amount;
     * this.Name = Name;
     * this.Income = Income;
     * this.AccountID = AccountID;
     * this.Create_time = Create_time;
     */
    private void handleViewReports(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== View Transaction Reports ===");
        System.out.printf("%-20s %10s %-20s %10s %-15s %20s%n", "Name", "Amount", "Categories", "Income", "Account ID", "Created At");
        for (Transaction t : accountData.getTransactions()) {
            System.out.printf("%-20s %10.2f %-20s %10.2f %-15s %20s%n",
                    t.getName(),
                    t.getAmount(),
                    t.getCategories(),
                    t.getIncome(),
                    t.getAccountID(),
                    t.getCreate_time());
        }
    }

    /**
     * Handle View Budgets menu option
     * private String id;
     * private String name;
     * private double limits;
     * private double balance;
     * private String start_date;
     * private String end_date;
     * private String trackedCategories;
     */
    private void handleViewBudgets(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== View Budgets ===");
        System.out.printf("%-20s %10s %10s %-12s %-12s %-20s%n", "Name", "Limits", "Balance", "Start", "End", "Tracked Categories");
        for (Budget b : accountData.getBudgets()) {
            System.out.printf("%-20s %10.2f %10.2f %-12s %-12s %-20s%n",
                    b.getName(),
                    b.getLimits(),
                    b.getBalance(),
                    b.getStart_date(),
                    b.getEnd_date(),
                    b.getTrackedCategories());
        }
    }

    /**
     * Handle View Goals menu option
     * private String id;
     * private String name;
     * private double target;
     * private double current;
     * private String deadline;
     * private double priority;
     * private String createAt;
     */
    private void handleViewGoals(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== View Goals ===");
        System.out.printf("%-20s %10s %10s %-12s %10s %20s%n", "Name", "Target", "Current", "Deadline", "Priority", "Created At");
        for (Goal g : accountData.getGoals()) {
            System.out.printf("%-20s %10.2f %10.2f %-12s %10.2f %20s%n",
                    g.getName(),
                    g.getTarget(),
                    g.getCurrent(),
                    g.getDeadline(),
                    g.getPriority(),
                    g.getCreateAt());
        }
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

    private void printMainMenu(String AccountID) {
        System.out.println("Main Menu: " + AccountID);
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
    private void handleAddTransaction(String accountID, AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Add Transaction ===");

        // Auto-generate a unique transaction ID
        String id = UUID.randomUUID().toString();

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

        // Construct Transaction; constructor persists to DB like Budget
        Transaction transaction = new Transaction(id, category, amount, name, income, accountID, createTimeInput);
        accountData.getTransactions().add(transaction);
        System.out.println("Transaction created: " + transaction.getName());
    }

    /**
     * Handle Add Budget menu option
     */
    private void handleAddBudget(String accountID, AccountDataLoader.DataHolder accountData) {
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
        accountData.getBudgets().add(budget);
        System.out.println("Budget created: " + budget.getName());
    }

    /**
     * Handle Add Goal menu option
     */
    private void handleAddGoal(String accountID, AccountDataLoader.DataHolder accountData) {
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
        accountData.getGoals().add(goal);
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
