package gitgud.pfm.fixtures;

import gitgud.pfm.Models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating test data fixtures.
 * Provides reusable test objects for unit, integration, and E2E tests.
 */
public class TestDataFactory {

    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ==================== WALLET FIXTURES ====================

    /**
     * Create a test wallet with specified parameters
     */
    public static Wallet createWallet(String color, double balance, String name) {
        return new Wallet(color, balance, name);
    }

    /**
     * Create a default test wallet with initial balance of 500,000
     */
    public static Wallet createDefaultWallet() {
        return createWallet("Blue", 500000.0, "Default Wallet");
    }

    /**
     * Create a wallet with zero balance
     */
    public static Wallet createEmptyWallet(String name) {
        return createWallet("Gray", 0.0, name);
    }

    /**
     * Create multiple test wallets
     */
    public static List<Wallet> createMultipleWallets() {
        List<Wallet> wallets = new ArrayList<>();
        wallets.add(createWallet("Green", 100000.0, "Cash Wallet"));
        wallets.add(createWallet("Blue", 500000.0, "Savings"));
        wallets.add(createWallet("Red", 250000.0, "Checking"));
        return wallets;
    }

    // ==================== TRANSACTION FIXTURES ====================

    /**
     * Create a test transaction with specified parameters
     */
    public static Transaction createTransaction(String categoryId, double amount, String name,
                                                 double income, String walletId, String createTime) {
        return new Transaction(categoryId, amount, name, income, walletId, createTime);
    }

    /**
     * Create an income transaction
     */
    public static Transaction createIncomeTransaction(double amount, String name, String walletId) {
        return createTransaction(
            "CAT_SALARY",
            amount,
            name,
            1.0, // income flag
            walletId,
            LocalDateTime.now().format(DATETIME_FORMATTER)
        );
    }

    /**
     * Create an expense transaction
     */
    public static Transaction createExpenseTransaction(double amount, String name, 
                                                        String categoryId, String walletId) {
        return createTransaction(
            categoryId,
            amount,
            name,
            0.0, // expense flag
            walletId,
            LocalDateTime.now().format(DATETIME_FORMATTER)
        );
    }

    /**
     * Create a food expense transaction
     */
    public static Transaction createFoodExpense(double amount, String walletId) {
        return createExpenseTransaction(amount, "Food Purchase", "CAT_FOOD", walletId);
    }

    /**
     * Create a transport expense transaction
     */
    public static Transaction createTransportExpense(double amount, String walletId) {
        return createExpenseTransaction(amount, "Transport", "CAT_TRANSPORT", walletId);
    }

    /**
     * Create a salary income transaction
     */
    public static Transaction createSalaryIncome(double amount, String walletId) {
        return createIncomeTransaction(amount, "Monthly Salary", walletId);
    }

