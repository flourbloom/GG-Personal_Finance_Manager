package gitgud.pfm.GUI.data;

import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Wallet;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.services.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI DataStore - Bridges GUI layer with actual database services
 * Provides unified access to Transaction, Goal, Wallet, and Budget data
 */
public class DataStore {
    private static DataStore instance;
    private final TransactionService transactionService;
    private final GoalService goalService;
    private final WalletService walletService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    
    private DataStore() {
        this.transactionService = new TransactionService();
        this.goalService = new GoalService();
        this.walletService = new WalletService();
        this.budgetService = new BudgetService();
        this.categoryService = new CategoryService();
    }
    
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
    
    // ============== Transaction Methods ==============
    public List<Transaction> getTransactions() {
        try {
            return transactionService.readAll();
        } catch (Exception e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addTransaction(Transaction transaction) {
        try {
            transactionService.create(transaction);
        } catch (Exception e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }
    
    public void updateTransaction(Transaction transaction) {
        try {
            transactionService.update(transaction);
        } catch (Exception e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }
    
    public void deleteTransaction(String id) {
        try {
            transactionService.delete(id);
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }
    
    public double getTotalIncome() {
        try {
            return transactionService.getTotalIncome();
        } catch (Exception e) {
            System.err.println("Error calculating total income: " + e.getMessage());
            return 0.0;
        }
    }
    
    public double getTotalExpenses() {
        try {
            return transactionService.getTotalExpenses();
        } catch (Exception e) {
            System.err.println("Error calculating total expenses: " + e.getMessage());
            return 0.0;
        }
    }
    
    // ============== Goal Methods ==============
    public List<Goal> getGoals() {
        try {
            return goalService.readAll();
        } catch (Exception e) {
            System.err.println("Error loading goals: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addGoal(Goal goal) {
        try {
            goalService.create(goal);
        } catch (Exception e) {
            System.err.println("Error adding goal: " + e.getMessage());
        }
    }
    
    public void updateGoal(Goal goal) {
        try {
            goalService.update(goal);
        } catch (Exception e) {
            System.err.println("Error updating goal: " + e.getMessage());
        }
    }
    
    public void deleteGoal(String id) {
        try {
            goalService.delete(id);
        } catch (Exception e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
    }
    
    // ============== Wallet Methods ==============
    public List<Wallet> getWallets() {
        try {
            return walletService.readAll();
        } catch (Exception e) {
            System.err.println("Error loading wallets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Wallet getWalletById(String id) {
        try {
            return walletService.read(id);
        } catch (Exception e) {
            System.err.println("Error loading wallet: " + e.getMessage());
            return null;
        }
    }
    
    public void addWallet(Wallet wallet) {
        try {
            walletService.create(wallet);
        } catch (Exception e) {
            System.err.println("Error adding wallet: " + e.getMessage());
        }
    }
    
    public void updateWallet(Wallet wallet) {
        try {
            walletService.update(wallet);
        } catch (Exception e) {
            System.err.println("Error updating wallet: " + e.getMessage());
        }
    }
    
    public void deleteWallet(String id) {
        try {
            walletService.delete(id);
        } catch (Exception e) {
            System.err.println("Error deleting wallet: " + e.getMessage());
        }
    }
    
    // ============== Budget Methods ==============
    public List<Budget> getBudgets() {
        try {
            return budgetService.readAll();
        } catch (Exception e) {
            System.err.println("Error loading budgets: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void addBudget(Budget budget) {
        try {
            budgetService.create(budget);
            // If budget has a category, create the Budget_Category relationship
            if (budget.getCategoryId() != null) {
                budgetService.addCategoryToBudget(budget.getId(), budget.getCategoryId());
            }
        } catch (Exception e) {
            System.err.println("Error adding budget: " + e.getMessage());
        }
    }
    
    public void addBudgetWithCategories(Budget budget, List<String> categoryIds) {
        try {
            budgetService.create(budget);
            // Create Budget_Category relationships for each category
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (String categoryId : categoryIds) {
                    budgetService.addCategoryToBudget(budget.getId(), categoryId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding budget with categories: " + e.getMessage());
        }
    }
    
    public void updateBudget(Budget budget) {
        try {
            // First, remove all existing category relationships for this budget
            budgetService.removeAllCategoriesFromBudget(budget.getId());
            
            // Update the budget itself
            budgetService.update(budget);
            
            // If budget has a category, create the new Budget_Category relationship
            if (budget.getCategoryId() != null) {
                budgetService.addCategoryToBudget(budget.getId(), budget.getCategoryId());
            }
        } catch (Exception e) {
            System.err.println("Error updating budget: " + e.getMessage());
        }
    }
    
    public void updateBudgetWithCategories(Budget budget, List<String> categoryIds) {
        try {
            // First, remove all existing category relationships for this budget
            budgetService.removeAllCategoriesFromBudget(budget.getId());
            
            // Update the budget itself
            budgetService.update(budget);
            
            // Create new Budget_Category relationships for each category
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (String categoryId : categoryIds) {
                    budgetService.addCategoryToBudget(budget.getId(), categoryId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating budget with categories: " + e.getMessage());
        }
    }
    
    public void deleteBudget(String id) {
        try {
            budgetService.delete(id);
        } catch (Exception e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
    }
    
    // ============== Category Methods ==============
    public CategoryService getCategoryService() {
        return categoryService;
    }
    
    // ============== Refresh Listeners ==============
    private final java.util.List<Runnable> walletRefreshListeners = new java.util.ArrayList<>();
    private final java.util.List<Runnable> goalRefreshListeners = new java.util.ArrayList<>();
    private final java.util.List<Runnable> budgetRefreshListeners = new java.util.ArrayList<>();
    
    public void addWalletRefreshListener(Runnable listener) {
        walletRefreshListeners.add(listener);
    }
    
    public void removeWalletRefreshListener(Runnable listener) {
        walletRefreshListeners.remove(listener);
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
    
    public void removeGoalRefreshListener(Runnable listener) {
        goalRefreshListeners.remove(listener);
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
    
    public void removeBudgetRefreshListener(Runnable listener) {
        budgetRefreshListeners.remove(listener);
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
}
