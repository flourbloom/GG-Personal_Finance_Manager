package gitgud.pfm.services;

import java.util.ArrayList;
import java.util.List;
 
import gitgud.pfm.Models.*;

/**
 * AccountDataLoader - Centralized data management with singleton pattern.
 * Provides CRUD operations for all entities and observer pattern for UI refresh.
 */
public class AccountDataLoader {

    // Singleton instance
    private static AccountDataLoader instance;
    
    // Services
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final GoalService goalService;
    private final BudgetService budgetService;
    
    // Refresh listeners (observer pattern)
    private final List<Runnable> walletRefreshListeners = new ArrayList<>();
    private final List<Runnable> goalRefreshListeners = new ArrayList<>();
    private final List<Runnable> budgetRefreshListeners = new ArrayList<>();
    
    // Private constructor for singleton
    private AccountDataLoader() {
        this.walletService = new WalletService();
        this.transactionService = new TransactionService();
        this.goalService = new GoalService();
        this.budgetService = new BudgetService();
    }
    
    // Singleton getInstance
    public static synchronized AccountDataLoader getInstance() {
        if (instance == null) {
            instance = new AccountDataLoader();
        }
        return instance;
    }

    // ==================== WALLET OPERATIONS ====================
    
    public List<Wallet> getWallets() {
        try {
            return walletService.readAll();
        } catch (Exception e) {
            System.err.println("Error reading wallets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Wallet getWalletById(String walletId) {
        try {
            return walletService.read(walletId);
        } catch (Exception e) {
            System.err.println("Error reading wallet: " + e.getMessage());
            return null;
        }
    }
    
    public void addWallet(Wallet wallet) {
        walletService.create(wallet);
    }
    
    public void updateWallet(Wallet wallet) {
        walletService.update(wallet);
    }
    
    public void deleteWallet(String walletId) {
        walletService.delete(walletId);
    }
    
    // ==================== TRANSACTION OPERATIONS ====================
    
    public List<Transaction> getTransactions() {
        try {
            return transactionService.readAll();
        } catch (Exception e) {
            System.err.println("Error reading transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addTransaction(Transaction transaction) {
        transactionService.create(transaction);
    }
    
    public void updateTransaction(Transaction transaction) {
        transactionService.update(transaction);
    }
    
    public void deleteTransaction(String transactionId) {
        transactionService.delete(transactionId);
    }
    
    // ==================== GOAL OPERATIONS ====================
    
    public List<Goal> getGoals() {
        try {
            return goalService.readAll();
        } catch (Exception e) {
            System.err.println("Error reading goals: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addGoal(Goal goal) {
        goalService.create(goal);
    }
    
    public void updateGoal(Goal goal) {
        goalService.update(goal);
    }
    
    public void deleteGoal(String goalId) {
        goalService.delete(goalId);
    }
    
    // ==================== BUDGET OPERATIONS ====================
    
    public List<Budget> getBudgets() {
        try {
            return budgetService.readAll();
        } catch (Exception e) {
            System.err.println("Error reading budgets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addBudget(Budget budget) {
        budgetService.create(budget);
    }
    
    public void addBudgetWithCategories(Budget budget, List<String> categoryIds) {
        budgetService.create(budget);
        if (categoryIds != null) {
            for (String categoryId : categoryIds) {
                budgetService.addCategoryToBudget(budget.getId(), categoryId);
            }
        }
    }
    
    public void updateBudget(Budget budget) {
        budgetService.update(budget);
    }
    
    public void updateBudgetWithCategories(Budget budget, List<String> categoryIds) {
        budgetService.update(budget);
        // Clear existing categories and add new ones
        budgetService.setCategoriesForBudget(budget.getId(), categoryIds != null ? categoryIds : new ArrayList<>());
    }
    
    public void deleteBudget(String budgetId) {
        budgetService.delete(budgetId);
    }
    
    // ==================== UTILITY METHODS ====================
    
    public double getTotalExpenses() {
        return getTransactions().stream()
                .filter(t -> t.getIncome() == 0) // expenses have income = 0
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getTotalIncome() {
        return getTransactions().stream()
                .filter(t -> t.getIncome() > 0)
                .mapToDouble(Transaction::getIncome)
                .sum();
    }
    
    // ==================== REFRESH LISTENERS (Observer Pattern) ====================
    
    public void addWalletRefreshListener(Runnable listener) {
        walletRefreshListeners.add(listener);
    }
    
    public void notifyWalletRefresh() {
        for (Runnable listener : walletRefreshListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in wallet refresh listener: " + e.getMessage());
            }
        }
    }
    
    public void addGoalRefreshListener(Runnable listener) {
        goalRefreshListeners.add(listener);
    }
    
    public void notifyGoalRefresh() {
        for (Runnable listener : goalRefreshListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in goal refresh listener: " + e.getMessage());
            }
        }
    }
    
    public void addBudgetRefreshListener(Runnable listener) {
        budgetRefreshListeners.add(listener);
    }
    
    public void notifyBudgetRefresh() {
        for (Runnable listener : budgetRefreshListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in budget refresh listener: " + e.getMessage());
            }
        }
    }

    // ==================== LEGACY SUPPORT ====================
    
    public static class DataHolder {
        private List<Budget> budgets;
        private List<Goal> goals;
        private List<Transaction> transactions;
        private List<Wallet> wallets;

        public List<Budget> getBudgets() { return budgets; }
        public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }
        public List<Goal> getGoals() { return goals; }
        public void setGoals(List<Goal> goals) { this.goals = goals; }
        public List<Transaction> getTransactions() { return transactions; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
        public List<Wallet> getWallets() { return wallets; }
        public void setWallets(List<Wallet> wallets) { this.wallets = wallets; }
    }

    public static DataHolder loadAccountData() {
        AccountDataLoader loader = getInstance();
        DataHolder data = new DataHolder();
        data.budgets = loader.getBudgets();
        data.transactions = loader.getTransactions();
        data.wallets = loader.getWallets();
        data.goals = loader.getGoals();
        return data;
    }
}
