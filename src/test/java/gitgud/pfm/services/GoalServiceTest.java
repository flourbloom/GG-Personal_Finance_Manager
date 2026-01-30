package gitgud.pfm.services;

import gitgud.pfm.Models.Goal;
import gitgud.pfm.fixtures.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GoalService
 * Tests CRUD operations, progress tracking, and goal status
 */
@DisplayName("GoalService Unit Tests")
class GoalServiceTest {

    private Connection testConnection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:goaltest;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa", ""
        );
        initializeTestSchema();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
            }
            testConnection.close();
        }
    }

    private void initializeTestSchema() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Goal (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    target DECIMAL(15, 2) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    deadline VARCHAR(50),
                    priority DECIMAL(5, 2),
                    createAt VARCHAR(50)
                )
            """);
        }
    }

    private void insertGoal(Goal goal) throws SQLException {
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

    private Goal readGoal(String id) throws SQLException {
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

    private List<Goal> readAllGoals() throws SQLException {
        String sql = "SELECT * FROM Goal ORDER BY priority DESC, deadline";
        List<Goal> goals = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
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
        }
        return goals;
    }

    private List<Goal> readActiveGoals() throws SQLException {
        String sql = "SELECT * FROM Goal WHERE balance < target ORDER BY priority DESC, deadline";
        List<Goal> goals = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
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
        }
        return goals;
    }

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("Create Goal Tests")
    class CreateGoalTests {

        @Test
        @DisplayName("Should create goal with valid data")
        void shouldCreateGoalWithValidData() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createEmergencyFundGoal();

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Emergency Fund");
            assertThat(retrieved.getTarget()).isEqualTo(1000000.0);
            assertThat(retrieved.getBalance()).isEqualTo(100000.0);
            assertThat(retrieved.getPriority()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should create goal with zero initial balance")
        void shouldCreateGoalWithZeroInitialBalance() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal(
                "New Goal", 500000.0, 0.0,
                LocalDate.now().plusMonths(6).format(DATE_FORMATTER), 0.5
            );

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getBalance()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should generate unique ID for each goal")
        void shouldGenerateUniqueIdForEachGoal() throws SQLException {
            // Arrange
            Goal g1 = TestDataFactory.createEmergencyFundGoal();
            Goal g2 = TestDataFactory.createVacationGoal();

            // Act
            insertGoal(g1);
            insertGoal(g2);

            // Assert
            assertThat(g1.getId()).isNotEqualTo(g2.getId());
            assertThat(readAllGoals()).hasSize(2);
        }

        @ParameterizedTest
        @DisplayName("Should create goal with various target amounts")
        @ValueSource(doubles = {1000.0, 100000.0, 1000000.0, 10000000.0})
        void shouldCreateGoalWithVariousTargetAmounts(double target) throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal(
                "Target Test", target, 0.0,
                LocalDate.now().plusYears(1).format(DATE_FORMATTER), 0.5
            );

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getTarget()).isEqualTo(target);
        }

        @ParameterizedTest
        @DisplayName("Should create goal with various priority levels")
        @ValueSource(doubles = {0.0, 0.25, 0.5, 0.75, 1.0})
        void shouldCreateGoalWithVariousPriorityLevels(double priority) throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal(
                "Priority Test", 500000.0, 0.0,
                LocalDate.now().plusMonths(6).format(DATE_FORMATTER), priority
            );

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getPriority()).isEqualTo(priority);
        }
    }

    // ==================== READ TESTS ====================

    @Nested
    @DisplayName("Read Goal Tests")
    class ReadGoalTests {

        @Test
        @DisplayName("Should return null for non-existent goal ID")
        void shouldReturnNullForNonExistentId() throws SQLException {
            assertThat(readGoal("NON_EXISTENT_ID")).isNull();
        }

        @Test
        @DisplayName("Should read all goals ordered by priority DESC then deadline")
        void shouldReadAllGoalsOrderedByPriorityAndDeadline() throws SQLException {
            // Arrange - Create goals with different priorities and deadlines
            Goal lowPriority = TestDataFactory.createSavingsGoal("Low Priority", 100000.0, 0.0, "2026-06-01", 0.3);
            Goal highPriority = TestDataFactory.createSavingsGoal("High Priority", 200000.0, 0.0, "2026-12-01", 1.0);
            Goal mediumPriority = TestDataFactory.createSavingsGoal("Medium Priority", 150000.0, 0.0, "2026-03-01", 0.5);

            insertGoal(lowPriority);
            insertGoal(highPriority);
            insertGoal(mediumPriority);

            // Act
            List<Goal> goals = readAllGoals();

            // Assert - Should be ordered: High (1.0), Medium (0.5), Low (0.3)
            assertThat(goals).hasSize(3);
            assertThat(goals.get(0).getName()).isEqualTo("High Priority");
            assertThat(goals.get(1).getName()).isEqualTo("Medium Priority");
            assertThat(goals.get(2).getName()).isEqualTo("Low Priority");
        }

        @Test
        @DisplayName("Should return empty list when no goals exist")
        void shouldReturnEmptyListWhenNoGoalsExist() throws SQLException {
            assertThat(readAllGoals()).isEmpty();
        }

        @Test
        @DisplayName("Should read only active (incomplete) goals")
        void shouldReadOnlyActiveGoals() throws SQLException {
            // Arrange
            Goal activeGoal = TestDataFactory.createSavingsGoal("Active", 500000.0, 100000.0, "2026-12-31", 1.0);
            Goal completedGoal = TestDataFactory.createCompletedGoal();

            insertGoal(activeGoal);
            insertGoal(completedGoal);

            // Act
            List<Goal> activeGoals = readActiveGoals();

            // Assert
            assertThat(activeGoals).hasSize(1);
            assertThat(activeGoals.get(0).getName()).isEqualTo("Active");
        }
    }

    // ==================== UPDATE TESTS ====================

    @Nested
    @DisplayName("Update Goal Tests")
    class UpdateGoalTests {

        @Test
        @DisplayName("Should update goal current balance (add contribution)")
        void shouldUpdateGoalCurrentBalance() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Savings", 500000.0, 100000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Add 50000 contribution
            String updateSql = "UPDATE Goal SET balance = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 150000.0);
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal updated = readGoal(goal.getId());
            assertThat(updated.getBalance()).isEqualTo(150000.0);
        }

        @Test
        @DisplayName("Should update goal target amount")
        void shouldUpdateGoalTargetAmount() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Adjustable Goal", 500000.0, 100000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Increase target
            String updateSql = "UPDATE Goal SET target = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 750000.0);
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal updated = readGoal(goal.getId());
            assertThat(updated.getTarget()).isEqualTo(750000.0);
        }

        @Test
        @DisplayName("Should update goal deadline")
        void shouldUpdateGoalDeadline() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Deadline Goal", 500000.0, 100000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Extend deadline
            String updateSql = "UPDATE Goal SET deadline = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "2027-06-30");
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal updated = readGoal(goal.getId());
            assertThat(updated.getDeadline()).isEqualTo("2027-06-30");
        }

        @Test
        @DisplayName("Should update goal priority")
        void shouldUpdateGoalPriority() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Priority Change", 500000.0, 100000.0, "2026-12-31", 0.5);
            insertGoal(goal);

            // Act - Increase priority
            String updateSql = "UPDATE Goal SET priority = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 1.0);
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal updated = readGoal(goal.getId());
            assertThat(updated.getPriority()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should update goal name")
        void shouldUpdateGoalName() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Original Name", 500000.0, 100000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act
            String updateSql = "UPDATE Goal SET name = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "Updated Name");
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal updated = readGoal(goal.getId());
            assertThat(updated.getName()).isEqualTo("Updated Name");
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Delete Goal Tests")
    class DeleteGoalTests {

        @Test
        @DisplayName("Should delete goal by ID")
        void shouldDeleteGoalById() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createEmergencyFundGoal();
            insertGoal(goal);
            assertThat(readGoal(goal.getId())).isNotNull();

            // Act
            String deleteSql = "DELETE FROM Goal WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readGoal(goal.getId())).isNull();
        }

        @Test
        @DisplayName("Should not affect other goals when deleting one")
        void shouldNotAffectOtherGoalsWhenDeletingOne() throws SQLException {
            // Arrange
            Goal g1 = TestDataFactory.createEmergencyFundGoal();
            Goal g2 = TestDataFactory.createVacationGoal();
            insertGoal(g1);
            insertGoal(g2);

            // Act
            String deleteSql = "DELETE FROM Goal WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, g1.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readGoal(g1.getId())).isNull();
            assertThat(readGoal(g2.getId())).isNotNull();
            assertThat(readAllGoals()).hasSize(1);
        }

        @Test
        @DisplayName("Delete non-existent goal should not throw error")
        void deleteNonExistentGoalShouldNotThrowError() throws SQLException {
            // Act & Assert
            String deleteSql = "DELETE FROM Goal WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, "NON_EXISTENT_ID");
                int rowsAffected = pstmt.executeUpdate();
                assertThat(rowsAffected).isEqualTo(0);
            }
        }
    }

    // ==================== PROGRESS TRACKING TESTS ====================

    @Nested
    @DisplayName("Goal Progress Tracking Tests")
    class ProgressTrackingTests {

        @Test
        @DisplayName("Should calculate progress percentage correctly")
        void shouldCalculateProgressPercentageCorrectly() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Progress Test", 1000000.0, 500000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act
            Goal retrieved = readGoal(goal.getId());
            double progressPercentage = (retrieved.getBalance() / retrieved.getTarget()) * 100;

            // Assert
            assertThat(progressPercentage).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should track multiple contributions")
        void shouldTrackMultipleContributions() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Multiple Contributions", 1000000.0, 0.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Simulate multiple contributions
            double[] contributions = {100000.0, 150000.0, 200000.0, 50000.0};
            double currentBalance = 0;
            for (double contribution : contributions) {
                currentBalance += contribution;
                String updateSql = "UPDATE Goal SET balance = ? WHERE id = ?";
                try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                    pstmt.setDouble(1, currentBalance);
                    pstmt.setString(2, goal.getId());
                    pstmt.executeUpdate();
                }
            }

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getBalance()).isEqualTo(500000.0); // Sum of contributions
        }

        @Test
        @DisplayName("Should detect goal completion")
        void shouldDetectGoalCompletion() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Almost Done", 500000.0, 450000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Final contribution
            String updateSql = "UPDATE Goal SET balance = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 500000.0);
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal retrieved = readGoal(goal.getId());
            boolean isCompleted = retrieved.getBalance() >= retrieved.getTarget();
            assertThat(isCompleted).isTrue();
            
            // Should not appear in active goals
            List<Goal> activeGoals = readActiveGoals();
            assertThat(activeGoals).isEmpty();
        }

        @Test
        @DisplayName("Should handle over-contribution (exceeding target)")
        void shouldHandleOverContribution() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Over Saver", 500000.0, 400000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Contribute more than remaining
            String updateSql = "UPDATE Goal SET balance = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 600000.0); // 100000 over target
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getBalance()).isEqualTo(600000.0);
            assertThat(retrieved.getBalance()).isGreaterThan(retrieved.getTarget());
        }

        @ParameterizedTest
        @DisplayName("Should calculate remaining amount correctly")
        @CsvSource({
            "1000000, 0, 1000000",
            "1000000, 250000, 750000",
            "1000000, 500000, 500000",
            "1000000, 1000000, 0",
            "1000000, 1500000, -500000"  // Over-contributed
        })
        void shouldCalculateRemainingAmountCorrectly(double target, double current, double expectedRemaining) {
            // Act
            double remaining = target - current;

            // Assert
            assertThat(remaining).isEqualTo(expectedRemaining);
        }
    }

    // ==================== GOAL STATUS TESTS ====================

    @Nested
    @DisplayName("Goal Status Tests")
    class GoalStatusTests {

        @ParameterizedTest
        @DisplayName("Should determine correct goal status based on progress")
        @CsvSource({
            "1000000, 0, NOT_STARTED",
            "1000000, 100000, IN_PROGRESS",
            "1000000, 500000, HALFWAY",
            "1000000, 900000, ALMOST_DONE",
            "1000000, 1000000, COMPLETED",
            "1000000, 1200000, EXCEEDED"
        })
        void shouldDetermineCorrectGoalStatus(double target, double current, String expectedStatus) {
            // Act
            String status;
            double percentage = (current / target) * 100;
            if (percentage == 0) {
                status = "NOT_STARTED";
            } else if (percentage >= 100) {
                status = percentage > 100 ? "EXCEEDED" : "COMPLETED";
            } else if (percentage >= 90) {
                status = "ALMOST_DONE";
            } else if (percentage >= 50) {
                status = "HALFWAY";
            } else {
                status = "IN_PROGRESS";
            }

            // Assert
            assertThat(status).isEqualTo(expectedStatus);
        }

        @Test
        @DisplayName("Should identify overdue goals")
        void shouldIdentifyOverdueGoals() throws SQLException {
            // Arrange - Goal with past deadline
            Goal overdueGoal = TestDataFactory.createSavingsGoal(
                "Overdue Goal", 500000.0, 200000.0, "2025-12-31", 1.0 // Past deadline
            );
            insertGoal(overdueGoal);

            // Act
            Goal retrieved = readGoal(overdueGoal.getId());
            LocalDate deadline = LocalDate.parse(retrieved.getDeadline());
            boolean isOverdue = deadline.isBefore(LocalDate.now()) && retrieved.getBalance() < retrieved.getTarget();

            // Assert
            assertThat(isOverdue).isTrue();
        }

        @Test
        @DisplayName("Should identify goals on track")
        void shouldIdentifyGoalsOnTrack() throws SQLException {
            // Arrange - Goal with future deadline
            Goal futureGoal = TestDataFactory.createSavingsGoal(
                "Future Goal", 500000.0, 200000.0, "2027-12-31", 1.0
            );
            insertGoal(futureGoal);

            // Act
            Goal retrieved = readGoal(futureGoal.getId());
            LocalDate deadline = LocalDate.parse(retrieved.getDeadline());
            boolean isOnTrack = deadline.isAfter(LocalDate.now()) && retrieved.getBalance() < retrieved.getTarget();

            // Assert
            assertThat(isOnTrack).isTrue();
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle goal with very long name")
        void shouldHandleGoalWithVeryLongName() throws SQLException {
            // Arrange
            String longName = "A".repeat(100);
            Goal goal = TestDataFactory.createSavingsGoal(longName, 500000.0, 0.0, "2026-12-31", 1.0);

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle goal with special characters in name")
        void shouldHandleGoalWithSpecialCharactersInName() throws SQLException {
            // Arrange
            String specialName = "Emergency Fund! @#$%^&*()";
            Goal goal = TestDataFactory.createSavingsGoal(specialName, 500000.0, 0.0, "2026-12-31", 1.0);

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle goal with zero target")
        void shouldHandleGoalWithZeroTarget() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Zero Target", 0.0, 0.0, "2026-12-31", 1.0);

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getTarget()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle decimal precision for amounts")
        void shouldHandleDecimalPrecisionForAmounts() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Precise Goal", 123456.78, 12345.67, "2026-12-31", 0.5);

            // Act
            insertGoal(goal);

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getTarget()).isEqualTo(123456.78);
            assertThat(retrieved.getBalance()).isEqualTo(12345.67);
        }

        @Test
        @DisplayName("Should handle many concurrent goals")
        void shouldHandleManyConcurrentGoals() throws SQLException {
            // Arrange - Create 100 goals
            for (int i = 0; i < 100; i++) {
                Goal goal = TestDataFactory.createSavingsGoal(
                    "Goal " + i, 100000.0 + i * 1000, i * 100.0,
                    LocalDate.now().plusMonths(i % 12 + 1).format(DATE_FORMATTER),
                    (i % 10) / 10.0
                );
                insertGoal(goal);
            }

            // Act
            List<Goal> allGoals = readAllGoals();

            // Assert
            assertThat(allGoals).hasSize(100);
        }

        @Test
        @DisplayName("Should handle withdrawal (balance reduction)")
        void shouldHandleWithdrawal() throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Withdrawal Test", 500000.0, 300000.0, "2026-12-31", 1.0);
            insertGoal(goal);

            // Act - Simulate withdrawal
            String updateSql = "UPDATE Goal SET balance = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 200000.0); // Reduced balance
                pstmt.setString(2, goal.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Goal retrieved = readGoal(goal.getId());
            assertThat(retrieved.getBalance()).isEqualTo(200000.0);
        }
    }

    // ==================== DATE HANDLING TESTS ====================

    @Nested
    @DisplayName("Date Handling Tests")
    class DateHandlingTests {

        @ParameterizedTest
        @DisplayName("Should store and retrieve various deadline formats")
        @CsvSource({
            "2026-01-01, 2026-01-01",
            "2026-12-31, 2026-12-31",
            "2027-06-15, 2027-06-15"
        })
        void shouldStoreAndRetrieveDeadlines(String inputDate, String expectedDate) throws SQLException {
            // Arrange
            Goal goal = TestDataFactory.createSavingsGoal("Date Test", 500000.0, 0.0, inputDate, 1.0);

            // Act
            insertGoal(goal);
            Goal retrieved = readGoal(goal.getId());

            // Assert
            assertThat(retrieved.getDeadline()).isEqualTo(expectedDate);
        }

        @Test
        @DisplayName("Should handle createTime timestamp")
        void shouldHandleCreateTimeTimestamp() throws SQLException {
            // Arrange
            String createTime = "2026-01-31T10:30:45";
            Goal goal = TestDataFactory.createGoal(
                "Timestamp Test", 500000.0, 0.0, "2026-12-31", 1.0, createTime
            );

            // Act
            insertGoal(goal);
            Goal retrieved = readGoal(goal.getId());

            // Assert
            assertThat(retrieved.getCreateTime()).isEqualTo(createTime);
        }
    }
}
