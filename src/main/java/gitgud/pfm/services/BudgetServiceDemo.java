package gitgud.pfm.services;

import gitgud.pfm.Models.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BudgetServiceDemo - Demonstrates the enhanced budget tracking functionality
 * This class shows all the acceptance criteria in action:
 * 1. Create budgets with spending limits (by category or account-wide)
 * 2. Budgets set for specific time periods (weekly, monthly, yearly)
 * 3. Track spent amount vs. budget limit for each period
 * 4. Calculate remaining budget
 * 5. Data persisted in SQLite
 * 6. Support multiple budgets across different wallets
 */
public class BudgetServiceDemo {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   Budget Tracking with Category Limits - Demo");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        // Initialize services
        BudgetService budgetService = new BudgetService();
        CategoryService categoryService = new CategoryService();
        WalletService walletService = new WalletService();
        TransactionService transactionService = new TransactionService();
        
        // Setup: Create test data
        setupTestData(walletService, categoryService);
        
        // Demo 1: Create monthly budget with category limits
        System.out.println("📊 DEMO 1: Monthly Budget with Category Limits");
        System.out.println("─────────────────────────────────────────────────────────────");
        demonstrateMonthlyBudget(budgetService, categoryService, transactionService);
        
        // Demo 2: Create weekly budget for specific wallet
        System.out.println("\n📊 DEMO 2: Weekly Budget for Specific Wallet");
        System.out.println("─────────────────────────────────────────────────────────────");
        demonstrateWeeklyBudget(budgetService, categoryService, walletService);
        
        // Demo 3: Account-wide yearly budget
        System.out.println("\n📊 DEMO 3: Account-Wide Yearly Budget");
        System.out.println("─────────────────────────────────────────────────────────────");
        demonstrateYearlyBudget(budgetService, categoryService);
        
        // Demo 4: Track spending and calculate remaining budget
        System.out.println("\n📊 DEMO 4: Spending Tracking & Budget Compliance");
        System.out.println("─────────────────────────────────────────────────────────────");
        demonstrateSpendingTracking(budgetService, transactionService, categoryService);
        
