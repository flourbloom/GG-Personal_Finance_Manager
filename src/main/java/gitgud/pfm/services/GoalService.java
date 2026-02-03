package gitgud.pfm.services;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GoalService - Refactored CRUD operations for Goal entity
 * 
 * KEY CHANGES (per GOAL_REFACTOR_INSTRUCTIONS.md):
 * - Removed 'balance' from all SQL queries (no longer stored in database)
 * - Added computeGoalProgress() to calculate balance from transaction_records
 * - Goal.balance is now a READ-ONLY computed field
 * - All SQL queries explicitly show field mappings for clarity
 */
public class GoalService implements CRUDInterface<Goal> {
    private final Connection connection;

    public GoalService() {
        this.connection = Database.getInstance().getConnection();
    }

    /**
     * Create a new goal in the database
     * Explicit fields: id, name, target, deadline, priority, createAt
     * NOTE: balance is NOT stored - it's computed from transactions
     */
    @Override
    public void create(Goal goal) {
        String sql = "INSERT INTO Goal (id, name, target, deadline, priority, createAt) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTarget());
            pstmt.setString(4, goal.getDeadline());
            pstmt.setDouble(5, goal.getPriority());
            pstmt.setString(6, goal.getCreateTime());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating goal: " + e.getMessage());
        }
    }

    /**
     * Read a single goal by id
     * Explicit fields: id, name, target, deadline, priority, createAt
     * NOTE: balance is computed AFTER retrieval via computeGoalProgress()
     */
    @Override
    public Goal read(String id) {
        String sql = "SELECT id, name, target, deadline, priority, createAt " +
                "FROM Goal WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getString("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTarget(rs.getDouble("target"));
                    goal.setDeadline(rs.getString("deadline"));
                    goal.setPriority(rs.getDouble("priority"));
                    goal.setCreateTime(rs.getString("createAt"));

                    // Compute balance from transactions (read-only)
                    double computedBalance = computeGoalProgress(goal.getId());
                    goal.setBalance(computedBalance); // This will be ignored by the overridden setter

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
     * Explicit fields: id, name, target, deadline, priority, createAt
     * NOTE: balance is computed for each goal from transactions
     */
    public List<Goal> readAll() {
        String sql = "SELECT id, name, target, deadline, priority, createAt " +
                "FROM Goal ORDER BY priority DESC, deadline";
        List<Goal> goals = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Goal goal = new Goal();
                goal.setId(rs.getString("id"));
                goal.setName(rs.getString("name"));
                goal.setTarget(rs.getDouble("target"));
                goal.setDeadline(rs.getString("deadline"));
                goal.setPriority(rs.getDouble("priority"));
                goal.setCreateTime(rs.getString("createAt"));

                // Compute balance from transactions (read-only)
                double computedBalance = computeGoalProgress(goal.getId());
                goal.setBalance(computedBalance);

                goals.add(goal);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all goals: " + e.getMessage());
        }
        return goals;
    }

    /**
     * Update an existing goal
     * Explicit fields: name, target, deadline, priority, createAt (WHERE id = ?)
     * NOTE: balance is NEVER updated - it's always computed from transactions
     */
    @Override
    public void update(Goal goal) {
        String sql = "UPDATE Goal SET name = ?, target = ?, deadline = ?, " +
                "priority = ?, createAt = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTarget());
            pstmt.setString(3, goal.getDeadline());
            pstmt.setDouble(4, goal.getPriority());
            pstmt.setString(5, goal.getCreateTime());
            pstmt.setString(6, goal.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating goal: " + e.getMessage());
        }
    }

    /**
     * Delete a goal by id
     * 
     * IMPORTANT: Decide what to do with orphaned transactions:
     * - Option A: CASCADE DELETE (deletes transactions too) - Data loss!
     * - Option B: SOFT DELETE (mark goal as archived) - Preserves history
     * - Option C: UNLINK (set transaction.goalId = NULL) - Preserves history
     * 
     * Current implementation: Simple delete (relies on database constraints)
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

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPUTED BALANCE METHODS (NEW - Core of the refactor)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Compute goal progress by summing all transactions linked to this goal
     * 
     * FORMULA: balance = SUM(amount) FROM transaction_records WHERE goalId = goalId
     * 
     * This is the authoritative source of truth for goal.getBalance()
     * 
     * @param goalId The goal ID to compute progress for
     * @return The sum of all transaction amounts allocated to this goal
     */
    public double computeGoalProgress(String goalId) {
        String sql = "SELECT SUM(amount) as total FROM transaction_records WHERE goalId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goalId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    // Handle NULL case (no transactions linked to this goal)
                    return rs.wasNull() ? 0.0 : total;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error computing goal progress: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Get goal progress percentage
     * 
     * @param goalId The goal ID
     * @return Progress as percentage (0-100+)
     */
    public double getGoalProgressPercentage(String goalId) {
        Goal goal = read(goalId);
        if (goal == null || goal.getTarget() <= 0) {
            return 0.0;
        }

        double current = computeGoalProgress(goalId);
        return (current / goal.getTarget()) * 100.0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILTERED QUERY METHODS (Updated to use computed balance)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get goals that are still active (not yet reached target)
     * NOTE: Must compute balance for each goal to determine if active
     */
    public List<Goal> getActiveGoals() {
        List<Goal> allGoals = readAll(); // Already computes balance
        List<Goal> activeGoals = new ArrayList<>();

        for (Goal goal : allGoals) {
            // Use the computed balance (already set by readAll)
            if (goal.getBalance() < goal.getTarget()) {
                activeGoals.add(goal);
            }
        }

        return activeGoals;
    }

    /**
     * Get goals that have reached or exceeded target
     * NOTE: Must compute balance for each goal to determine if inactive
     */
    public List<Goal> getInactiveGoals() {
        List<Goal> allGoals = readAll(); // Already computes balance
        List<Goal> inactiveGoals = new ArrayList<>();

        for (Goal goal : allGoals) {
            // Use the computed balance (already set by readAll)
            if (goal.getBalance() >= goal.getTarget()) {
                inactiveGoals.add(goal);
            }
        }

        return inactiveGoals;
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

    /**
     * Find goals by name pattern
     * NOTE: Balance is computed for each matching goal
     */
    public List<Goal> findByName(String namePattern) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT id, name, target, deadline, priority, createAt " +
                "FROM Goal WHERE name LIKE ? ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, namePattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getString("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTarget(rs.getDouble("target"));
                    goal.setDeadline(rs.getString("deadline"));
                    goal.setPriority(rs.getDouble("priority"));
                    goal.setCreateTime(rs.getString("createAt"));

                    // Compute balance from transactions
                    double computedBalance = computeGoalProgress(goal.getId());
                    goal.setBalance(computedBalance);

                    goals.add(goal);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching goals by name: " + e.getMessage());
        }
        return goals;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADDITIONAL UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get the number of transactions allocated to a specific goal
     * 
     * @param goalId The goal ID
     * @return Number of transactions linked to this goal
     */
    public int getTransactionCount(String goalId) {
        String sql = "SELECT COUNT(*) as count FROM transaction_records WHERE goalId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goalId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting transactions for goal: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Check if a goal has any transactions allocated to it
     * Useful before deletion to warn user
     * 
     * @param goalId The goal ID
     * @return true if goal has transactions, false otherwise
     */
    public boolean hasAllocatedTransactions(String goalId) {
        return getTransactionCount(goalId) > 0;
    }
}