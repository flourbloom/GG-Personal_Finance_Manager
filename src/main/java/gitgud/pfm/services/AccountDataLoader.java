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

    }

    public static DataHolder AccountDataLoader(String accountID) {
        DataHolder data = new DataHolder();

        // Load Budgets
        Map<String, Object> config = new HashMap<>();
        config.put("class", Budget.class);
        config.put("table", "Budget");
        config.put("filters", accountID);
        config.put("orderBy", "date DESC");
        data.budgets = GenericSQLiteService.readAll(config);

        config.clear();
        config.put("class", Transaction.class);
        config.put("table", "transactions");
        config.put("filters", accountID);
        config.put("orderBy", "date DESC");
        data.transactions = GenericSQLiteService.readAll(config);

        config.clear();
        config.put("class", Goal.class);
        config.put("table", "Goal");
        config.put("filters", accountID);
        config.put("orderBy", "date DESC");
        data.goals = GenericSQLiteService.readAll(config);

        return data;
    }
}
