package gitgud.pfm.cli;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.services.TransactionService;

import java.io.IOException;

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
        
        // Get transaction index
        System.out.print("Enter transaction ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        
        // Get amount
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        
        // Get title/description
        System.out.print("Enter transaction title: ");
        String title = scanner.nextLine().trim();
        
        // Call service to add transaction
        TransactionService transactionService = new TransactionService();
        transactionService.addTransaction(id, amount, title);
        
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
