package gitgud.pfm.services;

import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalService - Explicit CRUD operations for Goal entity
 * All SQL queries explicitly show field mappings for clarity
 */
public class GoalService implements CRUDInterface<Goal> {
    private final Connection connection;
    
    public GoalService() {
        this.connection = Database.getInstance().getConnection();
    }
    
    /**
     * Create a new goal in the database
     * Explicit fields: id, name, target, balance (current), deadline, priority, createAt
     */
    @Override
    public void create(Goal goal) {
        String sql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTarget());
            pstmt.setDouble(4, goal.getBalance()); // balance is "current" amount
            pstmt.setString(5, goal.getDeadline());
            pstmt.setDouble(6, goal.getPriority());
            pstmt.setString(7, goal.getCreateTime());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating goal: " + e.getMessage());
        }
    }
    
    /**
     * Read a single goal by id
     * Explicit fields: id, name, target, balance (current), deadline, priority, createAt
     */
    @Override
    public Goal read(String id) {
        String sql = "SELECT id, name, target, balance, deadline, priority, createAt " +
                     "FROM Goal WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getString("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTarget(rs.getDouble("target"));
                    goal.setBalance(rs.getDouble("balance")); // balance is "current"
                    goal.setDeadline(rs.getString("deadline"));
                    goal.setPriority(rs.getDouble("priority"));
                    goal.setCreateTime(rs.getString("createAt"));
                    return goal;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading goal: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read all goals from the database
     */
    public List<Goal> readAll() {
        String sql = "SELECT id, name, target, balance, deadline, priority, createAt " +
                     "FROM Goal ORDER BY priority DESC, deadline";
        List<Goal> goals = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Goal goal = new Goal();
                goal.setId(rs.getString("id"));
                goal.setName(rs.getString("name"));
                goal.setTarget(rs.getDouble("target"));
                goal.setBalance(rs.getDouble("balance")); // balance is "current"
                goal.setDeadline(rs.getString("deadline"));
                goal.setPriority(rs.getDouble("priority"));
                goal.setCreateTime(rs.getString("createAt"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all goals: " + e.getMessage());
        }
        return goals;
    }
    
    /**
     * Update an existing goal (WHERE id = ?)
     */
    @Override
    public void update(Goal goal) {
        String sql = "UPDATE Goal SET name = ?, target = ?, balance = ?, deadline = ?, " +
                     "priority = ?, createAt = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTarget());
            pstmt.setDouble(3, goal.getBalance());
            pstmt.setString(4, goal.getDeadline());
            pstmt.setDouble(5, goal.getPriority());
            pstmt.setString(6, goal.getCreateTime());
            pstmt.setString(7, goal.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating goal: " + e.getMessage());
        }
    }
    
    /**
     * Delete a goal by id
     */
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM Goal WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
        }
    }
    
    /**
     * Get goals that are still active (not yet reached target)
     */
    public List<Goal> getActiveGoals() {
        String sql = "SELECT id, name, target, balance, deadline, priority, createAt " +
                     "FROM Goal WHERE balance < target " +
                     "ORDER BY priority DESC, deadline";
        List<Goal> goals = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Goal goal = new Goal();
                goal.setId(rs.getString("id"));
                goal.setName(rs.getString("name"));
                goal.setTarget(rs.getDouble("target"));
                goal.setBalance(rs.getDouble("balance"));
                goal.setDeadline(rs.getString("deadline"));
                goal.setPriority(rs.getDouble("priority"));
                goal.setCreateTime(rs.getString("createAt"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error reading active goals: " + e.getMessage());
        }
        return goals;
    }
    
    
    public List<Goal> getInactiveGoals() {
        String sql = "SELECT id, name, target, balance, deadline, priority, createAt " +
                     "FROM Goal WHERE balance >= target " +
                     "ORDER BY priority DESC, deadline";
        List<Goal> goals = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Goal goal = new Goal();
                goal.setId(rs.getString("id"));
                goal.setName(rs.getString("name"));
                goal.setTarget(rs.getDouble("target"));
                goal.setBalance(rs.getDouble("balance"));
                goal.setDeadline(rs.getString("deadline"));
                goal.setPriority(rs.getDouble("priority"));
                goal.setCreateTime(rs.getString("createAt"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error reading inactive goals: " + e.getMessage());
        }
        return goals;
    }
    
    /**
     * Get all goals ordered: active goals first, then inactive goals
     */
    public List<Goal> getAllGoalsOrdered() {
        List<Goal> result = new ArrayList<>();
        result.addAll(getActiveGoals());
        result.addAll(getInactiveGoals());
        return result;
    }
    
    public List<Goal> findByName(String namePattern) {
		List<Goal> goals = new ArrayList<>();
		String sql = "SELECT * FROM Goal WHERE name LIKE ? ORDER BY name";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setString(1, namePattern);
            try (ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				Goal goal = new Goal();
                    goal.setId(rs.getString("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTarget(rs.getDouble("target"));
                    goal.setBalance(rs.getDouble("balance"));
                    goal.setDeadline(rs.getString("deadline"));
                    goal.setPriority(rs.getDouble("priority"));
                    goal.setCreateTime(rs.getString("createAt"));
                    goals.add(goal);
			}
        }
		} catch (SQLException e) {
			System.out.println("Error searching goals by name: " + e.getMessage());
		}
		return goals;
	}
}
