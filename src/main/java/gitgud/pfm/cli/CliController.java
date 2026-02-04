package gitgud.pfm.cli;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Category;
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

    private AccountDataLoader.DataHolder accountData;

    // Services for create/update operations
    private final CategoryService categoryService = new CategoryService();
    private final WalletService walletService = new WalletService();
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
        initializeDefaultWallets();
        mainMenuLoop();
        shutdown();
    }

    /**
     * Initialize default wallets if none exist
     */
    private void initializeDefaultWallets() {
        List<Wallet> wallets = walletService.readAll();
        if (wallets.isEmpty()) {
            System.out.println("Initializing default wallets...");

            Wallet cashWallet = new Wallet("Green", 0.0, "Cash");
            Wallet cardWallet = new Wallet("Blue", 0.0, "Card");

            walletService.create(cashWallet);
            walletService.create(cardWallet);

            System.out.println("Default wallets created: Cash and Card\n");
        }
    }

    /**
     * Main menu loop - handles user input and navigation
     */
    private void mainMenuLoop() {
        this.accountData = AccountDataLoader.loadAccountData();
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
                    handleUpdateTransaction(accountData);
                    break;
                case "5":
                    handleDeleteTransaction(accountData);
                    break;
                case "6":
                    handleViewAllBudgets();
                    break;
                case "7":
                    handleAddBudget();
                    break;
                case "8":
                    handleUpdateBudget(accountData);
                    break;
                case "9":
                    handleDeleteBudget(accountData);
                    break;
                case "10":
                    handleViewAllGoals();
                    break;
                case "11":
                    handleAddGoal();
                    break;
                case "12":
                    handleUpdateGoal(accountData);
                    break;
                case "13":
                    handleDeleteGoal(accountData);
                    break;
                case "14":
                    handleDeleteTransaction(accountData);
                    break;
                case "15":
                    handleDeleteBudget(accountData);
                    break;
                case "17":
                    handleViewReports(accountData);
                    break;
                case "0":
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
     * Handle Account Summary - show all accounts with balances and total
     */
    private void handleAccountSummary() {
        System.out.println("=== Account Summary ===");

        List<Wallet> wallets = walletService.readAll();

        if (wallets.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            System.out.println("\nAccounts:");
            System.out.println("-".repeat(50));
            System.out.printf("%-20s %15s %-10s%n", "Name", "Balance", "Color");
            System.out.println("-".repeat(50));

            for (Wallet wallet : wallets) {
                System.out.printf("%-20s $%,14.2f %-10s%n",
                        truncate(wallet.getName(), 20),
                        wallet.getBalance(),
                        truncate(wallet.getColor(), 10));
            }

            System.out.println("-".repeat(50));
            double totalBalance = walletService.getTotalBalance();
            System.out.printf("%-20s $%,14.2f%n", "Total Balance:", totalBalance);
            System.out.println("=".repeat(50) + "\n");
        }
    }

    /**
     * Handle View All Transactions
     * Now displays goalId allocation status
     */
    private void handleViewAllTransactions() {
        System.out.println("=== All Transactions ===");

        List<Transaction> transactions = accountData.getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            // Create a map of category ID to category name
            Map<String, String> categoryMap = new HashMap<>();
            List<Category> categories = categoryService.getAllCategories();
            for (Category category : categories) {
                categoryMap.put(category.getId(), category.getName());
            }

            System.out.println("\nTransactions (most recent first):");
            System.out.println("-".repeat(95));
            System.out.printf("%-18s %-15s %-12s %10s %-8s %-12s %-12s%n",
                    "Name", "Category", "Wallet", "Amount", "Type", "Goal", "Date");
            System.out.println("-".repeat(95));

            double totalIncome = 0.0;
            double totalExpenses = 0.0;

            for (Transaction tx : transactions) {
                String type = tx.getIncome() == 1 ? "Income" : "Expense";
                String categoryName = categoryMap.getOrDefault(tx.getCategoryId(), tx.getCategoryId());
                String goalDisplay = tx.getGoalId() != null ? truncate(tx.getGoalId(), 12) : "-";
                String date = tx.getCreateTime() != null ? tx.getCreateTime().substring(0, Math.min(10, tx.getCreateTime().length())) : "";
                
                System.out.printf("%-18s %-15s %-12s $%,9.2f %-8s %-12s %-12s%n",
                        truncate(tx.getName(), 18),
                        truncate(categoryName, 15),
                        truncate(tx.getWalletId(), 12),
                        tx.getAmount(),
                        type,
                        goalDisplay,
                        date);

                if (tx.getIncome() == 1) {
                    totalIncome += tx.getAmount();
                } else {
                    totalExpenses += tx.getAmount();
                }
            }

            System.out.println("-".repeat(95));
            System.out.println("Total transactions: " + transactions.size());
            System.out.printf("Total Income: $%,.2f%n", totalIncome);
            System.out.printf("Total Expenses: $%,.2f%n", totalExpenses);
            System.out.printf("Net: $%,.2f%n", totalIncome - totalExpenses);
            System.out.println("\n");
        }
    }

    /**
     * Handle View All Budgets
     */
    private void handleViewAllBudgets() {
        System.out.println("=== All Budgets ===");

        List<Budget> budgets = accountData.getBudgets();

        if (budgets.isEmpty()) {
            System.out.println("No budgets found.");
        } else {
            System.out.println("\nBudgets:");
            System.out.println("-".repeat(90));
            System.out.printf("%-15s %12s %12s %12s %-10s %-10s %-18s%n",
                    "Name", "Limit", "Spent", "Remaining", "Start", "End", "Categories");
            System.out.println("-".repeat(90));

            for (Budget budget : budgets) {
                // Get tracked categories
                List<Category> trackedCategories = budgetService.getCategoriesForBudget(budget.getId());
                StringBuilder categoryNames = new StringBuilder();
                for (int i = 0; i < trackedCategories.size(); i++) {
                    if (i > 0) categoryNames.append(", ");
                    categoryNames.append(trackedCategories.get(i).getName());
                }
                String categoriesStr = categoryNames.length() > 0 ? categoryNames.toString() : "(none)";
                
                // Calculate actual spent amount
                double spent = budgetService.getTotalSpentForBudget(budget.getId());
                double remaining = budget.getLimitAmount() - spent;
                
                String startDate = budget.getStartDate() != null ? budget.getStartDate().substring(0, Math.min(10, budget.getStartDate().length())) : "";
                String endDate = budget.getEndDate() != null ? budget.getEndDate().substring(0, Math.min(10, budget.getEndDate().length())) : "";
                
                System.out.printf("%-15s $%,10.2f $%,10.2f $%,10.2f %-10s %-10s %-18s%n",
                        truncate(budget.getName(), 15),
                        budget.getLimitAmount(),
                        spent,
                        remaining,
                        startDate,
                        endDate,
                        truncate(categoriesStr, 18));
            }

            System.out.println("-".repeat(90));
            System.out.println("Total budgets: " + budgets.size() + "\n");
        }
    }

    /**
     * Handle View All Goals
     * Balance is computed from allocated transactions
     */
    private void handleViewAllGoals() {
        System.out.println("=== All Goals ===");

        List<Goal> goals = accountData.getGoals();

        if (goals.isEmpty()) {
            System.out.println("No goals found.");
        } else {
            System.out.println("\nGoals (Balance computed from allocated transactions):");
            System.out.println("-".repeat(85));
            System.out.printf("%-18s %12s %12s %-10s %8s %9s%n",
                "Name", "Target", "Current", "Deadline", "Priority", "Progress");
            System.out.println("-".repeat(85));

            for (Goal goal : goals) {
                int txCount = goal.getTxCount();
                double progress = goal.getProgress();
                String deadline = goal.getDeadline() != null ? goal.getDeadline().substring(0, Math.min(10, goal.getDeadline().length())) : "";
                
                System.out.printf("%-18s $%,10.2f $%,10.2f %-10s %8.1f %8.1f%%%n",
                    truncate(goal.getName(), 18),
                    goal.getTarget(),
                    goal.getBalance(),
                    deadline,
                    goal.getPriority(),
                    progress);
            }
            System.out.println("-".repeat(85));
            System.out.println("Total goals: " + goals.size());
            System.out.println("\n💡 Tip: Allocate transactions to goals when adding them (Option 3)\n");
        }
    }

    /**
     * Handle Add Transaction menu option
     */
