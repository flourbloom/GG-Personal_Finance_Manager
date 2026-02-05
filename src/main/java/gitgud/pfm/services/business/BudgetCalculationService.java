package gitgud.pfm.services.business;

import gitgud.pfm.Models.Budget;
import gitgud.pfm.services.BudgetService;
import gitgud.pfm.services.TransactionService;
import java.util.List;

/**
 * Budget Calculation Service - Handles all budget-related calculations
 * Follows Single Responsibility Principle - only calculates budget metrics
 */
public class BudgetCalculationService {
    
    private final BudgetService budgetService;
    private final TransactionService transactionService;
    
    public BudgetCalculationService(BudgetService budgetService, TransactionService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
    }
    
    /**
     * Get the monthly budget limit
     * @return Monthly budget limit, or default 3000.0 if not set
     */
    public double getMonthlyBudgetLimit() {
        List<Budget> budgets = budgetService.readAll();
        
        // Look for a monthly budget
        for (Budget budget : budgets) {
            if (budget.getPeriodType() == Budget.PeriodType.MONTHLY) {
                return budget.getLimitAmount();
            }
        }
        
        // Return first budget limit if exists, otherwise default
        if (!budgets.isEmpty()) {
            return budgets.get(0).getLimitAmount();
        }
        
        return 3000.0; // Default budget limit
    }
    
    /**
     * Calculate total expenses
     */
    public double getTotalExpenses() {
        return transactionService.getTotalExpenses();
    }
    
    /**
     * Calculate budget usage percentage
     */
    public double getBudgetUsagePercentage() {
        double budgetLimit = getMonthlyBudgetLimit();
        double totalSpent = getTotalExpenses();
        return Math.min(100, (totalSpent / budgetLimit) * 100);
    }
    
    /**
     * Calculate remaining budget
     */
    public double getRemainingBudget() {
        double budgetLimit = getMonthlyBudgetLimit();
        double totalSpent = getTotalExpenses();
        return Math.max(0, budgetLimit - totalSpent);
    }
    
    /**
     * Get budget status summary
     */
    public BudgetSummary getBudgetSummary() {
        double budgetLimit = getMonthlyBudgetLimit();
        double totalSpent = getTotalExpenses();
        double percentage = getBudgetUsagePercentage();
        double remaining = getRemainingBudget();
        
        return new BudgetSummary(budgetLimit, totalSpent, percentage, remaining);
    }
    
    /**
     * Budget Summary DTO
     */
    public static class BudgetSummary {
        private final double budgetLimit;
        private final double totalSpent;
        private final double percentage;
        private final double remaining;
        
        public BudgetSummary(double budgetLimit, double totalSpent, double percentage, double remaining) {
            this.budgetLimit = budgetLimit;
            this.totalSpent = totalSpent;
            this.percentage = percentage;
            this.remaining = remaining;
        }
        
        public double getBudgetLimit() {
            return budgetLimit;
        }
        
        public double getTotalSpent() {
            return totalSpent;
        }
        
        public double getPercentage() {
            return percentage;
        }
        
        public double getRemaining() {
            return remaining;
        }
    }
}