    /**
     * Create a list of test transactions
     */
    public static List<Transaction> createTestTransactions(String walletId) {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createSalaryIncome(100000.0, walletId));
        transactions.add(createFoodExpense(50000.0, walletId));
        transactions.add(createTransportExpense(30000.0, walletId));
        transactions.add(createIncomeTransaction(25000.0, "Freelance Work", walletId));
        return transactions;
    }

    /**
     * Create a transaction with specific date
     */
    public static Transaction createTransactionWithDate(String categoryId, double amount, 
                                                         String name, double income,
                                                         String walletId, LocalDate date) {
        return createTransaction(
            categoryId,
            amount,
            name,
            income,
            walletId,
            date.atStartOfDay().format(DATETIME_FORMATTER)
        );
    }

    // ==================== BUDGET FIXTURES ====================

    /**
     * Create a test budget with specified parameters
     */
    public static Budget createBudget(String name, double limitAmount, double balance, 
                                       String startDate, String endDate) {
        return new Budget(name, limitAmount, balance, startDate, endDate);
    }

    /**
     * Create a monthly budget for current month
     */
    public static Budget createMonthlyBudget(String name, double limitAmount) {
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfMonth(1).format(DATE_FORMATTER);
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).format(DATE_FORMATTER);
        return createBudget(name, limitAmount, 0.0, startDate, endDate);
    }

    /**
     * Create a food budget
     */
    public static Budget createFoodBudget(double limitAmount) {
        return createMonthlyBudget("Food Budget", limitAmount);
    }

    /**
     * Create a transport budget
     */
    public static Budget createTransportBudget(double limitAmount) {
        return createMonthlyBudget("Transport Budget", limitAmount);
    }

    /**
     * Create a budget that is already partially spent
     */
    public static Budget createPartiallySpentBudget(String name, double limitAmount, double spent) {
        LocalDate now = LocalDate.now();
        String startDate = now.withDayOfMonth(1).format(DATE_FORMATTER);
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).format(DATE_FORMATTER);
        return createBudget(name, limitAmount, spent, startDate, endDate);
    }

    /**
     * Create an exceeded budget (spent more than limit)
     */
    public static Budget createExceededBudget() {
        return createPartiallySpentBudget("Exceeded Budget", 500000.0, 600000.0);
    }

    /**
     * Create multiple test budgets
     */
    public static List<Budget> createTestBudgets() {
        List<Budget> budgets = new ArrayList<>();
        budgets.add(createFoodBudget(500000.0));
        budgets.add(createTransportBudget(200000.0));
        budgets.add(createPartiallySpentBudget("Entertainment", 300000.0, 100000.0));
        return budgets;
    }

    // ==================== GOAL FIXTURES ====================

    /**
     * Create a test goal with specified parameters
     */
    public static Goal createGoal(String name, double target, double current,
                                   String deadline, double priority, String createTime) {
        return new Goal(name, target, current, deadline, priority, createTime);
    }

    /**
     * Create a savings goal
     */
    public static Goal createSavingsGoal(String name, double target, double current, 
                                          String deadline, double priority) {
        return createGoal(
            name,
            target,
            current,
            deadline,
            priority,
            LocalDateTime.now().format(DATETIME_FORMATTER)
        );
    }

    /**
     * Create an emergency fund goal
     */
    public static Goal createEmergencyFundGoal() {
        return createSavingsGoal(
            "Emergency Fund",
            1000000.0,  // 1 million target
            100000.0,   // 100k current
            LocalDate.now().plusYears(1).format(DATE_FORMATTER),
            1.0         // High priority
        );
    }

    /**
     * Create a vacation goal
     */
    public static Goal createVacationGoal() {
        return createSavingsGoal(
            "Vacation Fund",
            500000.0,
            50000.0,
            LocalDate.now().plusMonths(6).format(DATE_FORMATTER),
            0.5 // Medium priority
        );
    }

    /**
     * Create a completed goal (current >= target)
     */
    public static Goal createCompletedGoal() {
        return createSavingsGoal(
            "Completed Goal",
            500000.0,
            500000.0,
            LocalDate.now().plusMonths(3).format(DATE_FORMATTER),
            1.0
        );
    }

    /**
     * Create multiple test goals
     */
    public static List<Goal> createTestGoals() {
        List<Goal> goals = new ArrayList<>();
        goals.add(createEmergencyFundGoal());
        goals.add(createVacationGoal());
        goals.add(createSavingsGoal("New Phone", 200000.0, 75000.0,
            LocalDate.now().plusMonths(2).format(DATE_FORMATTER), 0.3));
        return goals;
    }

    // ==================== CATEGORY FIXTURES ====================

    /**
     * Create a test category
     */
    public static Category createCategory(String id, String name, String description, Category.Type type) {
        return new Category(id, name, description, type);
    }

    /**
     * Create an expense category
     */
    public static Category createExpenseCategory(String id, String name) {
        return createCategory(id, name, name + " expenses", Category.Type.EXPENSE);
    }

    /**
     * Create an income category
     */
    public static Category createIncomeCategory(String id, String name) {
        return createCategory(id, name, name + " income", Category.Type.INCOME);
    }

    /**
     * Create default test categories
     */
    public static List<Category> createDefaultCategories() {
        List<Category> categories = new ArrayList<>();
        
        // Expense categories
        categories.add(createExpenseCategory("CAT_FOOD", "Food & Drinks"));
        categories.add(createExpenseCategory("CAT_TRANSPORT", "Transport"));
        categories.add(createExpenseCategory("CAT_UTILITIES", "Utilities"));
        categories.add(createExpenseCategory("CAT_ENTERTAINMENT", "Entertainment"));
        categories.add(createExpenseCategory("CAT_HOUSING", "Housing"));
        
        // Income categories
        categories.add(createIncomeCategory("CAT_SALARY", "Salary"));
        categories.add(createIncomeCategory("CAT_FREELANCE", "Freelance"));
        categories.add(createIncomeCategory("CAT_INTEREST", "Interest"));
        
        return categories;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get current date formatted
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Get current datetime formatted
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * Get date N days from now
     */
    public static String getDateFromNow(int days) {
        return LocalDate.now().plusDays(days).format(DATE_FORMATTER);
    }

    /**
     * Get date N months from now
     */
    public static String getDateMonthsFromNow(int months) {
        return LocalDate.now().plusMonths(months).format(DATE_FORMATTER);
    }

    /**
     * Calculate budget status (percentage spent)
     */
    public static double calculateBudgetPercentage(Budget budget) {
        if (budget.getLimitAmount() == 0) return 0;
        return (budget.getBalance() / budget.getLimitAmount()) * 100;
    }

    /**
     * Check if budget is exceeded
     */
    public static boolean isBudgetExceeded(Budget budget) {
        return budget.getBalance() > budget.getLimitAmount();
    }

    /**
     * Calculate goal progress percentage
     */
    public static double calculateGoalProgress(Goal goal) {
        if (goal.getTarget() == 0) return 0;
        return (goal.getBalance() / goal.getTarget()) * 100;
    }

    /**
     * Check if goal is completed
     */
    public static boolean isGoalCompleted(Goal goal) {
        return goal.getBalance() >= goal.getTarget();
    }
}
