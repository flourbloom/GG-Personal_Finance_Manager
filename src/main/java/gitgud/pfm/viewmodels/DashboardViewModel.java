package gitgud.pfm.viewmodels;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.TransactionService;
import gitgud.pfm.services.business.BudgetCalculationService;
import gitgud.pfm.services.business.BudgetCalculationService.BudgetSummary;
import gitgud.pfm.services.business.GoalProgressService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Dashboard ViewModel - Handles presentation logic for Dashboard view
 * Separates data preparation from UI code
 * Follows MVVM pattern
 */
public class DashboardViewModel {
    
    private final BudgetCalculationService budgetCalculationService;
    private final GoalProgressService goalProgressService;
    private final TransactionService transactionService;
    
    // Observable properties for UI binding
    private final StringProperty totalSpentText = new SimpleStringProperty();
    private final StringProperty budgetLimitText = new SimpleStringProperty();
    private final StringProperty budgetPercentText = new SimpleStringProperty();
    private final DoubleProperty budgetProgress = new SimpleDoubleProperty();
    private final StringProperty budgetHintText = new SimpleStringProperty();
    
    private final ObservableList<Goal> priorityGoals = FXCollections.observableArrayList();
    private final ObservableList<Transaction> recentTransactions = FXCollections.observableArrayList();
    
    public DashboardViewModel(BudgetCalculationService budgetCalculationService,
                             GoalProgressService goalProgressService,
                             TransactionService transactionService) {
        this.budgetCalculationService = budgetCalculationService;
        this.goalProgressService = goalProgressService;
        this.transactionService = transactionService;
    }
    
    /**
     * Load all dashboard data
     */
    public void loadData() {
        loadBudgetData();
        loadPriorityGoals();
        loadRecentTransactions();
    }
    
    /**
     * Refresh budget data
     */
    public void loadBudgetData() {
        BudgetSummary summary = budgetCalculationService.getBudgetSummary();
        
        totalSpentText.set(String.format("$%.2f", summary.getTotalSpent()));
        budgetLimitText.set(String.format("$%.0f", summary.getBudgetLimit()));
        budgetPercentText.set(String.format("%.0f%%", summary.getPercentage()));
        budgetProgress.set(summary.getPercentage() / 100.0);
        budgetHintText.set(String.format("$%.2f remaining this month", summary.getRemaining()));
    }
    
    /**
     * Load priority goals (priority <= 5)
     */
    public void loadPriorityGoals() {
        List<Goal> goals = goalProgressService.getPriorityGoals();
        priorityGoals.setAll(goals);
    }
    
    /**
     * Load recent transactions (last 10)
     */
    public void loadRecentTransactions() {
        List<Transaction> transactions = transactionService.readAll();
        int limit = Math.min(10, transactions.size());
        recentTransactions.setAll(transactions.subList(0, limit));
    }
    
    /**
     * Get goal completion percentage
     */
    public double getGoalCompletionPercentage(Goal goal) {
        return goalProgressService.getGoalCompletionPercentage(goal);
    }
    
    // ============== Property Getters for Binding ==============
    
    public StringProperty totalSpentTextProperty() {
        return totalSpentText;
    }
    
    public StringProperty budgetLimitTextProperty() {
        return budgetLimitText;
    }
    
    public StringProperty budgetPercentTextProperty() {
        return budgetPercentText;
    }
    
    public DoubleProperty budgetProgressProperty() {
        return budgetProgress;
    }
    
    public StringProperty budgetHintTextProperty() {
        return budgetHintText;
    }
    
    public ObservableList<Goal> getPriorityGoals() {
        return priorityGoals;
    }
    
    public ObservableList<Transaction> getRecentTransactions() {
        return recentTransactions;
    }
    
    // ============== Read-only Properties ==============
    
    public String getTotalSpentText() {
        return totalSpentText.get();
    }
    
    public String getBudgetLimitText() {
        return budgetLimitText.get();
    }
    
    public String getBudgetPercentText() {
        return budgetPercentText.get();
    }
    
    public double getBudgetProgress() {
        return budgetProgress.get();
    }
    
    public String getBudgetHintText() {
        return budgetHintText.get();
    }
}
