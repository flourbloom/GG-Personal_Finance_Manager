package gitgud.pfm.CRUDs;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.services.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class GoalCRUD {
    
    public void saveGoal(Goal goal) {
        String insertSql = "INSERT INTO Goal(id, name, target, current, deadline, priority, createAt) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, goal.getId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTarget());
            pstmt.setDouble(4, goal.getCurrent());
            pstmt.setString(5, goal.getDeadline());
            pstmt.setDouble(6, goal.getPriority());
            pstmt.setString(7, goal.getCreateAt());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving goal: " + e.getMessage());
        }
    }
    
    public List<Goal> ShowAllGoals() {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM Goal ORDER BY priority DESC";
        
        try (Connection conn = Database.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                goals.add(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving goals: " + e.getMessage());
        }
        return goals;
    }
    
    public void deleteGoal(String goalId) {
        String sql = "DELETE FROM Goal WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goalId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting goal: " + e.getMessage());
        }
    }
    
    public void updateGoal(Goal goal) {
        String sql = "UPDATE Goal SET name = ?, target = ?, current = ?, deadline = ?, priority = ?, createAt = ? WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTarget());
            pstmt.setDouble(3, goal.getCurrent());
            pstmt.setString(4, goal.getDeadline());
            pstmt.setDouble(5, goal.getPriority());
            pstmt.setString(6, goal.getCreateAt());
            pstmt.setString(7, goal.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating goal: " + e.getMessage());
        }
    }
    
    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        return new Goal(
            rs.getString("id"),
            rs.getString("name"),
            rs.getDouble("target"),
            rs.getDouble("current"),
            rs.getString("deadline"),
            rs.getDouble("priority"),
            rs.getString("createAt")
        );
    }
    
    public Goal getGoalById(String goalId) {
        String sql = "SELECT * FROM Goal WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToGoal(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving goal: " + e.getMessage());
        }
        return null;
    }
    
    public boolean exists(String goalId) {
        String sql = "SELECT COUNT(*) FROM Goal WHERE id = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goalId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking goal existence: " + e.getMessage());
        }
        return false;
    }
}
