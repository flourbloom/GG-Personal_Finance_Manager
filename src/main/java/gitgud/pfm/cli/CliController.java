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
    private String currentAccountID = "default-account-id";
    
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
        this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
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
                    handleUpdateTransaction(currentAccountID, accountData);
                    break;
                case "5":
                    System.out.println("Delete Transaction feature not yet implemented.");
                    break;
                case "6":
                    handleViewAllBudgets();
                    break;
                case "7":
                    handleAddBudget();
                    break;
                case "8":
                    handleUpdateBudget(currentAccountID, accountData);
                    break;
                case "9":
                    System.out.println("Delete Budget feature not yet implemented.");
                    break;
                case "10":
                    handleViewAllGoals();
                    break;
                case "11":
                    handleAddGoal();
                    break;
                case "12":
                    handleUpdateGoal(currentAccountID, accountData);
                    break;
                case "13":
                    System.out.println("Delete Goal feature not yet implemented.");
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
            System.out.println("----------------------------------------");
            System.out.printf("%-15s %-20s %15s\n", "Account ID", "Name", "Balance");
            System.out.println("----------------------------------------");
            
            for (Wallet wallet : wallets) {
                System.out.printf("%-15s %-20s $%,14.2f\n", 
                    wallet.getId(), 
                    wallet.getName(), 
                    wallet.getBalance());
            }
            
            System.out.println("----------------------------------------");
            double totalBalance = walletService.getTotalBalance();
            System.out.printf("%-35s $%,14.2f\n", "Total Balance:", totalBalance);
            System.out.println("========================================\n");
        }
    }
    
    /**
     * Handle View All Transactions
     */
    private void handleViewAllTransactions() {
        System.out.println("=== All Transactions ===");
        
        List<Transaction> transactions = accountData.getTransactions();
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            // Create a map of category ID to category name
            Map<String, String> categoryMap = new HashMap<>();
            List<Category> categories = categoryService.getDefaultCategories();
            for (Category category : categories) {
                categoryMap.put(category.getId(), category.getName());
            }
            
            System.out.println("\nTransactions (most recent first):");
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %-15s %-12s $%-12s %-20s\n", 
                "ID", "Name", "Category", "Wallet", "Amount", "Date");
            System.out.println("--------------------------------------------------------------------------------------------");
            
            double totalIncome = 0.0;
            double totalExpenses = 0.0;
            
            for (Transaction tx : transactions) {
                String type = tx.getIncome() == 1 ? "[+]" : "[-]";
                String categoryName = categoryMap.getOrDefault(tx.getCategoryId(), tx.getCategoryId());
                System.out.printf("%-15s %-20s %-15s %-12s %s$%-11.2f %-20s\n",
                    tx.getId(),
                    tx.getName().length() > 20 ? tx.getName().substring(0, 17) + "..." : tx.getName(),
                    categoryName,
                    tx.getWalletId(),
                    type,
                    tx.getAmount(),
                    tx.getCreateTime().length() > 20 ? tx.getCreateTime().substring(0, 19) : tx.getCreateTime());
                
                if (tx.getIncome() == 1) {
                    totalIncome += tx.getAmount();
                } else {
                    totalExpenses += tx.getAmount();
                }
            }
            
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println("Total transactions: " + transactions.size());
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
        
        List<Budget> budgets = accountData.getBudgets();
        
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
        
        List<Goal> goals = accountData.getGoals();
        
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
        } else {
            System.out.println("\nGoals:");
            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %12s %12s %10s %-12s\n", 
                "ID", "Name", "Target", "Current", "Priority", "Deadline");
            System.out.println("-----------------------------------------------------------------------------------------");
            
            for (Goal goal : goals) {
                double progress = goal.getTarget() > 0 ? (goal.getBalance() / goal.getTarget()) * 100 : 0;
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

        // Show all categories grouped by type with continuous numbering
        var defaultCategories = categoryService.getDefaultCategories();
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

        // User selects category
        Category selectedCategory = null;
        while (selectedCategory == null) {
            System.out.print("Select the category (number): ");
            String input = scanner.nextLine().trim();
            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= defaultCategories.size()) {
                    selectedCategory = defaultCategories.get(num - 1);
                } else {
                    System.out.println("Invalid number. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        Category.Type chosenType = selectedCategory.getType();
        System.out.println("You selected: " + selectedCategory.getName());
        
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
            System.out.printf("  %d. %s (Balance: $%.2f)\n", i + 1, wallets.get(i).getName(), wallets.get(i).getBalance());
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

        // Only generate timestamp if all inputs are valid
        String timestamp = java.time.LocalDateTime.now().toString();
        String categoryId = selectedCategory.getId();
        Transaction transaction = new Transaction(categoryId, amount, name, income, walletId, timestamp);
        
        // Save to database using TransactionService
        transactionService.create(transaction);
        System.out.println("Transaction created: " + transaction.getName());
        
        // Refresh account data after creating transaction
        this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
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
        List<Category> categories = categoryService.getDefaultCategories();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
        
        System.out.printf("%-20s %10s %-20s %10s %-15s %20s%n", "Name", "Amount", "Categories", "Income", "Wallet ID", "Created At");
        for (Transaction t : accountData.getTransactions()) {
            String categoryName = categoryMap.getOrDefault(t.getCategoryId(), t.getCategoryId());
            System.out.printf("%-20s %10.2f %-20s %10.2f %-15s %20s%n",
                    t.getName(),
                    t.getAmount(),
                    categoryName,
                    t.getIncome(),
                    t.getWalletId(),
                    t.getCreateTime());
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
                    b.getLimitAmount(),
                    b.getBalance(),
                    b.getStartDate(),
                    b.getEndDate()//,
                    // TODO fix tracked categories display due to the new junction table
                    // b.getTrackedCategories()
                    );
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
        System.out.printf("%-20s %10s %10s %-12s %10s %20s%n", "Name", "Target", "Current", "Deadline", "Priority", "Created At");
        for (Goal g : accountData.getGoals()) {
            System.out.printf("%-20s %10.2f %10.2f %-12s %10.2f %20s%n",
                    g.getName(),
                    g.getTarget(),
                    g.getBalance(),
                    g.getDeadline(),
                    g.getPriority(),
                    g.getCreateTime());
        }
    }

    /* Update handlers: show view, ask for ID, ask which field, update in-memory and persist */
    private void handleUpdateTransaction(String accountID, AccountDataLoader.DataHolder accountData) {
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

        System.out.println("Fields: name, amount, category, income, walletid, createtime");
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
                    var defaultCategories = categoryService.getDefaultCategories();
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

            Map<String, Object> config = new HashMap<>();
            config.put("class", Transaction.class);
            config.put("table", "transaction_records");
            config.put("id", id);
            config.put("updates", updates);
            GenericSQLiteService.update(config);

            System.out.println("Transaction updated.");
            this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
        }
    }

    private void handleUpdateBudget(String accountID, AccountDataLoader.DataHolder accountData) {
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

        System.out.println("Fields: name, limitamount, balance, startdate, enddate");
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
                    // TODO: trackedCategories update handling
                // case "trackedCategoryIds":
                //     found.setTrackedCategoryIds(newValue);
                //     updates.put("trackedCategoryIds", newValue);
                //     break;
                default:
                    System.out.println("Unknown field.");
                    return;
            }

            Map<String, Object> config = new HashMap<>();
            config.put("class", Budget.class);
            config.put("table", "Budget");
            config.put("id", id);
            config.put("updates", updates);
            GenericSQLiteService.update(config);

            System.out.println("Budget updated.");
            this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
        }
    }

    private void handleUpdateGoal(String accountID, AccountDataLoader.DataHolder accountData) {
        System.out.println("=== Update Goal ===");
        handleViewGoals(accountData);
        System.out.print("Enter Goal name to update: ");
        String gname = scanner.nextLine().trim();

        Goal found = null;
        String id = null;
        for (Goal g : accountData.getGoals()) {
            if (g.getName() != null && g.getName().equals(gname)) {
                found = g;
                id = g.getId();
                break;
            }
        }

        if (found == null) {
            System.out.println("Goal not found.");
            return;
        }

        System.out.println("Fields: name, target, balance, deadline, priority, createat");
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
                    System.out.print("Enter new current amount: ");
                    double cur = Double.parseDouble(scanner.nextLine().trim());
                    found.setBalance(cur);
                    updates.put("balance", cur);
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
                case "createat":
                    System.out.print("Enter new creation time (YYYY-MM-DD): ");
                    String createTime = scanner.nextLine().trim();
                    found.setCreateTime(createTime);
                    updates.put("createAt", createTime);
                    break;
                default:
                    System.out.println("Unknown field.");
                    return;
            }

            Map<String, Object> config = new HashMap<>();
            config.put("class", Goal.class);
            config.put("table", "Goal");
            config.put("id", id);
            config.put("updates", updates);
            GenericSQLiteService.update(config);

            System.out.println("Goal updated.");
            this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid numeric value: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Failed to persist update: " + ex.getMessage());
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

    private void printMainMenu() {
        System.out.println("Main Menu:");
        System.out.println("1. View Account Summary");
        System.out.println("========================================");
        System.out.println("2. View All Transactions");
        System.out.println("3. Add Transaction");
        System.out.println("4. Edit Transaction");
        System.out.println("5. Delete Transaction (Not Implemented)");
        System.out.println("========================================");
        System.out.println("6. View All Budgets");
        System.out.println("7. Add Budget");
        System.out.println("8. Edit Budget");
        System.out.println("9. Delete Budget (Not Implemented)");
        System.out.println("========================================");
        System.out.println("10. View All Goals");
        System.out.println("11. Add Goal");
        System.out.println("12. Edit Goal");
        System.out.println("13. Delete Goal (Not Implemented)");
        System.out.println("========================================");
        System.out.println("17. View Reports");
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

        System.out.print("Enter tracked categories (comma-separated): ");
        String tracked = scanner.nextLine().trim();

        Budget budget = new Budget(name, limits, balance, startDate, endDate);
        
        // Save to database using BudgetService
        budgetService.create(budget);
        System.out.println("Budget created: " + budget.getName());
        
        // Refresh account data after creating budget
        this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
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
        String createAt = scanner.nextLine().trim();
        if (createAt.isEmpty()) {
            createAt = LocalDateTime.now().toString();
        }

        Goal goal = new Goal(name, target, current, deadline, priority, createAt);
        
        // Save to database using GoalService
        goalService.create(goal);
        System.out.println("Goal created: " + goal.getName());
        
        // Refresh account data after creating goal
        this.accountData = AccountDataLoader.loadAccountData(currentAccountID);
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
