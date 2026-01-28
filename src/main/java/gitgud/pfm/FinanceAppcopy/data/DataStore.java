package gitgud.pfm.FinanceAppcopy.data;

import gitgud.pfm.FinanceAppcopy.model.Transaction;
import gitgud.pfm.FinanceAppcopy.model.Goal;
import gitgud.pfm.FinanceAppcopy.model.Account;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;
    private List<Transaction> transactions;
    private List<Account> accounts;
    private List<Goal> goals;
    
    private DataStore() {
        initializeDefaultData();
    }
    
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }
    
    private void initializeDefaultData() {
        // Initialize transactions
        transactions = new ArrayList<>();
        transactions.add(new Transaction(1, "expense", "Grocery Store", "food", 85.20, 1, null, LocalDateTime.now()));
        transactions.add(new Transaction(2, "income", "Salary Deposit", "income", 3200.00, 1, null, LocalDateTime.now().minusHours(1)));
        transactions.add(new Transaction(3, "expense", "Uber Ride", "transport", 24.50, 2, null, LocalDateTime.now().minusDays(1)));
        transactions.add(new Transaction(4, "expense", "Netflix Subscription", "entertainment", 15.99, 1, null, LocalDateTime.now().minusDays(2)));
        transactions.add(new Transaction(5, "expense", "Electric Bill", "bills", 142.00, 1, null, LocalDateTime.now().minusDays(3)));
        transactions.add(new Transaction(6, "expense", "Amazon Purchase", "shopping", 67.89, 1, null, LocalDateTime.now().minusDays(4)));
        
        // Initialize accounts
        accounts = new ArrayList<>();
        accounts.add(new Account(1, "ABA Savings", "bank", 5420.50, "#3b82f6"));
        accounts.add(new Account(2, "Cash Wallet", "cash", 340.00, "#22c55e"));
        accounts.add(new Account(3, "Visa Credit", "credit", -850.00, "#ef4444"));
        
        // Initialize goals
        goals = new ArrayList<>();
        goals.add(new Goal(1, "Emergency Fund", 10000, 4500, LocalDate.of(2026, 6, 30), "emergency", true, LocalDate.of(2025, 1, 1)));
        goals.add(new Goal(2, "Summer Vacation", 3000, 1200, LocalDate.of(2026, 7, 15), "vacation", false, LocalDate.of(2025, 3, 15)));
        goals.add(new Goal(3, "New Laptop", 2000, 2000, LocalDate.of(2026, 2, 1), "other", false, LocalDate.of(2025, 6, 1)));
        goals.add(new Goal(4, "Car Down Payment", 8000, 2800, LocalDate.of(2026, 12, 31), "car", true, LocalDate.of(2025, 9, 1)));
    }
    
    // Transaction methods
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
    
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
    
    public int getNextTransactionId() {
        return transactions.stream()
                .mapToInt(Transaction::getId)
                .max()
                .orElse(0) + 1;
    }
    
    // Account methods
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }
    
    public Account getAccountById(int id) {
        return accounts.stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    public void addAccount(Account account) {
        accounts.add(account);
    }
    
    public void updateAccount(Account account) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getId() == account.getId()) {
                accounts.set(i, account);
                break;
            }
        }
    }
    
    public void deleteAccount(int id) {
        accounts.removeIf(a -> a.getId() == id);
    }
    
    public int getNextAccountId() {
        return accounts.stream()
                .mapToInt(Account::getId)
                .max()
                .orElse(0) + 1;
    }
    
    // Goal methods
    public List<Goal> getGoals() {
        return new ArrayList<>(goals);
    }
    
    public void addGoal(Goal goal) {
        goals.add(goal);
    }
    
    public void updateGoal(Goal goal) {
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getId() == goal.getId()) {
                goals.set(i, goal);
                break;
            }
        }
    }
    
    public void deleteGoal(int id) {
        goals.removeIf(g -> g.getId() == id);
    }
    
    public int getNextGoalId() {
        return goals.stream()
                .mapToInt(Goal::getId)
                .max()
                .orElse(0) + 1;
    }
}
