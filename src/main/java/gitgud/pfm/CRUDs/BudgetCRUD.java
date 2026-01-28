package gitgud.pfm.CRUDs;

import gitgud.pfm.Models.Budget;
import gitgud.pfm.services.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class BudgetImplement {
    
    public void saveBudget(Budget budget) {
        String insertSql = "INSERT INTO Budget(id, name, limits, balance, start_date, end_date, trackedCategories) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimits());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStart_date());
            pstmt.setString(6, budget.getEnd_date());
            pstmt.setString(7, budget.getTrackedCategories());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving budget: " + e.getMessage());
        }
    }
    
    public List<Budget> ShowAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM Budget ORDER BY start_date DESC";
        
        try (Connection conn = Database.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    public void deleteBudget(String budgetId) {
        String sql = "DELETE FROM Budget WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting budget: " + e.getMessage());
        }
    }
    
    public void updateBudget(Budget budget) {
        String sql = "UPDATE Budget SET name = ?, limits = ?, balance = ?, start_date = ?, end_date = ?, trackedCategories = ? WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budget.getName());
            pstmt.setDouble(2, budget.getLimits());
            pstmt.setDouble(3, budget.getBalance());
            pstmt.setString(4, budget.getStart_date());
            pstmt.setString(5, budget.getEnd_date());
            pstmt.setString(6, budget.getTrackedCategories());
            pstmt.setString(7, budget.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating budget: " + e.getMessage());
        }
    }
    
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        return new Budget(
            rs.getString("id"),
            rs.getString("name"),
            rs.getDouble("limits"),
            rs.getDouble("balance"),
            rs.getString("start_date"),
            rs.getString("end_date"),
            rs.getString("trackedCategories")
        );
    }
    
    public Budget getBudgetById(String budgetId) {
        String sql = "SELECT * FROM Budget WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBudget(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving budget: " + e.getMessage());
        }
        return null;
    }
    
    public List<Budget> getActiveBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM Budget WHERE date('now') BETWEEN start_date AND end_date";
        
        try (Connection conn = Database.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving active budgets: " + e.getMessage());
        }
        return budgets;
    }
    
    public boolean exists(String budgetId) {
        String sql = "SELECT COUNT(*) FROM Budget WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking budget existence: " + e.getMessage());
        }
        return false;
    }
}
