package gitgud.pfm.cli;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Account;
import gitgud.pfm.services.CategoryService;
import gitgud.pfm.services.AccountService;
import gitgud.pfm.services.BudgetService;
import gitgud.pfm.services.GoalService;
import gitgud.pfm.services.TransactionService;
import gitgud.pfm.Models.Category;

import java.io.IOException;

/**
 * CLI Controller - Manages the command-line interface and user interactions
 * Handles menu flow and delegates business logic to services
 */
public class CliController {
    private Scanner scanner;
    private boolean running = true;
    private final CategoryService categoryService = new CategoryService();
    private final AccountService accountService = new AccountService();
    private final BudgetService budgetService = new BudgetService();
    private final GoalService goalService = new GoalService();
    private final TransactionService transactionService = new TransactionService();
    
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

    private void printAllCategories() {
        var defaultCategories = categoryService.getDefaultCategories();
        System.out.println("Available Categories:");
        System.out.println("Expense Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.EXPENSE) {
                System.out.println("  - " + cat.getName());
            }
        }
        System.out.println("Income Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.INCOME) {
                System.out.println("  - " + cat.getName());
            }
        }
        System.out.println();
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
                    handleAccountSummary();
                    break;
                case "2":
                    handleViewAllTransactions();
                    break;
                case "3":
                    handleAddTransaction();
                    break;
                case "4":
                    handleViewAllBudgets();
                    break;
                case "5":
                    handleAddBudget();
                    break;
                case "6":
                    handleViewAllGoals();
                    break;
                case "7":
                    handleAddGoal();
                    break;
                case "8":
                    handleViewReports();
                    break;
                case "9":
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
     * Handle Account Summary - show all accounts with balances and total
     */
    private void handleAccountSummary() {
        System.out.println("=== Account Summary ===");
        
        List<Account> accounts = accountService.readAll();
        
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            System.out.println("\nAccounts:");
            System.out.println("----------------------------------------");
            System.out.printf("%-15s %-20s %15s\n", "Account ID", "Name", "Balance");
            System.out.println("----------------------------------------");
            
            for (Account account : accounts) {
                System.out.printf("%-15s %-20s $%,14.2f\n", 
                    account.getId(), 
                    account.getName(), 
                    account.getBalance());
            }
            
            System.out.println("----------------------------------------");
            double totalBalance = accountService.getTotalBalance();
            System.out.printf("%-35s $%,14.2f\n", "Total Balance:", totalBalance);
            System.out.println("========================================\n");
        }
    }
    
    /**
     * Handle View All Transactions
     */
    private void handleViewAllTransactions() {
        System.out.println("=== All Transactions ===");
        
        List<Transaction> transactions = transactionService.readAll();
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.println("\nTransactions (most recent first):");
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %-15s %-12s $%-12s %-20s\n", 
                "ID", "Name", "Category", "Account", "Amount", "Date");
            System.out.println("--------------------------------------------------------------------------------------------");
            
            for (Transaction tx : transactions) {
                String type = tx.getIncome() == 1 ? "[+]" : "[-]";
                System.out.printf("%-15s %-20s %-15s %-12s %s$%-11.2f %-20s\n",
                    tx.getId(),
                    tx.getName().length() > 20 ? tx.getName().substring(0, 17) + "..." : tx.getName(),
                    tx.getCategoryId(),
                    tx.getAccountId(),
                    type,
                    tx.getAmount(),
                    tx.getCreateTime().length() > 20 ? tx.getCreateTime().substring(0, 19) : tx.getCreateTime());
            }
            
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println("Total transactions: " + transactions.size());
            
            double totalIncome = transactionService.getTotalIncome();
            double totalExpenses = transactionService.getTotalExpenses();
            System.out.printf("Total Income: $%,.2f\n", totalIncome);
            System.out.printf("Total Expenses: $%,.2f\n", totalExpenses);
            System.out.printf("Net: $%,.2f\n", totalIncome - totalExpenses);
            System.out.println("\n");
        }
    }
    
    /**
     * Handle View All Budgets
     */
    private void handleViewAllBudgets() {
        System.out.println("=== All Budgets ===");
        
        List<Budget> budgets = budgetService.readAll();
        
        if (budgets.isEmpty()) {
            System.out.println("No budgets found.");
        } else {
            System.out.println("\nBudgets:");
            System.out.println("--------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %12s %12s %-12s %-12s\n", 
                "ID", "Name", "Limit", "Balance", "Start Date", "End Date");
            System.out.println("--------------------------------------------------------------------------------------");
            
            for (Budget budget : budgets) {
                System.out.printf("%-15s %-20s $%,10.2f $%,10.2f %-12s %-12s\n",
                    budget.getId(),
                    budget.getName(),
                    budget.getLimitAmount(),
                    budget.getBalance(),
                    budget.getStartDate(),
                    budget.getEndDate());
            }
            
            System.out.println("--------------------------------------------------------------------------------------");
            System.out.println("Total budgets: " + budgets.size() + "\n");
        }
    }
    
    /**
     * Handle View All Goals
     */
    private void handleViewAllGoals() {
        System.out.println("=== All Goals ===");
        
        List<Goal> goals = goalService.readAll();
        
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
        } else {
            System.out.println("\nGoals:");
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %12s %12s %10s %-12s\n", 
                "ID", "Name", "Target", "Current", "Priority", "Deadline");
            System.out.println("-----------------------------------------------------------------------------------------");
            
            for (Goal goal : goals) {
                double progress = (goal.getBalance() / goal.getTarget()) * 100;
                System.out.printf("%-15s %-20s $%,10.2f $%,10.2f %9.1f %-12s (%.1f%%)\n",
                    goal.getId(),
                    goal.getName(),
                    goal.getTarget(),
                    goal.getBalance(),
                    goal.getPriority(),
                    goal.getDeadline(),
                    progress);
            }
            
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.println("Total goals: " + goals.size() + "\n");
        }
    }
    
    /**
     * Handle Add Transaction menu option
     */
    private void handleAddTransaction() {
        System.out.println("=== Add Transaction ===");

        // ID is auto-generated

        // Show all categories grouped by type with numbering
        var defaultCategories = categoryService.getDefaultCategories();
        System.out.println("Available Categories:");
        int idx = 1;
        System.out.println("Expense Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.EXPENSE) {
                System.out.printf("  %d. %s\n", idx, cat.getName());
            }
            idx++;
        }
        idx = 1;
        System.out.println("Income Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.INCOME) {
                System.out.printf("  %d. %s\n", idx, cat.getName());
            }
            idx++;
        }

        // User selects category
        Category selectedCategory = null;
        int selectedNum = -1;
        while (selectedCategory == null) {
            System.out.print("Select the category (number): ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= defaultCategories.size()) {
                    selectedCategory = defaultCategories.get(num - 1);
                    selectedNum = num;
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        String category = selectedCategory.getName();
        Category.Type chosenType = selectedCategory.getType();
        System.out.println("You selected: " + category);

        // Enter amount (expense) or income (income)
        double amount = 0.0;
        int income = 0;
        if (chosenType == Category.Type.EXPENSE) {
            System.out.print("Enter the amount: ");
            amount = Double.parseDouble(scanner.nextLine().trim());
            income = 0;
            System.out.println("Income set to 0 (expense).");
        } else {
            System.out.print("Enter the amount: ");
            amount = Double.parseDouble(scanner.nextLine().trim());
            income = 1;
            System.out.println("Income set to 1 (income).");
        }

        // Enter transaction name (description)
        System.out.print("Enter transaction name: ");
        String name = scanner.nextLine().trim();

        // Pick account by number
        String[] accounts = {"Wallet", "Bank"};
        System.out.println("Pick an account:");
        for (int i = 0; i < accounts.length; i++) {
            System.out.printf("  %d. %s\n", i + 1, accounts[i]);
        }
        String accountID = null;
        int accountNum = -1;
        while (accountID == null) {
            System.out.print("Enter the number of the account: ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= accounts.length) {
                    accountID = accounts[num - 1];
                    accountNum = num;
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        System.out.println("You selected: " + accountID);

        // Only generate timestamp if all inputs are valid
        String timestamp = java.time.LocalDateTime.now().toString();
        Transaction transaction = new Transaction(category, amount, name, income, accountID, timestamp);
        
        // Save to database using TransactionService
        transactionService.create(transaction);
        System.out.println("Transaction created: " + transaction.getName());
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
        System.out.println("1. View Account Summary");
        System.out.println("2. View All Transactions");
        System.out.println("3. Add Transaction");
        System.out.println("4. View All Budgets");
        System.out.println("5. Add Budget");
        System.out.println("6. View All Goals");
        System.out.println("7. Add Goal");
        System.out.println("8. View Reports");
        System.out.println("9. Exit");
    }

    /**
     * Handle Add Budget menu option
     */
    private void handleAddBudget() {
        System.out.println("=== Add Budget ===");

        // ID is auto-generated

        System.out.print("Enter budget name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter limit amount: ");
        double limits = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter starting balance: ");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();

        System.out.print("Enter tracked categories (comma-separated): ");
        String tracked = scanner.nextLine().trim();

        Budget budget = new Budget(name, limits, balance, startDate, endDate);
        
        // Save to database using BudgetService
        budgetService.create(budget);
        System.out.println("Budget created: " + budget.getName());
    }

    /**
     * Handle Add Goal menu option
     */
    private void handleAddGoal() {
        System.out.println("=== Add Goal ===");

        // ID is auto-generated

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
        String createAtInput = scanner.nextLine().trim();
        String createAt = createAtInput.isEmpty() ? java.time.LocalDateTime.now().toString() : createAtInput;

        Goal goal = new Goal(name, target, current, deadline, priority, createAt);
        
        // Save to database using GoalService
        goalService.create(goal);
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
