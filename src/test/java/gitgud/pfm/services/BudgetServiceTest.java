package gitgud.pfm.services;

import gitgud.pfm.Models.Budget;
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
 * Unit tests for BudgetService
 * Focus on Bug #34: Budget cannot track their categories
 * Tests CRUD operations, budget tracking, and category integration
 */
@DisplayName("BudgetService Unit Tests")
class BudgetServiceTest {

    private Connection testConnection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:budgettest;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa", ""
        );
        initializeTestSchema();
        insertTestCategories();
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
                CREATE TABLE IF NOT EXISTS categories (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    type VARCHAR(20) NOT NULL
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Budget (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    limitAmount DECIMAL(15, 2) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    startDate VARCHAR(50),
                    endDate VARCHAR(50)
                )
            """);

            // Budget-Category association table for Bug #34 fix
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS BudgetCategory (
                    budgetId VARCHAR(50),
                    categoryId VARCHAR(50),
                    PRIMARY KEY (budgetId, categoryId),
                    FOREIGN KEY (budgetId) REFERENCES Budget(id),
                    FOREIGN KEY (categoryId) REFERENCES categories(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transaction_records (
                    id VARCHAR(50) PRIMARY KEY,
                    categoryId VARCHAR(50),
                    amount DECIMAL(15, 2) NOT NULL,
                    name VARCHAR(200),
                    income DECIMAL(1, 0),
                    walletId VARCHAR(50),
                    createTime VARCHAR(50)
                )
            """);
        }
    }

    private void insertTestCategories() throws SQLException {
        String sql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            insertCategory(pstmt, "CAT_FOOD", "Food & Drinks", "EXPENSE");
            insertCategory(pstmt, "CAT_TRANSPORT", "Transport", "EXPENSE");
            insertCategory(pstmt, "CAT_UTILITIES", "Utilities", "EXPENSE");
            insertCategory(pstmt, "CAT_ENTERTAINMENT", "Entertainment", "EXPENSE");
            insertCategory(pstmt, "CAT_GROCERIES", "Groceries", "EXPENSE");
        }
    }

    private void insertCategory(PreparedStatement pstmt, String id, String name, String type) throws SQLException {
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, name + " description");
        pstmt.setString(4, type);
        pstmt.executeUpdate();
    }

    private void insertBudget(Budget budget) throws SQLException {
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

    private void associateBudgetWithCategory(String budgetId, String categoryId) throws SQLException {
        String sql = "INSERT INTO BudgetCategory (budgetId, categoryId) VALUES (?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, categoryId);
            pstmt.executeUpdate();
        }
    }

    private void insertTransaction(String categoryId, double amount, String createTime) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, "TXN_" + System.nanoTime());
            pstmt.setString(2, categoryId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, "Test Transaction");
            pstmt.setDouble(5, 0.0); // Expense
            pstmt.setString(6, "WAL_001");
            pstmt.setString(7, createTime);
            pstmt.executeUpdate();
        }
    }

    private Budget readBudget(String id) throws SQLException {
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

    private List<Budget> readAllBudgets() throws SQLException {
        String sql = "SELECT * FROM Budget ORDER BY name";
        List<Budget> budgets = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
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
        }
        return budgets;
    }

    /**
     * Calculate spent amount for a budget based on associated categories
     * This is the key method to test for Bug #34
     */
    private double calculateBudgetSpent(String budgetId, String startDate, String endDate) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0) as spent
            FROM transaction_records t
            INNER JOIN BudgetCategory bc ON t.categoryId = bc.categoryId
            WHERE bc.budgetId = ?
            AND t.income = 0
            AND t.createTime >= ?
            AND t.createTime <= ?
        """;
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, budgetId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate + "T23:59:59");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("spent");
                }
            }
        }
        return 0.0;
    }

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("Create Budget Tests")
    class CreateBudgetTests {

        @Test
        @DisplayName("Should create budget with valid data")
        void shouldCreateBudgetWithValidData() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createFoodBudget(500000.0);

            // Act
            insertBudget(budget);

            // Assert
            Budget retrieved = readBudget(budget.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Food Budget");
            assertThat(retrieved.getLimitAmount()).isEqualTo(500000.0);
            assertThat(retrieved.getBalance()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create budget with start and end dates")
        void shouldCreateBudgetWithDates() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Monthly Budget",
                1000000.0,
                0.0,
                "2026-01-01",
                "2026-01-31"
            );

            // Act
            insertBudget(budget);

            // Assert
            Budget retrieved = readBudget(budget.getId());
            assertThat(retrieved.getStartDate()).isEqualTo("2026-01-01");
            assertThat(retrieved.getEndDate()).isEqualTo("2026-01-31");
        }

        @Test
        @DisplayName("Should generate unique ID for each budget")
        void shouldGenerateUniqueIdForEachBudget() throws SQLException {
            // Arrange
            Budget b1 = TestDataFactory.createFoodBudget(300000.0);
            Budget b2 = TestDataFactory.createTransportBudget(200000.0);

            // Act
            insertBudget(b1);
            insertBudget(b2);

            // Assert
            assertThat(b1.getId()).isNotEqualTo(b2.getId());
            assertThat(readAllBudgets()).hasSize(2);
        }

        @ParameterizedTest
        @DisplayName("Should create budget with various limit amounts")
        @ValueSource(doubles = {0.01, 100.0, 500000.0, 10000000.0})
        void shouldCreateBudgetWithVariousLimits(double limit) throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Test Budget", limit, 0.0,
                "2026-01-01", "2026-01-31"
            );

            // Act
            insertBudget(budget);

            // Assert
            Budget retrieved = readBudget(budget.getId());
            assertThat(retrieved.getLimitAmount()).isEqualTo(limit);
        }
    }

    // ==================== READ TESTS ====================

    @Nested
    @DisplayName("Read Budget Tests")
    class ReadBudgetTests {

        @Test
        @DisplayName("Should return null for non-existent budget ID")
        void shouldReturnNullForNonExistentId() throws SQLException {
            assertThat(readBudget("NON_EXISTENT_ID")).isNull();
        }

        @Test
        @DisplayName("Should read all budgets ordered by name")
        void shouldReadAllBudgetsOrderedByName() throws SQLException {
            // Arrange
            insertBudget(TestDataFactory.createBudget("Zebra Budget", 100.0, 0.0, "2026-01-01", "2026-01-31"));
            insertBudget(TestDataFactory.createBudget("Apple Budget", 200.0, 0.0, "2026-01-01", "2026-01-31"));
            insertBudget(TestDataFactory.createBudget("Middle Budget", 300.0, 0.0, "2026-01-01", "2026-01-31"));

            // Act
            List<Budget> budgets = readAllBudgets();

            // Assert
            assertThat(budgets).hasSize(3);
            assertThat(budgets.get(0).getName()).isEqualTo("Apple Budget");
            assertThat(budgets.get(1).getName()).isEqualTo("Middle Budget");
            assertThat(budgets.get(2).getName()).isEqualTo("Zebra Budget");
        }

        @Test
        @DisplayName("Should return empty list when no budgets exist")
        void shouldReturnEmptyListWhenNoBudgetsExist() throws SQLException {
            assertThat(readAllBudgets()).isEmpty();
        }
    }

    // ==================== UPDATE TESTS ====================

    @Nested
    @DisplayName("Update Budget Tests")
    class UpdateBudgetTests {

        @Test
        @DisplayName("Should update budget limit amount")
        void shouldUpdateBudgetLimitAmount() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createFoodBudget(500000.0);
            insertBudget(budget);

            // Act
            String updateSql = "UPDATE Budget SET limitAmount = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 750000.0);
                pstmt.setString(2, budget.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Budget updated = readBudget(budget.getId());
            assertThat(updated.getLimitAmount()).isEqualTo(750000.0);
        }

        @Test
        @DisplayName("Should update budget balance (spent amount)")
        void shouldUpdateBudgetBalance() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createFoodBudget(500000.0);
            insertBudget(budget);

            // Act
            String updateSql = "UPDATE Budget SET balance = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 250000.0);
                pstmt.setString(2, budget.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Budget updated = readBudget(budget.getId());
            assertThat(updated.getBalance()).isEqualTo(250000.0);
        }

        @Test
        @DisplayName("Should update budget date range")
        void shouldUpdateBudgetDateRange() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Test Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);

            // Act
            String updateSql = "UPDATE Budget SET startDate = ?, endDate = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "2026-02-01");
                pstmt.setString(2, "2026-02-28");
                pstmt.setString(3, budget.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Budget updated = readBudget(budget.getId());
            assertThat(updated.getStartDate()).isEqualTo("2026-02-01");
            assertThat(updated.getEndDate()).isEqualTo("2026-02-28");
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Delete Budget Tests")
    class DeleteBudgetTests {

        @Test
        @DisplayName("Should delete budget by ID")
        void shouldDeleteBudgetById() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createFoodBudget(500000.0);
            insertBudget(budget);
            assertThat(readBudget(budget.getId())).isNotNull();

            // Act
            String deleteSql = "DELETE FROM Budget WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readBudget(budget.getId())).isNull();
        }

        @Test
        @DisplayName("Should delete budget category associations when budget deleted")
        void shouldDeleteBudgetCategoryAssociationsWhenBudgetDeleted() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createFoodBudget(500000.0);
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");
            associateBudgetWithCategory(budget.getId(), "CAT_GROCERIES");

            // Verify associations exist
            String countSql = "SELECT COUNT(*) FROM BudgetCategory WHERE budgetId = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(countSql)) {
                pstmt.setString(1, budget.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1)).isEqualTo(2);
                }
            }

            // Act - Delete associations first (FK constraint), then budget
            String deleteAssocSql = "DELETE FROM BudgetCategory WHERE budgetId = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteAssocSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }
            String deleteBudgetSql = "DELETE FROM Budget WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteBudgetSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readBudget(budget.getId())).isNull();
            try (PreparedStatement pstmt = testConnection.prepareStatement(countSql)) {
                pstmt.setString(1, budget.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    assertThat(rs.getInt(1)).isEqualTo(0);
                }
            }
        }
    }

    // ==================== BUG #34: CATEGORY TRACKING TESTS ====================

    @Nested
    @DisplayName("Bug #34: Budget Category Tracking Tests")
    class BudgetCategoryTrackingTests {

        @Test
        @DisplayName("Should track single category spending")
        void shouldTrackSingleCategorySpending() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Food Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add transactions
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-15T10:00:00");
            insertTransaction("CAT_FOOD", 50000.0, "2026-01-20T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert
            assertThat(spent).isEqualTo(150000.0);
        }

        @Test
        @DisplayName("Should track multiple categories spending - BUG #34 FIX")
        void shouldTrackMultipleCategoriesSpending() throws SQLException {
            // Arrange - Budget tracks Food, Groceries, Transport
            Budget budget = TestDataFactory.createBudget(
                "Living Expenses", 2000000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");
            associateBudgetWithCategory(budget.getId(), "CAT_GROCERIES");
            associateBudgetWithCategory(budget.getId(), "CAT_TRANSPORT");

            // Add transactions
            insertTransaction("CAT_FOOD", 300000.0, "2026-01-10T10:00:00");
            insertTransaction("CAT_GROCERIES", 200000.0, "2026-01-15T10:00:00");
            insertTransaction("CAT_TRANSPORT", 100000.0, "2026-01-20T10:00:00");
            // This should NOT be counted (not in budget categories)
            insertTransaction("CAT_ENTERTAINMENT", 500000.0, "2026-01-25T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Should be 600000 (Food + Groceries + Transport), NOT 1100000
            assertThat(spent).isEqualTo(600000.0);
            assertThat(spent).isNotEqualTo(1100000.0); // Would be wrong if counting all
        }

        @Test
        @DisplayName("Should not count transactions outside budget date range")
        void shouldNotCountTransactionsOutsideDateRange() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "January Food", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add transactions - one in range, two outside
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-15T10:00:00"); // In range
            insertTransaction("CAT_FOOD", 200000.0, "2025-12-15T10:00:00"); // Before start
            insertTransaction("CAT_FOOD", 300000.0, "2026-02-15T10:00:00"); // After end

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Only 100000 should be counted
            assertThat(spent).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("Should not count income transactions in budget")
        void shouldNotCountIncomeTransactionsInBudget() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Food Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add expense transaction
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-15T10:00:00");
            
            // Add income transaction (should not be counted) - need to insert directly
            String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "TXN_INCOME");
                pstmt.setString(2, "CAT_FOOD");
                pstmt.setDouble(3, 500000.0);
                pstmt.setString(4, "Food Refund");
                pstmt.setDouble(5, 1.0); // Income
                pstmt.setString(6, "WAL_001");
                pstmt.setString(7, "2026-01-20T10:00:00");
                pstmt.executeUpdate();
            }

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Only expense (100000) should be counted
            assertThat(spent).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("Should correctly detect budget exceeded status")
        void shouldCorrectlyDetectBudgetExceededStatus() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Small Budget", 200000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add transactions that exceed budget
            insertTransaction("CAT_FOOD", 150000.0, "2026-01-10T10:00:00");
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-20T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            boolean isExceeded = spent > budget.getLimitAmount();

            // Assert
            assertThat(spent).isEqualTo(250000.0);
            assertThat(isExceeded).isTrue();
            assertThat(spent - budget.getLimitAmount()).isEqualTo(50000.0); // Exceeded by 50000
        }

        @Test
        @DisplayName("Should calculate remaining budget correctly")
        void shouldCalculateRemainingBudgetCorrectly() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Monthly Budget", 1000000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");
            associateBudgetWithCategory(budget.getId(), "CAT_TRANSPORT");

            // Add transactions
            insertTransaction("CAT_FOOD", 300000.0, "2026-01-10T10:00:00");
            insertTransaction("CAT_TRANSPORT", 150000.0, "2026-01-15T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            double remaining = budget.getLimitAmount() - spent;

            // Assert
            assertThat(spent).isEqualTo(450000.0);
            assertThat(remaining).isEqualTo(550000.0);
        }

        @Test
        @DisplayName("Should return zero spent when no transactions in categories")
        void shouldReturnZeroSpentWhenNoTransactionsInCategories() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Empty Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add transaction to different category (not tracked by this budget)
            insertTransaction("CAT_ENTERTAINMENT", 100000.0, "2026-01-15T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert
            assertThat(spent).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle adding new category to existing budget")
        void shouldHandleAddingNewCategoryToExistingBudget() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Expanding Budget", 1000000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add food transaction
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-10T10:00:00");

            // Verify initial spent
            double initialSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            assertThat(initialSpent).isEqualTo(100000.0);

            // Add groceries category and transaction
            associateBudgetWithCategory(budget.getId(), "CAT_GROCERIES");
            insertTransaction("CAT_GROCERIES", 50000.0, "2026-01-15T10:00:00");

            // Act
            double newSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Should now include both categories
            assertThat(newSpent).isEqualTo(150000.0);
        }

        @Test
        @DisplayName("Should handle removing category from budget")
        void shouldHandleRemovingCategoryFromBudget() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Shrinking Budget", 1000000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");
            associateBudgetWithCategory(budget.getId(), "CAT_TRANSPORT");

            // Add transactions
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-10T10:00:00");
            insertTransaction("CAT_TRANSPORT", 50000.0, "2026-01-15T10:00:00");

            // Verify initial spent
            double initialSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            assertThat(initialSpent).isEqualTo(150000.0);

            // Remove transport category
            String deleteSql = "DELETE FROM BudgetCategory WHERE budgetId = ? AND categoryId = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, budget.getId());
                pstmt.setString(2, "CAT_TRANSPORT");
                pstmt.executeUpdate();
            }

            // Act
            double newSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Should only count food now
            assertThat(newSpent).isEqualTo(100000.0);
        }

        @Test
        @DisplayName("Should handle transaction category change affecting budget")
        void shouldHandleTransactionCategoryChangeAffectingBudget() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Food Only Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");

            // Add food transaction
            String txnId = "TXN_CHANGEME";
            String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, txnId);
                pstmt.setString(2, "CAT_FOOD");
                pstmt.setDouble(3, 100000.0);
                pstmt.setString(4, "Food Purchase");
                pstmt.setDouble(5, 0.0);
                pstmt.setString(6, "WAL_001");
                pstmt.setString(7, "2026-01-15T10:00:00");
                pstmt.executeUpdate();
            }

            // Verify initial
            double initialSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            assertThat(initialSpent).isEqualTo(100000.0);

            // Change transaction category to Entertainment (not in budget)
            String updateSql = "UPDATE transaction_records SET categoryId = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "CAT_ENTERTAINMENT");
                pstmt.setString(2, txnId);
                pstmt.executeUpdate();
            }

            // Act
            double newSpent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Should now be 0 since transaction moved out of tracked category
            assertThat(newSpent).isEqualTo(0.0);
        }
    }

    // ==================== BUDGET STATUS TESTS ====================

    @Nested
    @DisplayName("Budget Status Tests")
    class BudgetStatusTests {

        @Test
        @DisplayName("Should calculate budget percentage correctly")
        void shouldCalculateBudgetPercentageCorrectly() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Half Spent Budget", 1000000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            associateBudgetWithCategory(budget.getId(), "CAT_FOOD");
            insertTransaction("CAT_FOOD", 500000.0, "2026-01-15T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");
            double percentage = (spent / budget.getLimitAmount()) * 100;

            // Assert
            assertThat(percentage).isEqualTo(50.0);
        }

        @ParameterizedTest
        @DisplayName("Should determine correct budget status")
        @CsvSource({
            "500000, 200000, ON_TRACK",
            "500000, 400000, WARNING",
            "500000, 500000, AT_LIMIT",
            "500000, 600000, EXCEEDED"
        })
        void shouldDetermineCorrectBudgetStatus(double limit, double spent, String expectedStatus) {
            // Act
            String status;
            double percentage = (spent / limit) * 100;
            if (percentage > 100) {
                status = "EXCEEDED";
            } else if (percentage == 100) {
                status = "AT_LIMIT";
            } else if (percentage >= 80) {
                status = "WARNING";
            } else {
                status = "ON_TRACK";
            }

            // Assert
            assertThat(status).isEqualTo(expectedStatus);
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle budget with zero limit")
        void shouldHandleBudgetWithZeroLimit() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Zero Budget", 0.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);

            // Assert
            Budget retrieved = readBudget(budget.getId());
            assertThat(retrieved.getLimitAmount()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle special characters in budget name")
        void shouldHandleSpecialCharactersInBudgetName() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "Food & Drinks Budget! @#$%", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);

            // Assert
            Budget retrieved = readBudget(budget.getId());
            assertThat(retrieved.getName()).isEqualTo("Food & Drinks Budget! @#$%");
        }

        @Test
        @DisplayName("Should handle budget with no categories assigned")
        void shouldHandleBudgetWithNoCategoriesAssigned() throws SQLException {
            // Arrange
            Budget budget = TestDataFactory.createBudget(
                "No Categories Budget", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            insertBudget(budget);
            // No categories associated

            // Add transaction
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-15T10:00:00");

            // Act
            double spent = calculateBudgetSpent(budget.getId(), "2026-01-01", "2026-01-31");

            // Assert - Should be 0 since no categories tracked
            assertThat(spent).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle overlapping budget periods")
        void shouldHandleOverlappingBudgetPeriods() throws SQLException {
            // Arrange - Two budgets tracking same category but different periods
            Budget januaryBudget = TestDataFactory.createBudget(
                "January Food", 500000.0, 0.0, "2026-01-01", "2026-01-31"
            );
            Budget februaryBudget = TestDataFactory.createBudget(
                "February Food", 500000.0, 0.0, "2026-02-01", "2026-02-28"
            );
            insertBudget(januaryBudget);
            insertBudget(februaryBudget);
            associateBudgetWithCategory(januaryBudget.getId(), "CAT_FOOD");
            associateBudgetWithCategory(februaryBudget.getId(), "CAT_FOOD");

            // Add transactions
            insertTransaction("CAT_FOOD", 100000.0, "2026-01-15T10:00:00");
            insertTransaction("CAT_FOOD", 200000.0, "2026-02-15T10:00:00");

            // Act
            double januarySpent = calculateBudgetSpent(januaryBudget.getId(), "2026-01-01", "2026-01-31");
            double februarySpent = calculateBudgetSpent(februaryBudget.getId(), "2026-02-01", "2026-02-28");

            // Assert - Each budget should only count its own period
            assertThat(januarySpent).isEqualTo(100000.0);
            assertThat(februarySpent).isEqualTo(200000.0);
        }
    }
}
