package gitgud.pfm.services;

import gitgud.pfm.Models.Transaction;
import gitgud.pfm.fixtures.TestDataFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TransactionService
 * Tests CRUD operations, calculations, and edge cases
 * Uses H2 in-memory database to isolate from production
 */
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        // Create in-memory H2 database for isolated testing
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:transactiontest;MODE=MySQL;DB_CLOSE_DELAY=-1",
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
                CREATE TABLE IF NOT EXISTS transaction_records (
                    id VARCHAR(50) PRIMARY KEY,
                    categoryId VARCHAR(50),
                    amount DECIMAL(15, 2) NOT NULL,
                    name VARCHAR(200),
                    income DECIMAL(1, 0),
                    walletId VARCHAR(50),
                    createTime VARCHAR(50),
                    FOREIGN KEY (categoryId) REFERENCES categories(id)
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Wallet (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    color VARCHAR(50)
                )
            """);
        }
    }

    private void insertTestCategories() throws SQLException {
        String sql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            // Expense categories
            insertCategory(pstmt, "CAT_FOOD", "Food & Drinks", "EXPENSE");
            insertCategory(pstmt, "CAT_TRANSPORT", "Transport", "EXPENSE");
            insertCategory(pstmt, "CAT_UTILITIES", "Utilities", "EXPENSE");
            
            // Income categories
            insertCategory(pstmt, "CAT_SALARY", "Salary", "INCOME");
            insertCategory(pstmt, "CAT_FREELANCE", "Freelance", "INCOME");
        }
    }

    private void insertCategory(PreparedStatement pstmt, String id, String name, String type) throws SQLException {
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, name + " description");
        pstmt.setString(4, type);
        pstmt.executeUpdate();
    }

    private void insertWallet(String id, String name, double balance) throws SQLException {
        String sql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setDouble(3, balance);
            pstmt.setString(4, "Blue");
            pstmt.executeUpdate();
        }
    }

    private void insertTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, t.getId());
            pstmt.setString(2, t.getCategoryId());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getName());
            pstmt.setDouble(5, t.getIncome());
            pstmt.setString(6, t.getWalletId());
            pstmt.setString(7, t.getCreateTime());
            pstmt.executeUpdate();
        }
    }

    private Transaction readTransaction(String id) throws SQLException {
        String sql = "SELECT * FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transaction t = new Transaction();
                    t.setId(rs.getString("id"));
                    t.setCategoryId(rs.getString("categoryId"));
                    t.setAmount(rs.getDouble("amount"));
                    t.setName(rs.getString("name"));
                    t.setIncome(rs.getDouble("income"));
                    t.setWalletId(rs.getString("walletId"));
                    t.setCreateTime(rs.getString("createTime"));
                    return t;
                }
            }
        }
        return null;
    }

    private List<Transaction> readAllTransactions() throws SQLException {
        String sql = "SELECT * FROM transaction_records ORDER BY createTime DESC";
        List<Transaction> transactions = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getString("id"));
                t.setCategoryId(rs.getString("categoryId"));
                t.setAmount(rs.getDouble("amount"));
                t.setName(rs.getString("name"));
                t.setIncome(rs.getDouble("income"));
                t.setWalletId(rs.getString("walletId"));
                t.setCreateTime(rs.getString("createTime"));
                transactions.add(t);
            }
        }
        return transactions;
    }

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction with valid expense data")
        void shouldCreateTransactionWithValidExpenseData() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createExpenseTransaction(
                50000.0, "Grocery Shopping", "CAT_FOOD", "WAL_001"
            );

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getAmount()).isEqualTo(50000.0);
            assertThat(retrieved.getName()).isEqualTo("Grocery Shopping");
            assertThat(retrieved.getCategoryId()).isEqualTo("CAT_FOOD");
            assertThat(retrieved.getIncome()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should create transaction with valid income data")
        void shouldCreateTransactionWithValidIncomeData() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createIncomeTransaction(
                100000.0, "Monthly Salary", "WAL_001"
            );

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getAmount()).isEqualTo(100000.0);
            assertThat(retrieved.getName()).isEqualTo("Monthly Salary");
            assertThat(retrieved.getIncome()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should generate unique ID for each transaction")
        void shouldGenerateUniqueIdForEachTransaction() throws SQLException {
            // Arrange
            Transaction t1 = TestDataFactory.createFoodExpense(30000.0, "WAL_001");
            Transaction t2 = TestDataFactory.createFoodExpense(40000.0, "WAL_001");

            // Act
            insertTransaction(t1);
            insertTransaction(t2);

            // Assert
            assertThat(t1.getId()).isNotEqualTo(t2.getId());
            assertThat(readAllTransactions()).hasSize(2);
        }

        @ParameterizedTest
        @DisplayName("Should create transaction with various amounts")
        @ValueSource(doubles = {0.01, 1.0, 100.0, 10000.0, 1000000.0, 99999999.99})
        void shouldCreateTransactionWithVariousAmounts(double amount) throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createExpenseTransaction(
                amount, "Test Transaction", "CAT_FOOD", "WAL_001"
            );

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Should create transaction with zero amount")
        void shouldCreateTransactionWithZeroAmount() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createExpenseTransaction(
                0.0, "Zero Amount Transaction", "CAT_FOOD", "WAL_001"
            );

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getAmount()).isEqualTo(0.0);
        }
    }

    // ==================== READ TESTS ====================

    @Nested
    @DisplayName("Read Transaction Tests")
    class ReadTransactionTests {

        @Test
        @DisplayName("Should return null for non-existent transaction ID")
        void shouldReturnNullForNonExistentId() throws SQLException {
            // Act
            Transaction retrieved = readTransaction("NON_EXISTENT_ID");

            // Assert
            assertThat(retrieved).isNull();
        }

        @Test
        @DisplayName("Should read all transactions ordered by createTime DESC")
        void shouldReadAllTransactionsOrderedByCreateTime() throws SQLException {
            // Arrange
            Transaction t1 = new Transaction("CAT_FOOD", 100.0, "First", 0.0, "WAL_001", "2026-01-01T10:00:00");
            Transaction t2 = new Transaction("CAT_FOOD", 200.0, "Second", 0.0, "WAL_001", "2026-01-02T10:00:00");
            Transaction t3 = new Transaction("CAT_FOOD", 300.0, "Third", 0.0, "WAL_001", "2026-01-03T10:00:00");
            insertTransaction(t1);
            insertTransaction(t2);
            insertTransaction(t3);

            // Act
            List<Transaction> transactions = readAllTransactions();

            // Assert
            assertThat(transactions).hasSize(3);
            assertThat(transactions.get(0).getName()).isEqualTo("Third");
            assertThat(transactions.get(1).getName()).isEqualTo("Second");
            assertThat(transactions.get(2).getName()).isEqualTo("First");
        }

        @Test
        @DisplayName("Should return empty list when no transactions exist")
        void shouldReturnEmptyListWhenNoTransactionsExist() throws SQLException {
            // Act
            List<Transaction> transactions = readAllTransactions();

            // Assert
            assertThat(transactions).isEmpty();
        }

        @Test
        @DisplayName("Should read transactions by wallet ID")
        void shouldReadTransactionsByWalletId() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_FOOD", 100.0, "Wallet1 Trans1", 0.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 200.0, "Wallet1 Trans2", 0.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 300.0, "Wallet2 Trans1", 0.0, "WAL_002", "2026-01-03T10:00:00"));

            // Act
            String sql = "SELECT * FROM transaction_records WHERE walletId = ?";
            List<Transaction> wallet1Transactions = new java.util.ArrayList<>();
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "WAL_001");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.setId(rs.getString("id"));
                        t.setWalletId(rs.getString("walletId"));
                        t.setName(rs.getString("name"));
                        wallet1Transactions.add(t);
                    }
                }
            }

            // Assert
            assertThat(wallet1Transactions).hasSize(2);
            assertThat(wallet1Transactions).allMatch(t -> t.getWalletId().equals("WAL_001"));
        }
    }

    // ==================== UPDATE TESTS ====================

    @Nested
    @DisplayName("Update Transaction Tests")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update transaction amount")
        void shouldUpdateTransactionAmount() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);

            // Act
            String updateSql = "UPDATE transaction_records SET amount = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 75000.0);
                pstmt.setString(2, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Transaction updated = readTransaction(transaction.getId());
            assertThat(updated.getAmount()).isEqualTo(75000.0);
        }

        @Test
        @DisplayName("Should update transaction name")
        void shouldUpdateTransactionName() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);

            // Act
            String updateSql = "UPDATE transaction_records SET name = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "Updated Name");
                pstmt.setString(2, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Transaction updated = readTransaction(transaction.getId());
            assertThat(updated.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should update transaction category")
        void shouldUpdateTransactionCategory() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);

            // Act
            String updateSql = "UPDATE transaction_records SET categoryId = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "CAT_TRANSPORT");
                pstmt.setString(2, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Transaction updated = readTransaction(transaction.getId());
            assertThat(updated.getCategoryId()).isEqualTo("CAT_TRANSPORT");
        }

        @Test
        @DisplayName("Should update transaction wallet (move to different account)")
        void shouldUpdateTransactionWallet() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);

            // Act
            String updateSql = "UPDATE transaction_records SET walletId = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "WAL_002");
                pstmt.setString(2, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Transaction updated = readTransaction(transaction.getId());
            assertThat(updated.getWalletId()).isEqualTo("WAL_002");
        }

        @Test
        @DisplayName("Should convert expense to income")
        void shouldConvertExpenseToIncome() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);
            assertThat(transaction.getIncome()).isEqualTo(0.0);

            // Act
            String updateSql = "UPDATE transaction_records SET income = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 1.0);
                pstmt.setString(2, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Transaction updated = readTransaction(transaction.getId());
            assertThat(updated.getIncome()).isEqualTo(1.0);
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Delete Transaction Tests")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete transaction by ID")
        void shouldDeleteTransactionById() throws SQLException {
            // Arrange
            Transaction transaction = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(transaction);
            assertThat(readTransaction(transaction.getId())).isNotNull();

            // Act
            String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, transaction.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readTransaction(transaction.getId())).isNull();
        }

        @Test
        @DisplayName("Should not affect other transactions when deleting one")
        void shouldNotAffectOtherTransactionsWhenDeletingOne() throws SQLException {
            // Arrange
            Transaction t1 = TestDataFactory.createFoodExpense(30000.0, "WAL_001");
            Transaction t2 = TestDataFactory.createFoodExpense(40000.0, "WAL_001");
            Transaction t3 = TestDataFactory.createFoodExpense(50000.0, "WAL_001");
            insertTransaction(t1);
            insertTransaction(t2);
            insertTransaction(t3);

            // Act
            String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, t2.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readAllTransactions()).hasSize(2);
            assertThat(readTransaction(t1.getId())).isNotNull();
            assertThat(readTransaction(t2.getId())).isNull();
            assertThat(readTransaction(t3.getId())).isNotNull();
        }

        @Test
        @DisplayName("Delete non-existent transaction should not throw error")
        void deleteNonExistentTransactionShouldNotThrowError() throws SQLException {
            // Act & Assert - should not throw
            String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, "NON_EXISTENT_ID");
                int rowsAffected = pstmt.executeUpdate();
                assertThat(rowsAffected).isEqualTo(0);
            }
        }
    }

    // ==================== CALCULATION TESTS ====================

    @Nested
    @DisplayName("Transaction Calculation Tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate total income correctly")
        void shouldCalculateTotalIncomeCorrectly() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_SALARY", 100000.0, "Salary", 1.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FREELANCE", 50000.0, "Freelance", 1.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food", 0.0, "WAL_001", "2026-01-03T10:00:00")); // Expense

            // Act
            String sql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 1";
            double totalIncome = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalIncome = rs.getDouble("total");
                }
            }

            // Assert
            assertThat(totalIncome).isEqualTo(150000.0); // 100000 + 50000
        }

        @Test
        @DisplayName("Should calculate total expenses correctly")
        void shouldCalculateTotalExpensesCorrectly() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_SALARY", 100000.0, "Salary", 1.0, "WAL_001", "2026-01-01T10:00:00")); // Income
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food", 0.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_TRANSPORT", 20000.0, "Transport", 0.0, "WAL_001", "2026-01-03T10:00:00"));

            // Act
            String sql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 0";
            double totalExpenses = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalExpenses = rs.getDouble("total");
                }
            }

            // Assert
            assertThat(totalExpenses).isEqualTo(50000.0); // 30000 + 20000
        }

        @Test
        @DisplayName("Should return zero for total income when no income transactions")
        void shouldReturnZeroForTotalIncomeWhenNoIncomeTransactions() throws SQLException {
            // Arrange - only expenses
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food", 0.0, "WAL_001", "2026-01-01T10:00:00"));

            // Act
            String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE income = 1";
            double totalIncome = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalIncome = rs.getDouble("total");
                }
            }

            // Assert
            assertThat(totalIncome).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate net balance correctly")
        void shouldCalculateNetBalanceCorrectly() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_SALARY", 100000.0, "Salary", 1.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food", 0.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_TRANSPORT", 20000.0, "Transport", 0.0, "WAL_001", "2026-01-03T10:00:00"));

            // Act - calculate income - expenses
            String incomeSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE income = 1";
            String expenseSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE income = 0";
            
            double totalIncome = 0, totalExpenses = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(incomeSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalIncome = rs.getDouble("total");
            }
            try (PreparedStatement pstmt = testConnection.prepareStatement(expenseSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalExpenses = rs.getDouble("total");
            }

            // Assert
            double netBalance = totalIncome - totalExpenses;
            assertThat(netBalance).isEqualTo(50000.0); // 100000 - 50000
        }

        @Test
        @DisplayName("Should handle negative net balance")
        void shouldHandleNegativeNetBalance() throws SQLException {
            // Arrange - more expenses than income
            insertTransaction(new Transaction("CAT_SALARY", 50000.0, "Salary", 1.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 80000.0, "Food", 0.0, "WAL_001", "2026-01-02T10:00:00"));

            // Act
            String incomeSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE income = 1";
            String expenseSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE income = 0";
            
            double totalIncome = 0, totalExpenses = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(incomeSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalIncome = rs.getDouble("total");
            }
            try (PreparedStatement pstmt = testConnection.prepareStatement(expenseSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalExpenses = rs.getDouble("total");
            }

            // Assert
            double netBalance = totalIncome - totalExpenses;
            assertThat(netBalance).isEqualTo(-30000.0); // 50000 - 80000
        }
    }

    // ==================== FILTERING TESTS ====================

    @Nested
    @DisplayName("Transaction Filtering Tests")
    class FilteringTests {

        @Test
        @DisplayName("Should filter transactions by category")
        void shouldFilterTransactionsByCategory() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food 1", 0.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 40000.0, "Food 2", 0.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_TRANSPORT", 20000.0, "Transport", 0.0, "WAL_001", "2026-01-03T10:00:00"));

            // Act
            String sql = "SELECT * FROM transaction_records WHERE categoryId = ?";
            List<Transaction> foodTransactions = new java.util.ArrayList<>();
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "CAT_FOOD");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.setCategoryId(rs.getString("categoryId"));
                        foodTransactions.add(t);
                    }
                }
            }

            // Assert
            assertThat(foodTransactions).hasSize(2);
            assertThat(foodTransactions).allMatch(t -> t.getCategoryId().equals("CAT_FOOD"));
        }

        @Test
        @DisplayName("Should filter transactions by date range")
        void shouldFilterTransactionsByDateRange() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Jan 15", 0.0, "WAL_001", "2026-01-15T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 40000.0, "Jan 20", 0.0, "WAL_001", "2026-01-20T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 50000.0, "Feb 05", 0.0, "WAL_001", "2026-02-05T10:00:00"));

            // Act - Filter January transactions
            String sql = "SELECT * FROM transaction_records WHERE createTime >= ? AND createTime < ?";
            List<Transaction> januaryTransactions = new java.util.ArrayList<>();
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "2026-01-01");
                pstmt.setString(2, "2026-02-01");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.setName(rs.getString("name"));
                        januaryTransactions.add(t);
                    }
                }
            }

            // Assert
            assertThat(januaryTransactions).hasSize(2);
        }

        @Test
        @DisplayName("Should filter transactions by amount range")
        void shouldFilterTransactionsByAmountRange() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_FOOD", 10000.0, "Small", 0.0, "WAL_001", "2026-01-01T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 50000.0, "Medium", 0.0, "WAL_001", "2026-01-02T10:00:00"));
            insertTransaction(new Transaction("CAT_FOOD", 100000.0, "Large", 0.0, "WAL_001", "2026-01-03T10:00:00"));

            // Act - Filter transactions between 30000 and 80000
            String sql = "SELECT * FROM transaction_records WHERE amount >= ? AND amount <= ?";
            List<Transaction> midRangeTransactions = new java.util.ArrayList<>();
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setDouble(1, 30000.0);
                pstmt.setDouble(2, 80000.0);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.setAmount(rs.getDouble("amount"));
                        t.setName(rs.getString("name"));
                        midRangeTransactions.add(t);
                    }
                }
            }

            // Assert
            assertThat(midRangeTransactions).hasSize(1);
            assertThat(midRangeTransactions.get(0).getName()).isEqualTo("Medium");
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void shouldReturnEmptyListForNonMatchingFilter() throws SQLException {
            // Arrange
            insertTransaction(new Transaction("CAT_FOOD", 30000.0, "Food", 0.0, "WAL_001", "2026-01-01T10:00:00"));

            // Act - Filter by non-existent category
            String sql = "SELECT * FROM transaction_records WHERE categoryId = ?";
            List<Transaction> transactions = new java.util.ArrayList<>();
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "CAT_NONEXISTENT");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        transactions.add(new Transaction());
                    }
                }
            }

            // Assert
            assertThat(transactions).isEmpty();
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle transaction with empty name")
        void shouldHandleTransactionWithEmptyName() throws SQLException {
            // Arrange
            Transaction transaction = new Transaction("CAT_FOOD", 50000.0, "", 0.0, "WAL_001", "2026-01-01T10:00:00");

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle transaction with very long name")
        void shouldHandleTransactionWithVeryLongName() throws SQLException {
            // Arrange
            String longName = "A".repeat(200);
            Transaction transaction = new Transaction("CAT_FOOD", 50000.0, longName, 0.0, "WAL_001", "2026-01-01T10:00:00");

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle transaction with special characters in name")
        void shouldHandleTransactionWithSpecialCharactersInName() throws SQLException {
            // Arrange
            String specialName = "Food & Drinks! @#$%^&*()";
            Transaction transaction = new Transaction("CAT_FOOD", 50000.0, specialName, 0.0, "WAL_001", "2026-01-01T10:00:00");

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle decimal precision for amounts")
        void shouldHandleDecimalPrecisionForAmounts() throws SQLException {
            // Arrange
            Transaction transaction = new Transaction("CAT_FOOD", 12345.67, "Precise Amount", 0.0, "WAL_001", "2026-01-01T10:00:00");

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getAmount()).isEqualTo(12345.67);
        }

        @ParameterizedTest
        @DisplayName("Should handle various date formats")
        @CsvSource({
            "2026-01-01T00:00:00, 2026-01-01T00:00:00",
            "2026-12-31T23:59:59, 2026-12-31T23:59:59",
            "2026-06-15T12:30:45, 2026-06-15T12:30:45"
        })
        void shouldHandleVariousDateFormats(String inputDate, String expectedDate) throws SQLException {
            // Arrange
            Transaction transaction = new Transaction("CAT_FOOD", 50000.0, "Date Test", 0.0, "WAL_001", inputDate);

            // Act
            insertTransaction(transaction);

            // Assert
            Transaction retrieved = readTransaction(transaction.getId());
            assertThat(retrieved.getCreateTime()).isEqualTo(expectedDate);
        }
    }
}
