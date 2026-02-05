package gitgud.pfm.services.business;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.GoalService;
import gitgud.pfm.services.TransactionService;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Goal Progress Service - Handles goal progress calculations
 * Follows Single Responsibility Principle
 */
public class GoalProgressService {
    
    private final GoalService goalService;
    private final TransactionService transactionService;
    
    public GoalProgressService(GoalService goalService, TransactionService transactionService) {
        this.goalService = goalService;
        this.transactionService = transactionService;
    }
    
    /**
     * Get all priority goals (priority <= 5) that are not yet completed
     */
    public List<Goal> getPriorityGoals() {
        return goalService.readAll().stream()
            .filter(g -> g.getPriority() <= 5 && g.getBalance() < g.getTarget())
            .sorted((a, b) -> Double.compare(a.getPriority(), b.getPriority()))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate goal completion percentage
     */
    public double getGoalCompletionPercentage(Goal goal) {
        if (goal.getTarget() <= 0) {
            return 0;
        }
        return Math.min(100, (goal.getBalance() / goal.getTarget()) * 100);
    }
    
    /**
     * Calculate total balance allocated to a goal from transactions
     */
    public double calculateGoalBalance(String goalId) {
        List<Transaction> goalTransactions = transactionService.readAll().stream()
            .filter(t -> goalId.equals(t.getGoalId()))
            .collect(Collectors.toList());
        
        return goalTransactions.stream()
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    /**
     * Get count of transactions allocated to a goal
     */
    public int getGoalTransactionCount(String goalId) {
        return (int) transactionService.readAll().stream()
            .filter(t -> goalId.equals(t.getGoalId()))
            .count();
    }
    
    /**
     * Get goal progress summary
     */
    public GoalProgressSummary getGoalProgressSummary(Goal goal) {
        double percentage = getGoalCompletionPercentage(goal);
        int transactionCount = getGoalTransactionCount(goal.getId());
        double remaining = Math.max(0, goal.getTarget() - goal.getBalance());
        
        return new GoalProgressSummary(
            goal.getBalance(),
            goal.getTarget(),
            percentage,
            remaining,
            transactionCount
        );
    }
    
    /**
     * Goal Progress Summary DTO
     */
    public static class GoalProgressSummary {
        private final double currentBalance;
        private final double targetAmount;
        private final double percentage;
        private final double remaining;
        private final int transactionCount;
        
        public GoalProgressSummary(double currentBalance, double targetAmount, 
                                   double percentage, double remaining, int transactionCount) {
            this.currentBalance = currentBalance;
            this.targetAmount = targetAmount;
            this.percentage = percentage;
            this.remaining = remaining;
            this.transactionCount = transactionCount;
        }
        
        public double getCurrentBalance() {
            return currentBalance;
        }
        
        public double getTargetAmount() {
            return targetAmount;
        }
        
        public double getPercentage() {
            return percentage;
        }
        
        public double getRemaining() {
            return remaining;
        }
        
        public int getTransactionCount() {
            return transactionCount;
        }
    }
}
