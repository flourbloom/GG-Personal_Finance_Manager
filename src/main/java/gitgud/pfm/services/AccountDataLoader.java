package gitgud.pfm.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
 

import gitgud.pfm.Models.*;

public class AccountDataLoader {

    public static class DataHolder {
        private List<Budget> budgets;
        private List<Goal> goals;
        private List<Transaction> transactions;
        private gitgud.pfm.Models.Account account;

        public List<Budget> getBudgets() {
            return budgets;
        }

        public void setBudgets(List<Budget> budgets) {
            this.budgets = budgets;
        }

        public List<Goal> getGoals() {
            return goals;
        }

        public void setGoals(List<Goal> goals) {
            this.goals = goals;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        public gitgud.pfm.Models.Account getAccount() {
            return account;
        }

        public void setAccount(gitgud.pfm.Models.Account account) {
            this.account = account;
        }

    }

    public static DataHolder loadAccountData(String accountID) {
        DataHolder data = new DataHolder();

        // Budgets: read all budgets (public data)
        Map<String, Object> config = new HashMap<>();
        config.put("class", Budget.class);
        config.put("table", "Budget");
        config.put("orderBy", "start_date DESC");
        try {
            data.budgets = GenericSQLiteService.readAll(config);
        } catch (Exception e) {
            System.err.println("Warning: failed to read budgets: " + e.getMessage());
            data.budgets = new java.util.ArrayList<>();
        }

        // Transactions: read all transactions where AccountID = keyString
        config.clear();
        Map<String, Object> txFilters = new HashMap<>();
        txFilters.put("AccountID", accountID);
        config.put("class", Transaction.class);
        config.put("table", "transaction_records");
        config.put("filters", txFilters);
        config.put("orderBy", "Create_time DESC");
        data.transactions = GenericSQLiteService.readAll(config);

        // Account: read Account by primary key 'AccountID'
        config.clear();
        config.put("class", Account.class);
        config.put("table", "Accounts");
        config.put("pk", "AccountID");
        config.put("id", accountID);
        try {
            gitgud.pfm.Models.Account Account = GenericSQLiteService.read(config);
            data.setAccount(Account);
        } catch (Exception e) {
            System.err.println("Warning: failed to read Account with AccountID = " + accountID + ": " + e.getMessage());
            data.setAccount(null);
        }

        // Goals: read all goals (public data)
        config.clear();
        config.put("class", Goal.class);
        config.put("table", "Goal");
        config.put("orderBy", "createAt DESC");
        try {
            data.goals = GenericSQLiteService.readAll(config);
        } catch (Exception e) {
            System.err.println("Warning: failed to read goals: " + e.getMessage());
            data.goals = new java.util.ArrayList<>();
        }

        return data;
    }
}