/**
     * Handle Add Transaction menu option
     * Now includes "Goals" as a special category
     */
    private void handleAddTransaction() {
        System.out.println("=== Add Transaction ===");

        // ID is auto-generated

        // Show all categories grouped by type with continuous numbering
        var defaultCategories = categoryService.getAllCategories();
        System.out.println("Available Categories:");
        int categoryIndex = 1;

        System.out.println("Expense Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.EXPENSE) {
                System.out.printf("  %d. %s\n", categoryIndex, cat.getName());
                categoryIndex++;
            }
        }

        System.out.println("Income Categories:");
        for (Category cat : defaultCategories) {
            if (cat.getType() == Category.Type.INCOME) {
                System.out.printf("  %d. %s\n", categoryIndex, cat.getName());
                categoryIndex++;
            }
        }

        // Add "Goals" as a special category option
        int goalsOptionNumber = categoryIndex;
        System.out.println("Special Categories:");
        System.out.printf("  %d. Goals (Allocate to Savings Goal)\n", goalsOptionNumber);

        // User selects category
        Category selectedCategory = null;
        boolean isGoalCategory = false;
        while (selectedCategory == null && !isGoalCategory) {
            System.out.print("Select the category (number): ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                
                // Check if user selected "Goals"
                if (num == goalsOptionNumber) {
                    isGoalCategory = true;
                    System.out.println("You selected: Goals");
                } else if (num >= 1 && num <= defaultCategories.size()) {
                    selectedCategory = defaultCategories.get(num - 1);
                    System.out.println("You selected: " + selectedCategory.getName());
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        // If user selected Goals category, handle goal allocation
        if (isGoalCategory) {
            handleGoalAllocationTransaction();
            return;
        }

        // Continue with normal transaction flow
        Category.Type chosenType = selectedCategory.getType();

        // Enter transaction name (description)
        System.out.print("Enter transaction name: ");
        String name = scanner.nextLine().trim();

        // Enter amount (expense) or income (income)
        double amount = 0.0;
        int income = 0;
        System.out.print("Enter the amount: ");
        amount = Double.parseDouble(scanner.nextLine().trim());
        income = (chosenType == Category.Type.EXPENSE) ? 0 : 1;
        System.out.println("Income set to " + income + " (" + (income == 0 ? "expense" : "income") + ").");

        // Pick account by number
        List<Wallet> wallets = walletService.readAll();
        if (wallets.isEmpty()) {
            System.out.println("No wallets found. Cannot create transaction.");
            return;
        }
        System.out.println("Pick an account:");
        for (int i = 0; i < wallets.size(); i++) {
            System.out.printf("  %d. %s (Balance: $%.2f)\n", i + 1, wallets.get(i).getName(),
                    wallets.get(i).getBalance());
        }
        String walletId = null;
        while (walletId == null) {
            System.out.print("Enter the number of the account: ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= wallets.size()) {
                    walletId = wallets.get(num - 1).getId();
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        System.out.println("You selected: " + walletId);

        // Ask if user wants to allocate this transaction to a goal
        String goalId = null;
        System.out.print("\nAllocate this transaction to a goal? (y/n): ");
        String allocateToGoal = scanner.nextLine().trim().toLowerCase();
        
        if (allocateToGoal.equals("y")) {
            List<Goal> goals = accountData.getGoals();
            if (!goals.isEmpty()) {
                System.out.println("\nAvailable Goals:");
                System.out.println("─────────────────────────────────────────────────────────────────");
                System.out.printf("%-3s %-20s %-15s %-15s %10s\n", "#", "Name", "Target", "Current", "Progress");
                System.out.println("─────────────────────────────────────────────────────────────────");
                for (int i = 0; i < goals.size(); i++) {
                    Goal g = goals.get(i);
                    double progress = g.getTarget() > 0 ? (g.getBalance() / g.getTarget()) * 100 : 0;
                    System.out.printf("%-3d %-20s $%-14.2f $%-14.2f %9.1f%%\n",
                            i + 1,
                            g.getName(),
                            g.getTarget(),
                            g.getBalance(),
                            progress);
                }
                System.out.println("─────────────────────────────────────────────────────────────────");
                
                System.out.print("Select goal number (or 0 to skip): ");
                String goalInput = scanner.nextLine().trim();
                try {
                    int goalNum = Integer.parseInt(goalInput);
                    if (goalNum > 0 && goalNum <= goals.size()) {
                        goalId = goals.get(goalNum - 1).getId();
                        System.out.println("✓ Transaction will be allocated to: " + goals.get(goalNum - 1).getName());
                    }
                } catch (NumberFormatException e) {
                    // Skip goal allocation if invalid input
                }
            } else {
                System.out.println("No goals available. Skipping goal allocation.");
            }
        }

        // Only generate timestamp if all inputs are valid
        String timestamp = java.time.LocalDateTime.now().toString();
        String categoryId = selectedCategory.getId();
        Transaction transaction = new Transaction(categoryId, amount, name, income, walletId, timestamp);
        
        // Set goal ID if allocated
        if (goalId != null) {
            transaction.setGoalId(goalId);
        }

        // Save to database using TransactionService
        transactionService.create(transaction);
        System.out.println("\n✓ Transaction created: " + transaction.getName());
        
        // Show goal update message if allocated
        if (goalId != null) {
            Goal allocatedGoal = null;
            for (Goal g : accountData.getGoals()) {
                if (g.getId().equals(goalId)) {
                    allocatedGoal = g;
                    break;
                }
            }
            if (allocatedGoal != null) {
                System.out.println("\n=== Goal Updated ===");
                System.out.println("Goal: " + allocatedGoal.getName());
                double newBalance = allocatedGoal.getBalance() + amount;
                double progress = allocatedGoal.getTarget() > 0 ? (newBalance / allocatedGoal.getTarget()) * 100 : 0;
                System.out.println("New Balance: $" + String.format("%,.2f", newBalance) + " ← Automatically computed!");
                System.out.println("Target: $" + String.format("%,.2f", allocatedGoal.getTarget()));
                System.out.println("Progress: " + String.format("%.1f%%", progress));
                // Show simple progress bar
                int barLength = 30;
                int filled = (int) (progress / 100 * barLength);
                System.out.print("[");
                for (int i = 0; i < barLength; i++) {
                    System.out.print(i < filled ? "█" : "░");
                }
                System.out.println("]");
            }
        }

        // Refresh account data after creating transaction
        this.accountData = AccountDataLoader.loadAccountData();
    }

    /**
     * Handle Goal Allocation Transaction (when user selects "Goals" category)
     * This creates a transaction that transfers money from wallet to goal
     */
    private void handleGoalAllocationTransaction() {
        System.out.println("\n=== Allocate to Goal ===");
        
        // Step 1: Show all goals
        List<Goal> goals = accountData.getGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found. Please create a goal first (Option 11).");
            return;
        }
        
        System.out.println("\nAvailable Goals:");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-3s %-30s %-20s %14s %14s %8s %-12s %9s\n",
                "#", "ID", "Name", "Target ($)", "Current ($)", "TxCnt", "Deadline", "Progress");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        
        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            int txCount = g.getTxCount();
            double progress = g.getProgress();
            String nameDisplay = g.getName() != null && g.getName().length() > 20
                    ? g.getName().substring(0, 17) + "..."
                    : g.getName();
            System.out.printf("%-3d %-30s %-20s $%,12.2f $%,12.2f %8d %-12s %8.1f%%\n",
                    i + 1,
                    g.getId(),
                    nameDisplay,
                    g.getTarget(),
                    g.getBalance(),
                    txCount,
                    g.getDeadline(),
                    progress);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        
        // Step 2: Select goal
        Goal selectedGoal = null;
        while (selectedGoal == null) {
            System.out.print("\nSelect goal number (or 0 to cancel): ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num == 0) {
                    System.out.println("Cancelled.");
                    return;
                }
                if (num >= 1 && num <= goals.size()) {
                    selectedGoal = goals.get(num - 1);
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        
        System.out.println("\n✓ Selected Goal: " + selectedGoal.getName());
        System.out.println("  Current Balance: $" + String.format("%,.2f", selectedGoal.getBalance()));
        System.out.println("  Target: $" + String.format("%,.2f", selectedGoal.getTarget()));
        double remaining = selectedGoal.getTarget() - selectedGoal.getBalance();
        if (remaining > 0) {
            System.out.println("  Remaining: $" + String.format("%,.2f", remaining));
        } else {
            System.out.println("  ✓ Goal already reached!");
        }
        
        // Step 3: Select wallet
        List<Wallet> wallets = walletService.readAll();
        if (wallets.isEmpty()) {
            System.out.println("\nNo wallets found. Cannot create transaction.");
            return;
        }
        
        System.out.println("\nPick a wallet to transfer from:");
        for (int i = 0; i < wallets.size(); i++) {
            System.out.printf("  %d. %s (Balance: $%,.2f)\n", 
                    i + 1, 
                    wallets.get(i).getName(),
                    wallets.get(i).getBalance());
        }
        
        Wallet selectedWallet = null;
        while (selectedWallet == null) {
            System.out.print("Enter the number of the wallet: ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= wallets.size()) {
                    selectedWallet = wallets.get(num - 1);
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        System.out.println("✓ Selected: " + selectedWallet.getName());
        
        // Step 4: Enter amount
        double amount = 0.0;
        boolean validAmount = false;
        while (!validAmount) {
            System.out.print("\nEnter amount to allocate: $");
            String input = scanner.nextLine().trim();
            try {
                amount = Double.parseDouble(input);

                // Check if wallet has enough balance
                if (selectedWallet.getBalance() - amount < 0) {
                    System.out.println("❌ Insufficient wallet balance. Available: $" + 
                            String.format("%,.2f", selectedWallet.getBalance()));
                    continue;
                }
                
                validAmount = true;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        
        // Step 5: Create the transaction and update balances in SQL
        String timestamp = java.time.LocalDateTime.now().toString();
        String transactionName = "Goal: " + selectedGoal.getName();
        
        // Use first available category
        var categories = categoryService.getAllCategories();
        String categoryId = categories.isEmpty() ? "CAT_GOAL" : categories.get(0).getId();
        
        // Create transaction (expense - money leaving wallet)
        Transaction transaction = new Transaction(
                categoryId,
                amount,
                transactionName,
                0,  // income = 0 (expense - money leaving wallet)
                selectedWallet.getId(),
                timestamp
        );
        
        // Link to goal if your transaction table has goalId column
        transaction.setGoalId(selectedGoal.getId());
        
        // Save transaction to database
        transactionService.create(transaction);
        
        // Update wallet balance in database
        selectedWallet.subtractFromBalance(amount);
        walletService.update(selectedWallet);
        
        // Update goal balance in database (THIS IS THE KEY FIX!)
        selectedGoal.addToBalance(amount);
        goalService.update(selectedGoal);
        
        System.out.println("\n✓ Transaction created successfully!");
        System.out.println("$" + String.format("%,.2f", amount) + " allocated from " + 
                selectedWallet.getName() + " → " + selectedGoal.getName());
        
        // Refresh account data - AccountDataLoader will load updated values from SQL
        this.accountData = AccountDataLoader.loadAccountData();
        
        // Find updated goal to show progress
        Goal updatedGoal = null;
        for (Goal g : accountData.getGoals()) {
            if (g.getId().equals(selectedGoal.getId())) {
                updatedGoal = g;
                break;
            }
        }
        
        if (updatedGoal != null) {
            double newBalance = updatedGoal.getBalance();
            double newProgress = updatedGoal.getTarget() > 0 ? (newBalance / updatedGoal.getTarget()) * 100 : 0;
            
            System.out.println("\n=== Goal Updated ===");
            System.out.println("Goal: " + updatedGoal.getName());
            System.out.println("New Balance: $" + String.format("%,.2f", newBalance));
            System.out.println("Target: $" + String.format("%,.2f", updatedGoal.getTarget()));
            System.out.println("Progress: " + String.format("%.1f%%", newProgress));
            
            // Show progress bar
            int barLength = 30;
            int filled = (int) (newProgress / 100 * barLength);
            if (filled > barLength) filled = barLength;
            System.out.print("[");
            for (int i = 0; i < barLength; i++) {
                System.out.print(i < filled ? "█" : "░");
            }
            System.out.println("]");
            
            if (newProgress >= 100) {
                System.out.println("\n🎉 Congratulations! Goal reached!");
            }
        }
    }

    /**
     * Handle View Reports menu option
     * 
     * this.ID = ID;
     * this.Categories = Categories;
     * this.Amount = Amount;
     * this.Name = Name;
     * this.Income = Income;
     * this.WalletID = WalletID;
     * this.Create_time = Create_time;
     */
    private void handleViewReports(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== View Transaction Reports ===");

        // Create a map of category ID to category name
        Map<String, String> categoryMap = new HashMap<>();
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        System.out.printf("%-20s %10s %-15s %7s %-12s %-15s%n", "Name", "Amount", "Category", "Type", "Wallet", "Date");
        System.out.println("-".repeat(85));
        for (Transaction t : accountData.getTransactions()) {
            String categoryName = categoryMap.getOrDefault(t.getCategoryId(), t.getCategoryId());
            String type = t.getIncome() > 0 ? "Income" : "Expense";
            String date = t.getCreateTime() != null ? t.getCreateTime().substring(0, Math.min(10, t.getCreateTime().length())) : "";
            
            System.out.printf("%-20s $%9.2f %-15s %-7s %-12s %-15s%n",
                    truncate(t.getName(), 20),
                    t.getAmount(),
                    truncate(categoryName, 15),
                    type,
                    truncate(t.getWalletId(), 12),
                    date);
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
        List<Budget> budgets = accountData.getBudgets();
        
        if (budgets.isEmpty()) {
            System.out.println("No budgets found. Create a budget using option 7.");
            return;
        }
        
        System.out.printf("%-15s %12s %12s %12s %-10s %-10s %-25s%n", "Name", "Limit", "Spent", "Remaining", "Start", "End", "Categories");
        System.out.println("-".repeat(105));
        
        for (Budget b : budgets) {
            // Get tracked categories for this budget
            List<Category> trackedCategories = budgetService.getCategoriesForBudget(b.getId());
            StringBuilder categoryNames = new StringBuilder();
            for (int i = 0; i < trackedCategories.size(); i++) {
                if (i > 0) categoryNames.append(", ");
                categoryNames.append(trackedCategories.get(i).getName());
            }
            String categoriesStr = categoryNames.length() > 0 ? categoryNames.toString() : "(none)";
            
            // Calculate actual spent amount from tracked category transactions
            double spent = budgetService.getTotalSpentForBudget(b.getId());
            double remaining = b.getLimitAmount() - spent;
            
            String startDate = b.getStartDate() != null ? b.getStartDate().substring(0, Math.min(10, b.getStartDate().length())) : "";
            String endDate = b.getEndDate() != null ? b.getEndDate().substring(0, Math.min(10, b.getEndDate().length())) : "";
            
            System.out.printf("%-15s $%,10.2f $%,10.2f $%,10.2f %-10s %-10s %-25s%n",
                truncate(b.getName(), 15),
                b.getLimitAmount(),
                spent,
                remaining,
                startDate,
                endDate,
                truncate(categoriesStr, 25));
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
     * private String createTime;
     */
    private void handleViewGoals(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== View Goals ===");
        System.out.printf("%-15s %12s %12s %-10s %8s %-15s%n", "Name", "Target", "Current", "Deadline", "Priority", "Created");
        System.out.println("-".repeat(80));
        for (Goal g : accountData.getGoals()) {
            String deadline = g.getDeadline() != null ? g.getDeadline().substring(0, Math.min(10, g.getDeadline().length())) : "";
            String created = g.getCreateTime() != null ? g.getCreateTime().substring(0, Math.min(10, g.getCreateTime().length())) : "";
            
            System.out.printf("%-15s $%,10.2f $%,10.2f %-10s %8.1f %-15s%n",
                    truncate(g.getName(), 15),
                    g.getTarget(),
                    g.getBalance(),
                    deadline,
                    g.getPriority(),
                    created);
        }
    }

    /*
     * Update handlers: show view, ask for ID, ask which field, update in-memory and
     * persist
     */
    private void handleUpdateTransaction(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Update Transaction ===");
        handleViewReports(accountData);
        System.out.print("Enter Transaction Name to update: ");
        String name = scanner.nextLine().trim();

        Transaction found = null;
        String id = null;
        for (Transaction t : accountData.getTransactions()) {
            if (t.getName() != null && t.getName().equals(name)) {
                found = t;
                id = t.getId();
                break;
            }
        }

        if (found == null) {
            System.out.println("Transaction not found.");
            return;
        }

        System.out.println("Fields: name, amount, category, income, walletid, goalid, createtime");
        System.out.print("Enter field to update: ");
        String field = scanner.nextLine().trim().toLowerCase();

        Map<String, Object> updates = new HashMap<>();

        try {
            switch (field) {
                case "name":
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine().trim();
                    found.setName(newName);
                    updates.put("name", newName);
                    break;
                case "amount":
                    System.out.print("Enter new amount: ");
                    double amt = Double.parseDouble(scanner.nextLine().trim());
                    found.setAmount(amt);
                    updates.put("amount", amt);
                    break;
                case "category":
                    var defaultCategories = categoryService.getAllCategories();
                    System.out.println("\nAvailable Categories:");
                    int categoryIndex = 1;

                    System.out.println("Expense Categories:");
                    for (Category cat : defaultCategories) {
                        if (cat.getType() == Category.Type.EXPENSE) {
                            System.out.printf("  %d. %s\n", categoryIndex, cat.getName());
                            categoryIndex++;
                        }
                    }

                    System.out.println("Income Categories:");
                    for (Category cat : defaultCategories) {
                        if (cat.getType() == Category.Type.INCOME) {
                            System.out.printf("  %d. %s\n", categoryIndex, cat.getName());
                            categoryIndex++;
                        }
                    }

                    Category selectedCategory = null;
                    while (selectedCategory == null) {
                        System.out.print("Select the category (number): ");
                        String categoryInput = scanner.nextLine().trim();
                        try {
                            int num = Integer.parseInt(categoryInput);
                            if (num >= 1 && num <= defaultCategories.size()) {
                                selectedCategory = defaultCategories.get(num - 1);
                            } else {
                                System.out.println("Invalid number. Try again.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid number.");
                        }
                    }
                    String newCategoryId = selectedCategory.getId();
                    found.setCategoryId(newCategoryId);
                    updates.put("categoryId", newCategoryId);
                    System.out.println("You selected: " + selectedCategory.getName());
                    break;
                case "income":
                    System.out.print("Enter new income value (0=expense, 1=income): ");
                    double inc = Double.parseDouble(scanner.nextLine().trim());
                    found.setIncome(inc);
                    updates.put("income", inc);
                    break;
                case "walletid":
                    System.out.print("Enter new wallet ID: ");
                    String walletId = scanner.nextLine().trim();
                    found.setWalletId(walletId);
                    updates.put("walletId", walletId);
                    break;
                case "goalid":
                    List<Goal> goals = accountData.getGoals();
                    if (!goals.isEmpty()) {
                        System.out.println("\nAvailable Goals (or enter 'null' to unlink):");
                        for (int i = 0; i < goals.size(); i++) {
                            Goal g = goals.get(i);
                            double progress = g.getTarget() > 0 ? (g.getBalance() / g.getTarget()) * 100 : 0;
                            System.out.printf("  %d. %s (Balance: $%.2f / Target: $%.2f)\n",
                                    i + 1, g.getName(), g.getBalance(), g.getTarget());
                        }
                        System.out.print("Enter goal number (or 'null' to unlink): ");
                        String goalInput = scanner.nextLine().trim();
                        if (goalInput.equalsIgnoreCase("null")) {
                            found.setGoalId(null);
                            updates.put("goalId", null);
                            System.out.println("Transaction unlinked from goal.");
                        } else {
                            try {
                                int goalNum = Integer.parseInt(goalInput);
                                if (goalNum > 0 && goalNum <= goals.size()) {
                                    String newGoalId = goals.get(goalNum - 1).getId();
                                    found.setGoalId(newGoalId);
                                    updates.put("goalId", newGoalId);
                                    System.out.println("You selected: " + goals.get(goalNum - 1).getName());
                                } else {
                                    System.out.println("Invalid goal number.");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid number or 'null'.");
                                return;
                            }
                        }
                    } else {
                        System.out.println("No goals available. Cannot allocate transaction.");
                        return;
                    }
                    break;
                case "createtime":
                    System.out.print("Enter new create time: ");
                    String createTime = scanner.nextLine().trim();
                    found.setCreateTime(createTime);
                    updates.put("createTime", createTime);
                    break;
                default:
                    System.out.println("Unknown field.");
                    return;
            }

            // Persist update via TransactionService
            transactionService.update(found);

            System.out.println("Transaction updated.");
            this.accountData = AccountDataLoader.loadAccountData();
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
        }
    }

    private void handleUpdateBudget(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Update Budget ===");
        handleViewBudgets(accountData);
        System.out.print("Enter Budget name to update: ");
        String bname = scanner.nextLine().trim();

        Budget found = null;
        String id = null;
        for (Budget b : accountData.getBudgets()) {
            if (b.getName() != null && b.getName().equals(bname)) {
                found = b;
                id = b.getId();
                break;
            }
        }

        if (found == null) {
            System.out.println("Budget not found.");
            return;
        }

        System.out.println("Fields: name, limitamount, balance, startdate, enddate, categories");
        System.out.print("Enter field to update: ");
        String field = scanner.nextLine().trim().toLowerCase();

        Map<String, Object> updates = new HashMap<>();

        try {
            switch (field) {
                case "name":
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine().trim();
                    found.setName(newName);
                    updates.put("name", newName);
                    break;
                case "limitamount":
                    System.out.print("Enter new limit amount: ");
                    double lim = Double.parseDouble(scanner.nextLine().trim());
                    found.setLimitAmount(lim);
                    updates.put("limitAmount", lim);
                    break;
                case "balance":
                    System.out.print("Enter new balance: ");
                    double bal = Double.parseDouble(scanner.nextLine().trim());
                    found.setBalance(bal);
                    updates.put("balance", bal);
                    break;
                case "startdate":
                    System.out.print("Enter new start date (YYYY-MM-DD): ");
                    String startDate = scanner.nextLine().trim();
                    found.setStartDate(startDate);
                    updates.put("startDate", startDate);
                    break;
                case "enddate":
                    System.out.print("Enter new end date (YYYY-MM-DD): ");
                    String endDate = scanner.nextLine().trim();
                    found.setEndDate(endDate);
                    updates.put("endDate", endDate);
                    break;
                case "categories":
                    // Show available categories
                    System.out.println("\nAvailable Categories:");
                    List<Category> categories = categoryService.getAllCategories();
                    for (int i = 0; i < categories.size(); i++) {
                        Category cat = categories.get(i);
                        boolean isLinked = budgetService.isCategoryInBudget(found.getId(), cat.getId());
                        String marker = isLinked ? " [TRACKED]" : "";
                        System.out.println("  " + (i + 1) + ". " + cat.getName() + " (" + cat.getType() + ")" + marker);
                    }
                    
                    System.out.print("\nEnter category numbers to track (comma-separated, replaces existing): ");
                    String tracked = scanner.nextLine().trim();
                    
                    if (!tracked.isEmpty()) {
                        List<String> categoryIds = new java.util.ArrayList<>();
                        String[] selections = tracked.split(",");
                        for (String selection : selections) {
                            try {
                                int index = Integer.parseInt(selection.trim()) - 1;
                                if (index >= 0 && index < categories.size()) {
                                    categoryIds.add(categories.get(index).getId());
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("  Invalid selection: " + selection);
                            }
                        }
                        budgetService.setCategoriesForBudget(found.getId(), categoryIds);
                        System.out.println("Categories updated for budget.");
                    } else {
                        budgetService.removeAllCategoriesFromBudget(found.getId());
                        System.out.println("All categories removed from budget.");
                    }
                    this.accountData = AccountDataLoader.loadAccountData();
                    return; // Categories handled separately
                default:
                    System.out.println("Unknown field.");
                    return;
            }

            // Persist update via BudgetService
            budgetService.update(found);

            System.out.println("Budget updated.");
            this.accountData = AccountDataLoader.loadAccountData();
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
        }
    }

    private void handleUpdateGoal(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Update Goal ===");

        // Show numbered goals so user can select by number
        List<Goal> goals = accountData.getGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found. Please create a goal first (Option 11).");
            return;
        }

        System.out.println("Available Goals:");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-3s %-10s %-25s %12s %12s %6s %8s %-12s %-20s\n",
            "#", "ID", "Name", "Target", "Current", "TxCnt", "Priority", "Deadline", "Created At");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            int txCount = g.getTxCount();
            String nameDisplay = g.getName() != null && g.getName().length() > 25 ? g.getName().substring(0, 22) + "..." : g.getName();
            System.out.printf("%-3d %-10s %-25s $%,10.2f $%,10.2f %6d %8.1f %-12s %-20s\n",
                i + 1,
                g.getId(),
                nameDisplay,
                g.getTarget(),
                g.getBalance(),
                txCount,
                g.getPriority(),
                g.getDeadline(),
                g.getCreateTime());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------");

        Goal found = null;
        while (found == null) {
            System.out.print("Select goal number to update (or 0 to cancel): ");
            String sel = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(sel);
                if (num == 0) {
                    System.out.println("Cancelled.");
                    return;
                }
                if (num >= 1 && num <= goals.size()) {
                    found = goals.get(num - 1);
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        System.out.println("\nSelected: " + found.getName());
        System.out.println("Fields you can edit: name, target, balance, deadline, priority");
        System.out.print("Enter field to update: ");
        String field = scanner.nextLine().trim().toLowerCase();

        Map<String, Object> updates = new HashMap<>();

        try {
            switch (field) {
                case "name":
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine().trim();
                    found.setName(newName);
                    updates.put("name", newName);
                    break;
                case "target":
                    System.out.print("Enter new target amount: ");
                    double tar = Double.parseDouble(scanner.nextLine().trim());
                    found.setTarget(tar);
                    updates.put("target", tar);
                    break;
                case "balance":
                    System.out.print("Enter new balance amount: ");
                    double newBal = Double.parseDouble(scanner.nextLine().trim());
                    found.setBalance(newBal);
                    updates.put("balance", newBal);
                    break;
                case "deadline":
                    System.out.print("Enter new deadline (YYYY-MM-DD): ");
                    String deadline = scanner.nextLine().trim();
                    found.setDeadline(deadline);
                    updates.put("deadline", deadline);
                    break;
                case "priority":
                    System.out.print("Enter new priority (numeric): ");
                    double pr = Double.parseDouble(scanner.nextLine().trim());
                    found.setPriority(pr);
                    updates.put("priority", pr);
                    break;
                
                default:
                    System.out.println("Unknown field.");
                    return;
            }

            // Persist update via GoalService
            goalService.update(found);

            System.out.println("Goal updated.");
            this.accountData = AccountDataLoader.loadAccountData();
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
        }
    }

    /*
     * Delete handlers: ask for name, delete row from DB and remove from in-memory
     * list
     */
    private void handleDeleteTransaction(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Delete Transaction ===");
        System.out.println("Available Transactions:");
        handleViewReports(accountData);
        System.out.print("\nEnter the name of the Transaction you want to delete: ");
        String transactionNameInput = scanner.nextLine().trim();

        Transaction transactionToDelete = null;
        for (Transaction t : accountData.getTransactions()) {
            if (t.getName() != null && t.getName().equals(transactionNameInput)) {
                transactionToDelete = t;
                break;
            }
        }

        if (transactionToDelete == null) {
            System.out.println("ERROR: Transaction '" + transactionNameInput + "' not found.");
            return;
        }

        String transactionId = transactionToDelete.getId();
        System.out.println("\nConfirming deletion of Transaction: " + transactionToDelete.getName() +
                " (Amount: " + transactionToDelete.getAmount() + ")");

        try {
            // Persist delete via TransactionService
            transactionService.delete(transactionId);
            accountData.getTransactions().remove(transactionToDelete);
            System.out.println("SUCCESS: Transaction '" + transactionToDelete.getName() + "' has been deleted successfully.");
            // refresh in-memory data to reflect persisted changes
            refreshDataHolder(accountData);
        } catch (Exception ex) {
            System.out.println("ERROR: Failed to delete transaction: " + ex.getMessage());
        }
    }

    private void handleDeleteBudget(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Delete Budget ===");
        System.out.println("Available Budgets:");
        handleViewBudgets(accountData);
        System.out.print("\nEnter the name of the Budget you want to delete: ");
        String budgetNameInput = scanner.nextLine().trim();

        Budget budgetToDelete = null;
        for (Budget b : accountData.getBudgets()) {
            if (b.getName() != null && b.getName().equals(budgetNameInput)) {
                budgetToDelete = b;
                break;
            }
        }

        if (budgetToDelete == null) {
            System.out.println("ERROR: Budget '" + budgetNameInput + "' not found.");
            return;
        }

        String budgetId = budgetToDelete.getId();
        System.out.println("\nConfirming deletion of Budget: " + budgetToDelete.getName() +
                " (Limit: " + budgetToDelete.getLimitAmount() + ")");

        try {
            // Persist delete via BudgetService
            budgetService.delete(budgetId);
            accountData.getBudgets().remove(budgetToDelete);
            System.out.println("SUCCESS: Budget '" + budgetToDelete.getName() + "' has been deleted successfully.");
            // refresh in-memory data to reflect persisted changes
            refreshDataHolder(accountData);
        } catch (Exception ex) {
            System.out.println("ERROR: Failed to delete budget: " + ex.getMessage());
        }
    }

    private void handleDeleteGoal(AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Delete Goal ===");

        List<Goal> goals = accountData.getGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return;
        }

        System.out.println("Available Goals:");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-3s %-10s %-25s %12s %12s %6s %8s %-12s %-20s\n",
                "#", "ID", "Name", "Target", "Current", "TxCnt", "Priority", "Deadline", "Created At");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            int txCount = g.getTxCount();
            String nameDisplay = g.getName() != null && g.getName().length() > 25 ? g.getName().substring(0, 22) + "..." : g.getName();
            System.out.printf("%-3d %-10s %-25s $%,10.2f $%,10.2f %6d %8.1f %-12s %-20s\n",
                    i + 1,
                    g.getId(),
                    nameDisplay,
                    g.getTarget(),
                    g.getBalance(),
                    txCount,
                    g.getPriority(),
                    g.getDeadline(),
                    g.getCreateTime());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------");

        Goal goalToDelete = null;
        while (goalToDelete == null) {
            System.out.print("Select goal number to delete (or 0 to cancel): ");
            String sel = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(sel);
                if (num == 0) {
                    System.out.println("Cancelled.");
                    return;
                }
                if (num >= 1 && num <= goals.size()) {
                    goalToDelete = goals.get(num - 1);
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        String goalId = goalToDelete.getId();

        // Count transactions allocated to this goal (in-memory)
        int allocatedTxCount = 0;
        for (Transaction tx : accountData.getTransactions()) {
            if (tx.getGoalId() != null && tx.getGoalId().equals(goalId)) {
                allocatedTxCount++;
            }
        }

        if (allocatedTxCount > 0) {
            System.out.println("\n⚠️  WARNING: This goal has " + allocatedTxCount + " transaction(s) allocated to it.");
            System.out.print("These transactions will be unlinked in-memory. Continue and delete goal? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }
        } else {
            System.out.print("Confirm delete '" + goalToDelete.getName() + "' (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }
        }

        try {
            // Persist delete via GoalService
            goalService.delete(goalId);

            // Unlink any in-memory transaction references to this goal
            for (Transaction tx : accountData.getTransactions()) {
                if (tx.getGoalId() != null && tx.getGoalId().equals(goalId)) {
                    tx.setGoalId(null);
                }
            }

            // Remove from in-memory goals list
            accountData.getGoals().remove(goalToDelete);

            System.out.println("SUCCESS: Goal '" + goalToDelete.getName() + "' has been deleted successfully.");

            // Refresh in-memory data from DB
            refreshDataHolder(accountData);
        } catch (Exception ex) {
            System.out.println("ERROR: Failed to delete goal: " + ex.getMessage());
        }
    }

    /**
     * Reloads account data from DB and updates the passed DataHolder in-place.
     */
    private void refreshDataHolder(AccountDataLoader.DataHolder accountData) {
        AccountDataLoader.DataHolder fresh = AccountDataLoader.loadAccountData();
        if (fresh == null)
            return;

        accountData.setBudgets(fresh.getBudgets());
        accountData.setGoals(fresh.getGoals());
        accountData.setTransactions(fresh.getTransactions());
        accountData.setWallets(fresh.getWallets());
    }

    /**
     * Persistence placeholders - replace with real DB persistence implementation
     * later.
     */
    private void persistUpdate(Map<String, Object> config) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("persistUpdate not implemented");
    }

    private void persistDelete(Map<String, Object> config) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("persistDelete not implemented");
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
        System.out.println("========================================");
        System.out.println("2. View All Transactions");
        System.out.println("3. Add Transaction");
        System.out.println("4. Edit Transaction");
        System.out.println("5. Delete Transaction");
        System.out.println("========================================");
        System.out.println("6. View All Budgets");
        System.out.println("7. Add Budget");
        System.out.println("8. Edit Budget");
        System.out.println("9. Delete Budget");
        System.out.println("========================================");
        System.out.println("10. View All Goals");
        System.out.println("11. Add Goal");
        System.out.println("12. Edit Goal");
        System.out.println("13. Delete Goal");
        System.out.println("========================================");
        System.out.println("14. View Reports");
        System.out.println("========================================");
        System.out.println("0. Exit");
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

        // Balance represents current spent amount
        double balance = 0.0;

        // TODO choose whether to set start date to now or custom
        System.out.print("Enter start date (YYYY-MM-DD leave blank for now): ");
        String startDate = scanner.nextLine().trim();
        if (startDate.isEmpty()) {
            startDate = LocalDateTime.now().toString();
        }

        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();

        // Display available categories for user to select
        System.out.println("\nAvailable Categories:");
        List<Category> categories = categoryService.getAllCategories();
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            System.out.println("  " + (i + 1) + ". " + cat.getName() + " (" + cat.getType() + ")");
        }
        
        System.out.print("\nEnter category numbers to track (comma-separated, e.g., 1,3,5): ");
        String tracked = scanner.nextLine().trim();

        Budget budget = new Budget(name, limits, balance, startDate, endDate);

        // Save to database using BudgetService
        budgetService.create(budget);
        
        // Link selected categories to the budget
        if (!tracked.isEmpty()) {
            String[] categorySelections = tracked.split(",");
            for (String selection : categorySelections) {
                try {
                    int index = Integer.parseInt(selection.trim()) - 1;
                    if (index >= 0 && index < categories.size()) {
                        Category selectedCategory = categories.get(index);
                        budgetService.addCategoryToBudget(budget.getId(), selectedCategory.getId());
                        System.out.println("  Linked category: " + selectedCategory.getName());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Invalid selection: " + selection);
                }
            }
        }
        
        System.out.println("Budget created: " + budget.getName());

        // Refresh account data after creating budget
        this.accountData = AccountDataLoader.loadAccountData();
    }

    /**
     * Handle Add Goal menu option
     * Note: Balance is computed from allocated transactions, not manually set
     */
    private void handleAddGoal() {
        System.out.println("=== Add Goal ===");

        // ID is auto-generated

        System.out.print("Enter goal name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter target amount: ");
        double target = Double.parseDouble(scanner.nextLine().trim());

        // REMOVED: Enter current amount - balance is computed from transactions

        System.out.print("Enter deadline (YYYY-MM-DD): ");
        String deadline = scanner.nextLine().trim();

        System.out.print("Enter priority (numeric): ");
        double priority = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter creation time (YYYY-MM-DD or leave blank for now): ");
        String createAt = scanner.nextLine().trim();
        if (createAt.isEmpty()) {
            createAt = LocalDateTime.now().toString();
        }

        // Create goal without current amount - balance starts at $0.00 (computed from transactions)
        Goal goal = new Goal(name, target, deadline, priority, createAt);

        // Save to database using GoalService
        goalService.create(goal);
        System.out.println("Goal created: " + goal.getName());
        System.out.println("Target: $" + String.format("%,.2f", target));
        System.out.println("Current Balance: $0.00 (will update as transactions are allocated)");

        // Refresh account data after creating goal
        this.accountData = AccountDataLoader.loadAccountData();
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

    /**
     * Truncate text to specified length with ellipsis
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
