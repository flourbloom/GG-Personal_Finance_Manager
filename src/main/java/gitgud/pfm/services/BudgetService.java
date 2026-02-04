package gitgud.pfm.services;

import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.BudgetCategory;
import gitgud.pfm.Models.Category;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BudgetService - Explicit CRUD operations for Budget entity
 * All SQL queries explicitly show field mappings for clarity
 */
public class BudgetService implements CRUDInterface<Budget> {
    private final Connection connection;
    
    public BudgetService() {
        this.connection = Database.getInstance().getConnection();
    }
    
    /**
     * Create a new budget in the database
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate, periodType, walletId
     */
    @Override
    public void create(Budget budget) {
        String sql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate, periodType, walletId) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimitAmount());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStartDate());
            pstmt.setString(6, budget.getEndDate());
            pstmt.setString(7, budget.getPeriodType() != null ? budget.getPeriodType().name() : "MONTHLY");
            pstmt.setString(8, budget.getWalletId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating budget: " + e.getMessage());
        }
    }
    
    /**
     * Read a single budget by id
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate, periodType, walletId
     */
    @Override
    public Budget read(String id) {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate, periodType, walletId " +
                     "FROM Budget WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = new Budget();
                    budget.setId(rs.getString("id"));
                    budget.setName(rs.getString("name"));
                    budget.setLimitAmount(rs.getDouble("limitAmount"));
                    budget.setBalance(rs.getDouble("balance"));
                    budget.setStartDate(rs.getString("startDate"));
                    budget.setEndDate(rs.getString("endDate"));
                    
                    String periodTypeStr = rs.getString("periodType");
                    if (periodTypeStr != null) {
                        try {
                            budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                        } catch (IllegalArgumentException e) {
                            budget.setPeriodType(Budget.PeriodType.MONTHLY);
                        }
                    }
                    
                    budget.setWalletId(rs.getString("walletId"));
                    return budget;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading budget: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read all budgets from the database
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate, periodType, walletId
     */
    public List<Budget> readAll() {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate, periodType, walletId " +
                     "FROM Budget ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getString("id"));
                budget.setName(rs.getString("name"));
                budget.setLimitAmount(rs.getDouble("limitAmount"));
                budget.setBalance(rs.getDouble("balance"));
                budget.setStartDate(rs.getString("startDate"));
                budget.setEndDate(rs.getString("endDate"));
                
                String periodTypeStr = rs.getString("periodType");
                if (periodTypeStr != null) {
                    try {
                        budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                    } catch (IllegalArgumentException e) {
                        budget.setPeriodType(Budget.PeriodType.MONTHLY);
                    }
                }
                
                budget.setWalletId(rs.getString("walletId"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    /**
     * Update an existing budget
     * Explicit fields: name, limitAmount, balance, startDate, endDate, periodType, walletId (WHERE id = ?)
     */
    @Override
    public void update(Budget budget) {
        String sql = "UPDATE Budget SET name = ?, limitAmount = ?, balance = ?, startDate = ?, " +
                     "endDate = ?, periodType = ?, walletId = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimitAmount());
            pstmt.setDouble(3, budget.getBalance());
            pstmt.setString(4, budget.getStartDate());
            pstmt.setString(5, budget.getEndDate());
            pstmt.setString(6, budget.getPeriodType() != null ? budget.getPeriodType().name() : "MONTHLY");
            pstmt.setString(7, budget.getWalletId());
            pstmt.setString(8, budget.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
        }
    }
    
    /**
     * Delete a budget by id
     */
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM Budget WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
    }
    public List<Budget> getActiveBudgets() {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate, periodType, walletId " +
                     "FROM Budget WHERE date('now') BETWEEN startDate AND endDate " +
                     "ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getString("id"));
                budget.setName(rs.getString("name"));
                budget.setLimitAmount(rs.getDouble("limitAmount"));
                budget.setBalance(rs.getDouble("balance"));
                budget.setStartDate(rs.getString("startDate"));
                budget.setEndDate(rs.getString("endDate"));
                
                String periodTypeStr = rs.getString("periodType");
                if (periodTypeStr != null) {
                    try {
                        budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                    } catch (IllegalArgumentException e) {
                        budget.setPeriodType(Budget.PeriodType.MONTHLY);
                    }
                }
                
                budget.setWalletId(rs.getString("walletId"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading active budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    /**
     * Get budgets by wallet ID
     */
    public List<Budget> getBudgetsByWallet(String walletId) {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate, periodType, walletId " +
                     "FROM Budget WHERE walletId = ? ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, walletId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget();
                    budget.setId(rs.getString("id"));
                    budget.setName(rs.getString("name"));
                    budget.setLimitAmount(rs.getDouble("limitAmount"));
                    budget.setBalance(rs.getDouble("balance"));
                    budget.setStartDate(rs.getString("startDate"));
                    budget.setEndDate(rs.getString("endDate"));
                    
                    String periodTypeStr = rs.getString("periodType");
                    if (periodTypeStr != null) {
                        try {
                            budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                        } catch (IllegalArgumentException e) {
                            budget.setPeriodType(Budget.PeriodType.MONTHLY);
                        }
                    }
                    
                    budget.setWalletId(rs.getString("walletId"));
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading budgets by wallet: " + e.getMessage());
        }
        return budgets;
    }
    
    /**
     * Get account-wide budgets (not linked to specific wallet)
     */
    public List<Budget> getAccountWideBudgets() {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate, periodType, walletId " +
                     "FROM Budget WHERE walletId IS NULL ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getString("id"));
                budget.setName(rs.getString("name"));
                budget.setLimitAmount(rs.getDouble("limitAmount"));
                budget.setBalance(rs.getDouble("balance"));
                budget.setStartDate(rs.getString("startDate"));
                budget.setEndDate(rs.getString("endDate"));
                
                String periodTypeStr = rs.getString("periodType");
                if (periodTypeStr != null) {
                    try {
                        budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                    } catch (IllegalArgumentException e) {
                        budget.setPeriodType(Budget.PeriodType.MONTHLY);
                    }
                }
                
                budget.setWalletId(rs.getString("walletId"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading account-wide budgets: " + e.getMessage());
        }
        return budgets;
    }

    /**
     * Get all categories associated with a specific budget
     * Explicit fields: c.id, c.name, c.description, c.type, c.color
     */
    public List<Category> getCategoriesForBudget(String budgetId) {
        String sql = "SELECT DISTINCT c.id, c.name, c.description, c.type, c.color " +
                     "FROM Category c " +
                     "INNER JOIN Budget_Category bc ON c.id = bc.categoryID " +
                     "WHERE bc.budgetID = ? " +
                     "ORDER BY c.name";
        List<Category> categories = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getString("id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    
                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        try {
                            category.setType(Category.Type.valueOf(typeStr));
                        } catch (IllegalArgumentException e) {
                            category.setType(Category.Type.EXPENSE);
                        }
                    }
                    
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories for budget: " + e.getMessage());
        }
        return categories;
    }
    
    /**
     * Get all budget-category relationships for a budget including category limits
     */
    public List<BudgetCategory> getBudgetCategoriesForBudget(String budgetId) {
        String sql = "SELECT budgetID, categoryID, categoryLimit " +
                     "FROM Budget_Category WHERE budgetID = ?";
        List<BudgetCategory> budgetCategories = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BudgetCategory bc = new BudgetCategory();
                    bc.setBudgetId(rs.getString("budgetID"));
                    bc.setCategoryId(rs.getString("categoryID"));
                    
                    double limit = rs.getDouble("categoryLimit");
                    bc.setCategoryLimit(rs.wasNull() ? null : limit);
                    
                    budgetCategories.add(bc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting budget categories: " + e.getMessage());
        }
        return budgetCategories;
    }

    /**
     * Get all budgets associated with a specific category
     * Explicit fields: b.id, b.name, b.limitAmount, b.balance, b.startDate, b.endDate, b.periodType, b.walletId
     */
    public List<Budget> getBudgetsForCategory(String categoryId) {
        String sql = "SELECT DISTINCT b.id, b.name, b.limitAmount, b.balance, b.startDate, b.endDate, b.periodType, b.walletId " +
                     "FROM Budget b " +
                     "INNER JOIN Budget_Category bc ON b.id = bc.budgetID " +
                     "WHERE bc.categoryID = ? " +
                     "ORDER BY b.name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = new Budget();
                    budget.setId(rs.getString("id"));
                    budget.setName(rs.getString("name"));
                    budget.setLimitAmount(rs.getDouble("limitAmount"));
                    budget.setBalance(rs.getDouble("balance"));
                    budget.setStartDate(rs.getString("startDate"));
                    budget.setEndDate(rs.getString("endDate"));
                    
                    String periodTypeStr = rs.getString("periodType");
                    if (periodTypeStr != null) {
                        try {
                            budget.setPeriodType(Budget.PeriodType.valueOf(periodTypeStr));
                        } catch (IllegalArgumentException e) {
                            budget.setPeriodType(Budget.PeriodType.MONTHLY);
                        }
                    }
                    
                    budget.setWalletId(rs.getString("walletId"));
                    budgets.add(budget);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting budgets for category: " + e.getMessage());
        }
        return budgets;
    }

    /**
     * Check if a category is linked to a budget
     * Returns true if the relationship exists in Budget_Category junction table
     */
    public boolean isCategoryInBudget(String budgetId, String categoryId) {
        String sql = "SELECT COUNT(*) FROM Budget_Category " +
                     "WHERE budgetID = ? AND categoryID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking category in budget: " + e.getMessage());
        }
        return false;
    }

    /**
     * Add a category to a budget (INSERT into Budget_Category junction table)
     * Handles duplicate key errors gracefully
     */
    public void addCategoryToBudget(String budgetId, String categoryId) {
        addCategoryToBudget(budgetId, categoryId, null);
    }
    
    /**
     * Add a category to a budget with specific category limit
     */
    public void addCategoryToBudget(String budgetId, String categoryId, Double categoryLimit) {
        String sql = "INSERT INTO Budget_Category (budgetID, categoryID, categoryLimit) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, categoryId);
            if (categoryLimit != null) {
                pstmt.setDouble(3, categoryLimit);
            } else {
                pstmt.setNull(3, Types.DOUBLE);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("PRIMARY KEY")) {
                System.out.println("Category already linked to budget.");
            } else {
                System.err.println("Error adding category to budget: " + e.getMessage());
            }
        }
    }
    
    /**
     * Update the category limit for a budget-category relationship
     */
    public void updateCategoryLimit(String budgetId, String categoryId, Double categoryLimit) {
        String sql = "UPDATE Budget_Category SET categoryLimit = ? WHERE budgetID = ? AND categoryID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (categoryLimit != null) {
                pstmt.setDouble(1, categoryLimit);
            } else {
                pstmt.setNull(1, Types.DOUBLE);
            }
            pstmt.setString(2, budgetId);
            pstmt.setString(3, categoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating category limit: " + e.getMessage());
        }
    }
    
    /**
     * Get the category limit for a specific budget-category relationship
     */
    public Double getCategoryLimit(String budgetId, String categoryId) {
        String sql = "SELECT categoryLimit FROM Budget_Category WHERE budgetID = ? AND categoryID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double limit = rs.getDouble("categoryLimit");
                    return rs.wasNull() ? null : limit;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting category limit: " + e.getMessage());
        }
        return null;
    }

    /**
     * Remove a category from a budget (DELETE from Budget_Category junction table)
     */
    public void removeCategoryFromBudget(String budgetId, String categoryId) {
        String sql = "DELETE FROM Budget_Category WHERE budgetID = ? AND categoryID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, categoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing category from budget: " + e.getMessage());
        }
    }

    /**
     * Replace all categories for a budget
     * Removes all existing category links and adds new ones (transactional)
     */
    public void setCategoriesForBudget(String budgetId, List<String> categoryIds) {
        try {
            connection.setAutoCommit(false);
            
            // Step 1: Delete all existing category links
            String deleteSql = "DELETE FROM Budget_Category WHERE budgetID = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, budgetId);
                deleteStmt.executeUpdate();
            }
            
            // Step 2: Insert new category links
            String insertSql = "INSERT INTO Budget_Category (budgetID, categoryID, categoryLimit) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                for (String categoryId : categoryIds) {
                    insertStmt.setString(1, budgetId);
                    insertStmt.setString(2, categoryId);
                    insertStmt.setNull(3, Types.DOUBLE);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
            
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error setting categories for budget: " + e.getMessage());
        }
    }

    /**
     * Remove all category links for a budget
     * Useful when deleting or resetting a budget's categories
     */
    public void removeAllCategoriesFromBudget(String budgetId) {
        String sql = "DELETE FROM Budget_Category WHERE budgetID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing all categories from budget: " + e.getMessage());
        }
    }
    
    // ==================== SPENDING TRACKING METHODS ====================
    
    /**
     * Calculate total spending for a budget within its date range
     * Only counts expense transactions (income = 0)
     */
    public double getTotalSpentForBudget(String budgetId) {
        Budget budget = read(budgetId);
        if (budget == null) return 0.0;
        
        String sql;
        PreparedStatement pstmt;
        
        try {
            // If budget is wallet-specific, filter by walletId
            if (budget.getWalletId() != null && !budget.getWalletId().isEmpty()) {
                sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                      "FROM transaction_records " +
                      "WHERE income = 0 AND walletId = ? " +
                      "AND createTime BETWEEN ? AND ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, budget.getWalletId());
                pstmt.setString(2, budget.getStartDate());
                pstmt.setString(3, budget.getEndDate());
            } else {
                // Account-wide budget: sum all transactions
                sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                      "FROM transaction_records " +
                      "WHERE income = 0 " +
                      "AND createTime BETWEEN ? AND ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, budget.getStartDate());
                pstmt.setString(2, budget.getEndDate());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error calculating total spent: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Calculate spending for a specific category within a budget's date range
     */
    public double getSpentForCategory(String budgetId, String categoryId) {
        Budget budget = read(budgetId);
        if (budget == null) return 0.0;
        
        String sql;
        PreparedStatement pstmt;
        
        try {
            // If budget is wallet-specific, filter by walletId
            if (budget.getWalletId() != null && !budget.getWalletId().isEmpty()) {
                sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                      "FROM transaction_records " +
                      "WHERE income = 0 AND categoryId = ? AND walletId = ? " +
                      "AND createTime BETWEEN ? AND ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, categoryId);
                pstmt.setString(2, budget.getWalletId());
                pstmt.setString(3, budget.getStartDate());
                pstmt.setString(4, budget.getEndDate());
            } else {
                // Account-wide budget
                sql = "SELECT COALESCE(SUM(amount), 0) as total " +
                      "FROM transaction_records " +
                      "WHERE income = 0 AND categoryId = ? " +
                      "AND createTime BETWEEN ? AND ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, categoryId);
                pstmt.setString(2, budget.getStartDate());
                pstmt.setString(3, budget.getEndDate());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error calculating category spending: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get spending breakdown by category for a budget
     * Returns BudgetCategory objects with spent, remaining, and percentage
     */
    public List<BudgetCategory> getSpendingBreakdown(String budgetId) {
        List<BudgetCategory> breakdown = new ArrayList<>();
        Budget budget = read(budgetId);
        if (budget == null) return breakdown;
        
        // Get all categories linked to this budget
        List<BudgetCategory> budgetCategories = getBudgetCategoriesForBudget(budgetId);
        
        for (BudgetCategory bc : budgetCategories) {
            // Get category details
            CategoryService categoryService = new CategoryService();
            Category category = categoryService.read(bc.getCategoryId());
            if (category == null) continue;
            
            // Calculate spent amount
            double spent = getSpentForCategory(budgetId, bc.getCategoryId());
            
            // Create spending object with calculated values
            BudgetCategory spending = new BudgetCategory(
                budgetId,
                bc.getCategoryId(),
                category.getName(),
                bc.getCategoryLimit(),
                spent
            );
            
            breakdown.add(spending);
        }
        
        return breakdown;
    }
    
    /**
     * Calculate remaining budget for entire budget
     */
    public double getRemainingBudget(String budgetId) {
        Budget budget = read(budgetId);
        if (budget == null) return 0.0;
        
        double totalSpent = getTotalSpentForBudget(budgetId);
        return budget.getLimitAmount() - totalSpent;
    }
    
    /**
     * Calculate remaining budget for a specific category
     */
    public double getRemainingForCategory(String budgetId, String categoryId) {
        Double categoryLimit = getCategoryLimit(budgetId, categoryId);
        if (categoryLimit == null) {
            // No specific limit, use overall budget limit
            Budget budget = read(budgetId);
            if (budget == null) return 0.0;
            return getRemainingBudget(budgetId);
        }
        
        double spent = getSpentForCategory(budgetId, categoryId);
        return categoryLimit - spent;
    }
    
    /**
     * Check if a budget is over its limit
     */
    public boolean isOverBudget(String budgetId) {
        return getRemainingBudget(budgetId) < 0;
    }
    
    /**
     * Check if a category within a budget is over its limit
     */
    public boolean isCategoryOverBudget(String budgetId, String categoryId) {
        return getRemainingForCategory(budgetId, categoryId) < 0;
    }
    
    /**
     * Get percentage of budget used
     */
    public double getBudgetUsagePercentage(String budgetId) {
        Budget budget = read(budgetId);
        if (budget == null || budget.getLimitAmount() == 0) return 0.0;
        
        double spent = getTotalSpentForBudget(budgetId);
        return (spent / budget.getLimitAmount()) * 100.0;
    }
    
    /**
     * Get percentage of category budget used
     */
    public double getCategoryUsagePercentage(String budgetId, String categoryId) {
        Double categoryLimit = getCategoryLimit(budgetId, categoryId);
        if (categoryLimit == null || categoryLimit == 0) return 0.0;
        
        double spent = getSpentForCategory(budgetId, categoryId);
        return (spent / categoryLimit) * 100.0;
    }
}
                