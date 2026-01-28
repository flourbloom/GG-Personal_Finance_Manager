package gitgud.pfm.services;

import gitgud.pfm.Models.Budget;
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
     * Explicit fields: id, name, limits, balance, start_date, end_date, trackedCategories
     */
    @Override
    public void create(Budget budget) {
        String sql = "INSERT INTO Budget (id, name, limits, balance, start_date, end_date, trackedCategories) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimits());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStart_date());
            pstmt.setString(6, budget.getEnd_date());
            pstmt.setString(7, budget.getTrackedCategories());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating budget: " + e.getMessage());
        }
    }
    
    /**
     * Read a single budget by id
     * Explicit fields: id, name, limits, balance, start_date, end_date, trackedCategories
     */
    @Override
    public Budget read(String id) {
        String sql = "SELECT id, name, limits, balance, start_date, end_date, trackedCategories " +
                     "FROM Budget WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = new Budget();
                    budget.setId(rs.getString("id"));
                    budget.setName(rs.getString("name"));
                    budget.setLimits(rs.getDouble("limits"));
                    budget.setBalance(rs.getDouble("balance"));
                    budget.setStart_date(rs.getString("start_date"));
                    budget.setEnd_date(rs.getString("end_date"));
                    budget.setTrackedCategories(rs.getString("trackedCategories"));
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
     * Explicit fields: id, name, limits, balance, start_date, end_date, trackedCategories
     */
    public List<Budget> readAll() {
        String sql = "SELECT id, name, limits, balance, start_date, end_date, trackedCategories " +
                     "FROM Budget ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getString("id"));
                budget.setName(rs.getString("name"));
                budget.setLimits(rs.getDouble("limits"));
                budget.setBalance(rs.getDouble("balance"));
                budget.setStart_date(rs.getString("start_date"));
                budget.setEnd_date(rs.getString("end_date"));
                budget.setTrackedCategories(rs.getString("trackedCategories"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    /**
     * Update an existing budget
     * Explicit fields: name, limits, balance, start_date, end_date, trackedCategories (WHERE id = ?)
     */
    @Override
    public void update(Budget budget) {
        String sql = "UPDATE Budget SET name = ?, limits = ?, balance = ?, start_date = ?, " +
                     "end_date = ?, trackedCategories = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimits());
            pstmt.setDouble(3, budget.getBalance());
            pstmt.setString(4, budget.getStart_date());
            pstmt.setString(5, budget.getEnd_date());
            pstmt.setString(6, budget.getTrackedCategories());
            pstmt.setString(7, budget.getId());
            
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
     * Get budgets that are currently active (current date within start_date and end_date)
     */
    public List<Budget> getActiveBudgets() {
        String sql = "SELECT id, name, limits, balance, start_date, end_date, trackedCategories " +
                     "FROM Budget WHERE date('now') BETWEEN start_date AND end_date " +
                     "ORDER BY name";
        List<Budget> budgets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Budget budget = new Budget();
                budget.setId(rs.getString("id"));
                budget.setName(rs.getString("name"));
                budget.setLimits(rs.getDouble("limits"));
                budget.setBalance(rs.getDouble("balance"));
                budget.setStart_date(rs.getString("start_date"));
                budget.setEnd_date(rs.getString("end_date"));
                budget.setTrackedCategories(rs.getString("trackedCategories"));
                budgets.add(budget);
            }
        } catch (SQLException e) {
            System.err.println("Error reading active budgets: " + e.getMessage());
        }
        return budgets;
    }
}
