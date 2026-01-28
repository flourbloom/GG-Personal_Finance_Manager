package gitgud.pfm.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.GenericSQLiteService;

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
                    handleAddTransaction();
                    break;
                case "3":
                    handleViewReports();
                    break;
                case "4":
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
        // TODO: Call AccountService to get and display accounts
    }
    
    /**
     * Handle Add Transaction menu option
     */
    private void handleAddTransaction() {
        System.out.println("=== Add Transaction ===");

        // Get transaction ID
        System.out.print("Enter transaction ID: ");
        String id = scanner.nextLine().trim();
        
        // Get amount
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        
        // Get transaction name
        System.out.print("Enter transaction name: ");
        String name = scanner.nextLine().trim();
        
        // Get category type (Expense or Income) and then category
        String[] expenseCategories = {"Grocery", "Food", "Transport", "Utilities"};
        String[] incomeCategories = {"Salary", "Bonus", "Investment", "Gift"};
        System.out.println("Select transaction type:");
        System.out.println("1. Expense");
        System.out.println("2. Income");
        int typeChoice = -1;
        while (typeChoice != 1 && typeChoice != 2) {
            System.out.print("Enter 1 for Expense or 2 for Income: ");
            try {
                typeChoice = Integer.parseInt(scanner.nextLine().trim());
                if (typeChoice != 1 && typeChoice != 2) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter 1 or 2.");
            }
        }
        String[] categories = (typeChoice == 1) ? expenseCategories : incomeCategories;
        System.out.println((typeChoice == 1 ? "Expense" : "Income") + " Categories:");
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%d. %s\n", i + 1, categories[i]);
        }
        int categoryChoice = -1;
        while (categoryChoice < 1 || categoryChoice > categories.length) {
            System.out.print("Select a category by number: ");
            try {
                categoryChoice = Integer.parseInt(scanner.nextLine().trim());
                if (categoryChoice < 1 || categoryChoice > categories.length) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        String category = categories[categoryChoice - 1];
        
        // Get account ID
        System.out.print("Enter account ID: ");
        String accountID = scanner.nextLine().trim();
        
        // Get income amount (0 if expense)
        System.out.print("Enter income amount (0 if expense): ");
        double income = Double.parseDouble(scanner.nextLine().trim());

        // Get current timestamp
        String timestamp = java.time.LocalDateTime.now().toString();

        // Call service to add transaction
        Transaction transaction = new Transaction(id, category, amount, name, income, accountID, timestamp);
        
        Map<String, Object> config = new HashMap<>();
        config.put("class", Transaction.class);
        config.put("table", "transaction_records");
        config.put("entity", transaction);
        GenericSQLiteService.create(config);
        
        System.out.println("Transaction added successfully!");
    }
    
    /**
     * Handle View Reports menu option
     */
    private void handleViewReports() {
        System.out.println("View Reports feature is not implemented yet.");
        // TODO: Call ReportService to display reports
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
        System.out.println("Main Menu:");
        System.out.println("1. View Accounts");
        System.out.println("2. Add Transaction");
        System.out.println("3. View Reports");
        System.out.println("4. Exit");
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
            }
            else {
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
