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
        private gitgud.pfm.Models.Wallet wallet;

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

        public gitgud.pfm.Models.Wallet getWallet() {
            return wallet;
        }

        public void setWallet(gitgud.pfm.Models.Wallet wallet) {
            this.wallet = wallet;
        }

    }

    public static DataHolder loadAccountData(String accountID) {
        DataHolder data = new DataHolder();

        // Budgets: read all budgets (public data)
        Map<String, Object> config = new HashMap<>();
        config.put("class", Budget.class);
        config.put("table", "Budget");
        config.put("orderBy", "start_date DESC");
        data.budgets = GenericSQLiteService.readAll(config);

        // Transactions: read all transactions where AccountID = keyString
        config.clear();
        Map<String, Object> txFilters = new HashMap<>();
        txFilters.put("AccountID", accountID);
        config.put("class", Transaction.class);
        config.put("table", "transaction_records");
        config.put("filters", txFilters);
        config.put("orderBy", "Create_time DESC");
        data.transactions = GenericSQLiteService.readAll(config);

        // Wallet: read Wallet by primary key 'AccountID'
        config.clear();
        config.put("class", Wallet.class);
        config.put("table", "Wallets");
        config.put("pk", "AccountID");
        config.put("id", accountID);
        Wallet wallet = GenericSQLiteService.read(config);
        data.setWallet(wallet);

        // Goals: read all goals (public data)
        config.clear();
        config.put("class", Goal.class);
        config.put("table", "Goal");
        config.put("orderBy", "createAt DESC");
        data.goals = GenericSQLiteService.readAll(config);

        return data;
    }
}