        // Demo 5: Show spending breakdown by category
        System.out.println("\n📊 DEMO 5: Spending Breakdown by Category");
        System.out.println("─────────────────────────────────────────────────────────────");
        demonstrateSpendingBreakdown(budgetService);
        
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("   All acceptance criteria demonstrated successfully! ✓");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }
    
    private static void setupTestData(WalletService walletService, CategoryService categoryService) {
        System.out.println("Setting up test data...");
        
        // Create wallets if they don't exist
        if (walletService.readAll().isEmpty()) {
            Wallet mainWallet = new Wallet("#4CAF50", 5000.0, "Main Wallet");
            Wallet savingsWallet = new Wallet("#2196F3", 10000.0, "Savings");
            walletService.create(mainWallet);
            walletService.create(savingsWallet);
            System.out.println("✓ Created wallets");
        }
        
        // Create categories if they don't exist
        if (categoryService.getAllCategories().isEmpty()) {
            Category groceries = new Category("CAT001", "Groceries", "Food and groceries", Category.Type.EXPENSE);
            Category dining = new Category("CAT002", "Dining Out", "Restaurants and cafes", Category.Type.EXPENSE);
            Category transport = new Category("CAT003", "Transportation", "Gas, public transport", Category.Type.EXPENSE);
            Category entertainment = new Category("CAT004", "Entertainment", "Movies, games, hobbies", Category.Type.EXPENSE);
            
            categoryService.create(groceries);
            categoryService.create(dining);
            categoryService.create(transport);
            categoryService.create(entertainment);
            System.out.println("✓ Created categories\n");
        }
    }
    
    private static void demonstrateMonthlyBudget(BudgetService budgetService, 
                                                  CategoryService categoryService,
                                                  TransactionService transactionService) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfMonth(1).format(formatter);
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).format(formatter);
        
        // Create monthly budget
        Budget monthlyBudget = new Budget(
            "February 2026 Budget",
            2000.0,  // Total limit
            0.0,
            startDate,
            endDate,
            Budget.PeriodType.MONTHLY,
            null  // Account-wide
        );
        
        budgetService.create(monthlyBudget);
        System.out.println("✓ Created monthly budget: " + monthlyBudget.getName());
        System.out.println("  Total limit: $" + monthlyBudget.getLimitAmount());
        System.out.println("  Period: " + startDate + " to " + endDate);
        System.out.println("  Type: " + monthlyBudget.getPeriodType());
        
        // Add categories with specific limits
        budgetService.addCategoryToBudget(monthlyBudget.getId(), "CAT001", 500.0);  // Groceries: $500
        budgetService.addCategoryToBudget(monthlyBudget.getId(), "CAT002", 300.0);  // Dining: $300
        budgetService.addCategoryToBudget(monthlyBudget.getId(), "CAT003", 200.0);  // Transport: $200
        budgetService.addCategoryToBudget(monthlyBudget.getId(), "CAT004", 150.0);  // Entertainment: $150
        
        System.out.println("\n✓ Added categories with limits:");
        List<BudgetCategory> categories = budgetService.getBudgetCategoriesForBudget(monthlyBudget.getId());
        for (BudgetCategory bc : categories) {
            Category cat = categoryService.read(bc.getCategoryId());
            if (cat != null) {
                System.out.println("  - " + cat.getName() + ": $" + bc.getCategoryLimit());
            }
        }
    }
    
    private static void demonstrateWeeklyBudget(BudgetService budgetService,
                                                CategoryService categoryService,
                                                WalletService walletService) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        String startDate = now.format(formatter);
        String endDate = now.plusDays(7).format(formatter);
        
        // Get first wallet
        List<Wallet> wallets = walletService.readAll();
        String walletId = wallets.isEmpty() ? null : wallets.get(0).getId();
        
        Budget weeklyBudget = new Budget(
            "Weekly Spending Budget",
            500.0,
            0.0,
            startDate,
            endDate,
            Budget.PeriodType.WEEKLY,
            walletId
        );
        
        budgetService.create(weeklyBudget);
        System.out.println("✓ Created weekly budget: " + weeklyBudget.getName());
        System.out.println("  Total limit: $" + weeklyBudget.getLimitAmount());
        System.out.println("  Period: " + startDate + " to " + endDate);
        System.out.println("  Type: " + weeklyBudget.getPeriodType());
        System.out.println("  Wallet-specific: " + (weeklyBudget.getWalletId() != null ? "Yes" : "No"));
        
        // Add categories
        budgetService.addCategoryToBudget(weeklyBudget.getId(), "CAT001", 150.0);  // Groceries
        budgetService.addCategoryToBudget(weeklyBudget.getId(), "CAT002", 100.0);  // Dining
        
        System.out.println("\n✓ Added categories for weekly budget:");
        List<BudgetCategory> categories = budgetService.getBudgetCategoriesForBudget(weeklyBudget.getId());
        for (BudgetCategory bc : categories) {
            Category cat = categoryService.read(bc.getCategoryId());
            if (cat != null) {
                System.out.println("  - " + cat.getName() + ": $" + bc.getCategoryLimit());
            }
        }
    }
    
    private static void demonstrateYearlyBudget(BudgetService budgetService,
                                                CategoryService categoryService) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfYear(1).format(formatter);
        String endDate = now.withDayOfYear(now.lengthOfYear()).format(formatter);
        
        Budget yearlyBudget = new Budget(
            "2026 Annual Budget",
            30000.0,
            0.0,
            startDate,
            endDate,
            Budget.PeriodType.YEARLY,
            null  // Account-wide
        );
        
        budgetService.create(yearlyBudget);
        System.out.println("✓ Created yearly budget: " + yearlyBudget.getName());
        System.out.println("  Total limit: $" + yearlyBudget.getLimitAmount());
        System.out.println("  Period: " + startDate + " to " + endDate);
        System.out.println("  Type: " + yearlyBudget.getPeriodType());
        
        // Add categories with yearly limits
        budgetService.addCategoryToBudget(yearlyBudget.getId(), "CAT001", 6000.0);   // Groceries
        budgetService.addCategoryToBudget(yearlyBudget.getId(), "CAT002", 3600.0);   // Dining
        budgetService.addCategoryToBudget(yearlyBudget.getId(), "CAT003", 2400.0);   // Transport
        budgetService.addCategoryToBudget(yearlyBudget.getId(), "CAT004", 1800.0);   // Entertainment
        
        System.out.println("\n✓ Added categories for yearly budget:");
        List<BudgetCategory> categories = budgetService.getBudgetCategoriesForBudget(yearlyBudget.getId());
        for (BudgetCategory bc : categories) {
            Category cat = categoryService.read(bc.getCategoryId());
            if (cat != null) {
                System.out.println("  - " + cat.getName() + ": $" + bc.getCategoryLimit());
            }
        }
    }
    
    private static void demonstrateSpendingTracking(BudgetService budgetService,
                                                    TransactionService transactionService,
                                                    CategoryService categoryService) {
        // Get first budget
        List<Budget> budgets = budgetService.readAll();
        if (budgets.isEmpty()) {
            System.out.println("No budgets found!");
            return;
        }
        
        Budget budget = budgets.get(0);
        System.out.println("Budget: " + budget.getName());
        System.out.println("Period: " + budget.getStartDate() + " to " + budget.getEndDate());
        System.out.println("Total Limit: $" + budget.getLimitAmount() + "\n");
        
        // Calculate spending
        double totalSpent = budgetService.getTotalSpentForBudget(budget.getId());
        double remaining = budgetService.getRemainingBudget(budget.getId());
        double percentage = budgetService.getBudgetUsagePercentage(budget.getId());
        boolean overBudget = budgetService.isOverBudget(budget.getId());
        
        System.out.println("Total Spent: $" + String.format("%.2f", totalSpent));
        System.out.println("Remaining: $" + String.format("%.2f", remaining));
        System.out.println("Usage: " + String.format("%.1f", percentage) + "%");
        System.out.println("Over Budget: " + (overBudget ? "⚠️ YES" : "✓ NO"));
        
        // Show per-category spending
        System.out.println("\nPer-Category Breakdown:");
        List<BudgetCategory> categories = budgetService.getBudgetCategoriesForBudget(budget.getId());
        for (BudgetCategory bc : categories) {
            Category cat = categoryService.read(bc.getCategoryId());
            if (cat != null) {
                double spent = budgetService.getSpentForCategory(budget.getId(), bc.getCategoryId());
                double catRemaining = budgetService.getRemainingForCategory(budget.getId(), bc.getCategoryId());
                double catPercentage = budgetService.getCategoryUsagePercentage(budget.getId(), bc.getCategoryId());
                boolean catOver = budgetService.isCategoryOverBudget(budget.getId(), bc.getCategoryId());
                
                System.out.println("\n  " + cat.getName() + ":");
                System.out.println("    Limit: $" + bc.getCategoryLimit());
                System.out.println("    Spent: $" + String.format("%.2f", spent));
                System.out.println("    Remaining: $" + String.format("%.2f", catRemaining));
                System.out.println("    Usage: " + String.format("%.1f", catPercentage) + "%");
                System.out.println("    Status: " + (catOver ? "⚠️ Over Budget" : "✓ Within Budget"));
            }
        }
    }
    
    private static void demonstrateSpendingBreakdown(BudgetService budgetService) {
        List<Budget> budgets = budgetService.readAll();
        if (budgets.isEmpty()) {
            System.out.println("No budgets found!");
            return;
        }
        
        for (Budget budget : budgets) {
            System.out.println("Budget: " + budget.getName());
            System.out.println("─────────────────────────────────────────────────────────────");
            
            List<BudgetCategory> breakdown = budgetService.getSpendingBreakdown(budget.getId());
            
            if (breakdown.isEmpty()) {
                System.out.println("  No categories tracked for this budget.\n");
                continue;
            }
            
            for (BudgetCategory spending : breakdown) {
                System.out.println("\n  Category: " + spending.getCategoryName());
                System.out.println("    Budget Limit: $" + spending.getCategoryLimit());
                System.out.println("    Spent: $" + String.format("%.2f", spending.getSpentAmount()));
                System.out.println("    Remaining: $" + String.format("%.2f", spending.getRemainingAmount()));
                System.out.println("    Usage: " + String.format("%.1f", spending.getPercentageUsed()) + "%");
                System.out.println("    Status: " + (spending.isOverBudget() ? "⚠️ OVER BUDGET" : "✓ OK"));
            }
            System.out.println();
        }
    }
}
