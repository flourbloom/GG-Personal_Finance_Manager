package gitgud.pfm.services.business;

import gitgud.pfm.Models.Transaction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Transaction Filter Service - Handles filtering logic for transactions
 * Follows Strategy Pattern and Single Responsibility Principle
 */
public class TransactionFilterService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    /**
     * Filter transactions by category
     */
    public List<Transaction> filterByCategory(List<Transaction> transactions, String categoryId) {
        if (categoryId == null || categoryId.equals("All Categories")) {
            return transactions;
        }
        return transactions.stream()
            .filter(t -> categoryId.equals(t.getCategoryId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter transactions by type (income/expense)
     */
    public List<Transaction> filterByType(List<Transaction> transactions, String type) {
        if (type == null || type.equals("All Types")) {
            return transactions;
        }
        
        double incomeValue = type.equals("Income") ? 1.0 : 0.0;
        return transactions.stream()
            .filter(t -> t.getIncome() == incomeValue)
            .collect(Collectors.toList());
    }
    
    /**
     * Filter transactions by date range
     */
    public List<Transaction> filterByDateRange(List<Transaction> transactions, 
                                               LocalDate fromDate, LocalDate toDate) {
        return transactions.stream()
            .filter(t -> {
                if (t.getCreateTime() == null) return true;
                try {
                    LocalDate txDate = LocalDate.parse(t.getCreateTime().substring(0, 10));
                    boolean afterFrom = fromDate == null || !txDate.isBefore(fromDate);
                    boolean beforeTo = toDate == null || !txDate.isAfter(toDate);
                    return afterFrom && beforeTo;
                } catch (Exception e) {
                    return true;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter transactions by search text (searches in name)
     */
    public List<Transaction> filterBySearchText(List<Transaction> transactions, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return transactions;
        }
        
        String lowerSearch = searchText.toLowerCase().trim();
        return transactions.stream()
            .filter(t -> t.getName() != null && 
                        t.getName().toLowerCase().contains(lowerSearch))
            .collect(Collectors.toList());
    }
    
    /**
     * Apply all filters at once
     */
    public List<Transaction> applyAllFilters(List<Transaction> transactions,
                                            String categoryId,
                                            String type,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            String searchText) {
        List<Transaction> result = transactions;
        result = filterByCategory(result, categoryId);
        result = filterByType(result, type);
        result = filterByDateRange(result, fromDate, toDate);
        result = filterBySearchText(result, searchText);
        return result;
    }
    
    /**
     * Create a custom filter predicate
     */
    public Predicate<Transaction> createCustomFilter(String categoryId, String type, 
                                                     LocalDate fromDate, LocalDate toDate, 
                                                     String searchText) {
        return transaction -> {
            // Category filter
            if (categoryId != null && !categoryId.equals("All Categories")) {
                if (!categoryId.equals(transaction.getCategoryId())) {
                    return false;
                }
            }
            
            // Type filter
            if (type != null && !type.equals("All Types")) {
                double incomeValue = type.equals("Income") ? 1.0 : 0.0;
                if (transaction.getIncome() != incomeValue) {
                    return false;
                }
            }
            
            // Date range filter
            if (fromDate != null || toDate != null) {
                if (transaction.getCreateTime() == null) {
                    return false;
                }
                try {
                    LocalDate txDate = LocalDate.parse(transaction.getCreateTime().substring(0, 10));
                    if (fromDate != null && txDate.isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && txDate.isAfter(toDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            
            // Search text filter
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerSearch = searchText.toLowerCase().trim();
                if (transaction.getName() == null || 
                    !transaction.getName().toLowerCase().contains(lowerSearch)) {
                    return false;
                }
            }
            
            return true;
        };
    }
}
