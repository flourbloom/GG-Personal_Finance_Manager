package gitgud.pfm.services;

import java.util.ArrayList;
import java.util.List;

import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.TransactionCriteria;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Budget;
public class Filter {

    Filter() {
    }
    public List<Transaction> filterTransactions(TransactionCriteria criteria, AccountDataLoader.DataHolder accountdata) {
    List<Transaction> transactions = accountdata.getTransactions();
    List<Transaction> result = new ArrayList<>();
    for (Transaction t : transactions) {
        if (matchesTransactionFilterCriteria(t, criteria)) {
            result.add(t);
        }
    }
    return result;
    }
     private boolean matchesTransactionFilterCriteria(Transaction transaction, TransactionCriteria criteria) {
        if (!criteria.hasFilters()) {
            return true;
        }

        // Apply each provided filter; if any check fails, return false.
        if (criteria.getMinAmount() != null) {
            if (transaction.getAmount() < criteria.getMinAmount()) {
                return false;
            }
        }

        if (criteria.getMaxAmount() != null) {
            if (transaction.getAmount() > criteria.getMaxAmount()) {
                return false;
            }
        }

        if (criteria.getCategoryId() != null && !criteria.getCategoryId().isEmpty()) {
            if (!transaction.getCategoryId().equalsIgnoreCase(criteria.getCategoryId())) {
                return false;
            }
        }

        if (criteria.getWalletId() != null && !criteria.getWalletId().isEmpty()) {
            if (transaction.getWalletId() == null || 
                !transaction.getWalletId().toLowerCase().contains(criteria.getWalletId().toLowerCase())) {
                return false;
            }
        }

        if (criteria.getDateFrom() != null && !criteria.getDateFrom().isEmpty()) {
            if (transaction.getCreateTime() == null || 
                transaction.getCreateTime().compareTo(criteria.getDateFrom()) < 0) {
                return false;
            }
        }

        if (criteria.getDateTo() != null && !criteria.getDateTo().isEmpty()) {
            String dateTo = criteria.getDateTo();
            if (dateTo.length() == 10) {
                dateTo = dateTo + " 23:59:59";
            }
            if (transaction.getCreateTime() == null || 
                transaction.getCreateTime().compareTo(dateTo) > 0) {
                return false;
            }
        }

        if (criteria.getIncome() != null) {
            if (transaction.getIncome() != criteria.getIncome()) {
                return false;
            }
        }

        return true;
    }
    public List<Transaction> searchTransactions(String searchTerm, AccountDataLoader.DataHolder accountdata) {
            List<Transaction> transactions = accountdata.getTransactions();
            List<Transaction> result = new ArrayList<>();
            if (searchTerm == null || searchTerm.isEmpty()) {
            return transactions; // No search term, return all
            }
            String lowerSearchTerm = searchTerm.toLowerCase();
            for (Transaction t : transactions) {
            if ((t.getName() != null && t.getName().toLowerCase().contains(lowerSearchTerm))) {
                result.add(t);
            }
            }
        return result;
}
    public List<Goal> searchGoals(String searchTerm, AccountDataLoader.DataHolder accountdata) {
            List<Goal> goals = accountdata.getGoals();
            List<Goal> result = new ArrayList<>();
            if (searchTerm == null || searchTerm.isEmpty()) {
                return goals;
            }
            String lowerSearchTerm = searchTerm.toLowerCase();
            for (Goal g : goals) {
                if (g.getName() != null && g.getName().toLowerCase().contains(lowerSearchTerm)) {
                    result.add(g);
                }
            }
            return result;
    }

    public List<Budget> searchBudgets(String searchTerm, AccountDataLoader.DataHolder accountdata) {
            List<Budget> budgets = accountdata.getBudgets();
            List<Budget> result = new ArrayList<>();
            if (searchTerm == null || searchTerm.isEmpty()) {
                return budgets;
            }
            String lowerSearchTerm = searchTerm.toLowerCase();
            for (Budget b : budgets) {
                if (b.getName() != null && b.getName().toLowerCase().contains(lowerSearchTerm)) {
                    result.add(b);
                }
            }
            return result;
    }
}