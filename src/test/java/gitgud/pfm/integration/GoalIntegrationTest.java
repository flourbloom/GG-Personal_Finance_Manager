package gitgud.pfm.integration;

import gitgud.pfm.Models.Goal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Goal Database Integration Tests")
class GoalIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Should insert and retrieve goal from database")
    void shouldInsertAndRetrieveGoal() throws SQLException {
        // Arrange
        Goal goal = new Goal(
            "New Car",
            20000.00,
            5000.00,
            "2026-12-31",
            1.0,
            "2026-01-01"
        );

        // Act - Insert
        String insertSql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, goal.getId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTarget());
            pstmt.setDouble(4, goal.getBalance());
            pstmt.setString(5, goal.getDeadline());
            pstmt.setDouble(6, goal.getPriority());
            pstmt.setString(7, goal.getCreateTime());
            pstmt.executeUpdate();
        }

        // Act - Retrieve
        String selectSql = "SELECT * FROM Goal WHERE id = ?";
        Goal retrieved;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, goal.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                retrieved = new Goal();
                retrieved.setId(rs.getString("id"));
                retrieved.setName(rs.getString("name"));
                retrieved.setTarget(rs.getDouble("target"));
                retrieved.setBalance(rs.getDouble("balance"));
                retrieved.setDeadline(rs.getString("deadline"));
                retrieved.setPriority(rs.getDouble("priority"));
                retrieved.setCreateTime(rs.getString("createAt"));
            }
        }

        // Assert
        assertThat(retrieved.getId()).isEqualTo(goal.getId());
        assertThat(retrieved.getName()).isEqualTo(goal.getName());
        assertThat(retrieved.getTarget()).isEqualTo(goal.getTarget());
        assertThat(retrieved.getBalance()).isEqualTo(goal.getBalance());
        assertThat(retrieved.getDeadline()).isEqualTo(goal.getDeadline());
        assertThat(retrieved.getPriority()).isEqualTo(goal.getPriority());
    }

    @Test
    @DisplayName("Should track goal progress over time")
    void shouldTrackGoalProgressOverTime() throws SQLException {
        // Arrange
        Goal goal = new Goal("Vacation", 5000.00, 0.00, "2026-06-30", 1.0, "2026-01-01");
        insertGoalObject(goal);

        // Act - Simulate contributions
        updateGoalBalance(goal.getId(), 1000.00);
        updateGoalBalance(goal.getId(), 2500.00);
        updateGoalBalance(goal.getId(), 4000.00);

        // Assert
        Goal updated = getGoalById(goal.getId());
        assertThat(updated.getBalance()).isEqualTo(4000.00);
        
        double progress = (updated.getBalance() / updated.getTarget()) * 100;
        assertThat(progress).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Should order goals by priority and deadline")
    void shouldOrderGoalsByPriorityAndDeadline() throws SQLException {
        // Arrange
        insertGoal("GOL_001", "High Priority Soon", 1.0, "2026-02-01");
        insertGoal("GOL_002", "Low Priority Later", 3.0, "2026-12-31");
        insertGoal("GOL_003", "Medium Priority Mid", 2.0, "2026-06-30");

        // Act
        String selectSql = "SELECT * FROM Goal ORDER BY priority ASC, deadline ASC";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {

            // Assert - Should be ordered by priority first, then deadline
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("High Priority Soon");

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Medium Priority Mid");

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Low Priority Later");
        }
    }

    @Test
    @DisplayName("Should identify completed goals")
    void shouldIdentifyCompletedGoals() throws SQLException {
        // Arrange
        insertGoalWithProgress("GOL_001", "Completed", 1000.00, 1000.00);
        insertGoalWithProgress("GOL_002", "In Progress", 1000.00, 500.00);
        insertGoalWithProgress("GOL_003", "Exceeded", 1000.00, 1200.00);

        // Act
        String selectSql = "SELECT * FROM Goal WHERE balance >= target";
        int count = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                count++;
            }
        }

        // Assert
        assertThat(count).isEqualTo(2); // Completed and Exceeded
    }

    @Test
    @DisplayName("Should calculate remaining amount for all goals")
    void shouldCalculateRemainingAmountForAllGoals() throws SQLException {
        // Arrange
        insertGoalWithProgress("GOL_001", "Goal 1", 5000.00, 2000.00);
        insertGoalWithProgress("GOL_002", "Goal 2", 3000.00, 1000.00);

        // Act
        String selectSql = "SELECT SUM(target - balance) as totalRemaining FROM Goal WHERE balance < target";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {

            // Assert
            assertThat(rs.next()).isTrue();
            double totalRemaining = rs.getDouble("totalRemaining");
            assertThat(totalRemaining).isEqualTo(5000.00); // (5000-2000) + (3000-1000)
        }
    }

    // Helper methods
    private void insertGoal(String id, String name, double priority, String deadline) throws SQLException {
        String sql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                    "VALUES (?, ?, 10000.00, 0.00, ?, ?, '2026-01-01')";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, deadline);
            pstmt.setDouble(4, priority);
            pstmt.executeUpdate();
        }
    }

    private void insertGoalWithProgress(String id, String name, double target, double balance) throws SQLException {
        String sql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                    "VALUES (?, ?, ?, ?, '2026-12-31', 1.0, '2026-01-01')";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setDouble(3, target);
            pstmt.setDouble(4, balance);
            pstmt.executeUpdate();
        }
    }

    private void insertGoalObject(Goal goal) throws SQLException {
        String sql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTarget());
            pstmt.setDouble(4, goal.getBalance());
            pstmt.setString(5, goal.getDeadline());
            pstmt.setDouble(6, goal.getPriority());
            pstmt.setString(7, goal.getCreateTime());
            pstmt.executeUpdate();
        }
    }

    private void updateGoalBalance(String id, double newBalance) throws SQLException {
        String sql = "UPDATE Goal SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }

    private Goal getGoalById(String id) throws SQLException {
        String sql = "SELECT * FROM Goal WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Goal goal = new Goal();
                    goal.setId(rs.getString("id"));
                    goal.setName(rs.getString("name"));
                    goal.setTarget(rs.getDouble("target"));
                    goal.setBalance(rs.getDouble("balance"));
                    goal.setDeadline(rs.getString("deadline"));
                    goal.setPriority(rs.getDouble("priority"));
                    goal.setCreateTime(rs.getString("createAt"));
                    return goal;
                }
            }
        }
        return null;
    }
}
