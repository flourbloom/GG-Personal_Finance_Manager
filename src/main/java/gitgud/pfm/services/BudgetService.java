package gitgud.pfm.services;

import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Transaction;
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
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate
     */
    @Override
    public void create(Budget budget) {
        String sql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimitAmount());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStartDate());
            pstmt.setString(6, budget.getEndDate());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating budget: " + e.getMessage());
        }
    }
    
    /**
     * Read a single budget by id
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate
     */
    @Override
    public Budget read(String id) {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate " +
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
     * Explicit fields: id, name, limitAmount, balance, startDate, endDate
     */
    public List<Budget> readAll() {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate " +
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
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    /**
     * Update an existing budget
     * Explicit fields: name, limitAmount, balance, startDate, endDate (WHERE id = ?)
     */
    @Override
    public void update(Budget budget) {
        String sql = "UPDATE Budget SET name = ?, limitAmount = ?, balance = ?, startDate = ?, " +
                     "endDate = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimitAmount());
            pstmt.setDouble(3, budget.getBalance());
            pstmt.setString(4, budget.getStartDate());
            pstmt.setString(5, budget.getEndDate());
            pstmt.setString(6, budget.getId());
            
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
    
    /**
     * Get budgets that are currently active (current date within startDate and endDate)
     */
    public List<Budget> getActiveBudgets() {
        String sql = "SELECT id, name, limitAmount, balance, startDate, endDate " +
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
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading active budgets: " + e.getMessage());
        }
        return budgets;
    }
    public List<Budget> findByName(String namePattern) {
		List<Budget> budgets = new ArrayList<>();
		String sql = "SELECT * FROM Budget WHERE name LIKE ? ORDER BY name";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            try (ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				Budget budget = new Budget();
                    budget.setId(rs.getString("id"));
                    budget.setName(rs.getString("name"));
                    budget.setLimitAmount(rs.getDouble("limitAmount"));
                    budget.setBalance(rs.getDouble("balance"));
                    budget.setStartDate(rs.getString("startDate"));
                    budget.setEndDate(rs.getString("endDate"));
                    budgets.add(budget);
			}
        }
		} catch (SQLException e) {
			System.out.println("Error searching budgets by name: " + e.getMessage());
		}
		return budgets;
	}
}
