package gitgud.pfm.services;

import java.util.List;
 
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
        try {
            BudgetService budgetService = new BudgetService();
            data.budgets = budgetService.readAll();
        } catch (Exception e) {
            System.err.println("Warning: failed to read budgets: " + e.getMessage());
            data.budgets = new java.util.ArrayList<>();
        }

        // Transactions: read all transactions
        try {
            TransactionService txService = new TransactionService();
            data.transactions = txService.readAll();
        } catch (Exception e) {
            System.err.println("Warning: failed to read transactions: " + e.getMessage());
            data.transactions = new java.util.ArrayList<>();
        }

        // Wallet: read Wallet by primary key 'AccountID'
        try {
            WalletService walletService = new WalletService();
            gitgud.pfm.Models.Wallet wallet = walletService.read(accountID);
            data.setWallet(wallet);
        } catch (Exception e) {
            System.err.println("Warning: failed to read Wallet with AccountID = " + accountID + ": " + e.getMessage());
            data.setWallet(null);
        }

        // Goals: read all goals (public data)
        try {
            GoalService goalService = new GoalService();
            data.goals = goalService.readAll();
        } catch (Exception e) {
            System.err.println("Warning: failed to read goals: " + e.getMessage());
            data.goals = new java.util.ArrayList<>();
        }

        return data;
    }
}
