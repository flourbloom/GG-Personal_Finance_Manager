package gitgud.pfm.integration;

import gitgud.pfm.Models.Budget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Budget Database Integration Tests")
class BudgetIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Should perform complete CRUD operations on Budget")
    void shouldPerformCompleteCrudOperations() throws SQLException {
        // CREATE
        Budget budget = new Budget(
            "Monthly Food",
            500.00,
            150.00,
            "2026-01-01",
            "2026-01-31"
        );

        String insertSql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimitAmount());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStartDate());
            pstmt.setString(6, budget.getEndDate());
            int rowsInserted = pstmt.executeUpdate();
            assertThat(rowsInserted).isEqualTo(1);
        }

        // READ
        String selectSql = "SELECT * FROM Budget WHERE id = ?";
        Budget retrieved;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, budget.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                retrieved = new Budget();
                retrieved.setId(rs.getString("id"));
                retrieved.setName(rs.getString("name"));
                retrieved.setLimitAmount(rs.getDouble("limitAmount"));
                retrieved.setBalance(rs.getDouble("balance"));
                retrieved.setStartDate(rs.getString("startDate"));
                retrieved.setEndDate(rs.getString("endDate"));
            }
        }

        assertThat(retrieved.getId()).isEqualTo(budget.getId());
        assertThat(retrieved.getName()).isEqualTo(budget.getName());
        assertThat(retrieved.getLimitAmount()).isEqualTo(budget.getLimitAmount());

        // UPDATE
        String updateSql = "UPDATE Budget SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
            pstmt.setDouble(1, 250.00);
            pstmt.setString(2, budget.getId());
            int rowsUpdated = pstmt.executeUpdate();
            assertThat(rowsUpdated).isEqualTo(1);
        }

        // Verify update
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, budget.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble("balance")).isEqualTo(250.00);
            }
        }

        // DELETE
        String deleteSql = "DELETE FROM Budget WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
            pstmt.setString(1, budget.getId());
            int rowsDeleted = pstmt.executeUpdate();
            assertThat(rowsDeleted).isEqualTo(1);
        }

        // Verify deletion
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, budget.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Should retrieve all budgets ordered by name")
    void shouldRetrieveAllBudgetsOrderedByName() throws SQLException {
        // Arrange
        insertBudget("BUD_001", "Zebra Budget", 1000.00, 100.00);
        insertBudget("BUD_002", "Alpha Budget", 2000.00, 200.00);
        insertBudget("BUD_003", "Middle Budget", 3000.00, 300.00);

        // Act
        String selectSql = "SELECT * FROM Budget ORDER BY name";
        List<String> budgetNames = new ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                budgetNames.add(rs.getString("name"));
            }
        }

        // Assert
        assertThat(budgetNames).containsExactly("Alpha Budget", "Middle Budget", "Zebra Budget");
    }

    @Test
    @DisplayName("Should track budget spending over time")
    void shouldTrackBudgetSpendingOverTime() throws SQLException {
        // Arrange
        Budget budget = new Budget("Tracking Test", 1000.00, 0.00, "2026-01-01", "2026-01-31");
        insertBudgetObject(budget);

        // Act - Simulate spending
        updateBudgetBalance(budget.getId(), 100.00);
        updateBudgetBalance(budget.getId(), 250.00);
        updateBudgetBalance(budget.getId(), 400.00);

        // Assert
        Budget updated = getBudgetById(budget.getId());
        assertThat(updated.getBalance()).isEqualTo(400.00);
        assertThat(updated.getBalance()).isLessThan(updated.getLimitAmount());
    }

    @Test
    @DisplayName("Should identify budgets that are over limit")
    void shouldIdentifyBudgetsOverLimit() throws SQLException {
        // Arrange
        insertBudget("BUD_001", "Under Limit", 1000.00, 500.00);
        insertBudget("BUD_002", "Over Limit", 500.00, 600.00);
        insertBudget("BUD_003", "At Limit", 1000.00, 1000.00);

        // Act
        String selectSql = "SELECT * FROM Budget WHERE balance >= limitAmount";
        List<String> overLimitBudgets = new ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                overLimitBudgets.add(rs.getString("name"));
            }
        }

        // Assert
        assertThat(overLimitBudgets).containsExactlyInAnyOrder("Over Limit", "At Limit");
    }

    @Test
    @DisplayName("Should filter budgets by date range")
    void shouldFilterBudgetsByDateRange() throws SQLException {
        // Arrange
        insertBudgetWithDates("BUD_001", "January", "2026-01-01", "2026-01-31");
        insertBudgetWithDates("BUD_002", "February", "2026-02-01", "2026-02-28");
        insertBudgetWithDates("BUD_003", "March", "2026-03-01", "2026-03-31");

        // Act
        String selectSql = "SELECT * FROM Budget WHERE startDate >= ? AND startDate < ?";
        int count = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, "2026-01-01");
            pstmt.setString(2, "2026-03-01");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    count++;
                }
            }
        }

        // Assert
        assertThat(count).isEqualTo(2);
    }

    // Helper methods
    private void insertBudget(String id, String name, double limit, double balance) throws SQLException {
        String sql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) " +
                    "VALUES (?, ?, ?, ?, '2026-01-01', '2026-01-31')";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setDouble(3, limit);
            pstmt.setDouble(4, balance);
            pstmt.executeUpdate();
        }
    }

    private void insertBudgetWithDates(String id, String name, String startDate, String endDate) throws SQLException {
        String sql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) " +
                    "VALUES (?, ?, 1000.00, 0.00, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.executeUpdate();
        }
    }

    private void insertBudgetObject(Budget budget) throws SQLException {
        String sql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, budget.getId());
            pstmt.setString(2, budget.getName());
            pstmt.setDouble(3, budget.getLimitAmount());
            pstmt.setDouble(4, budget.getBalance());
            pstmt.setString(5, budget.getStartDate());
            pstmt.setString(6, budget.getEndDate());
            pstmt.executeUpdate();
        }
    }

    private void updateBudgetBalance(String id, double newBalance) throws SQLException {
        String sql = "UPDATE Budget SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }

    private Budget getBudgetById(String id) throws SQLException {
        String sql = "SELECT * FROM Budget WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
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
        }
        return null;
    }
}
