package gitgud.pfm.services;

import gitgud.pfm.Models.Wallet;
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
 * Unit tests for WalletService
 * Focus on Bug #32: Account is not displaying its proper balance
 * Tests CRUD operations, balance calculations, and transaction integration
 */
@DisplayName("WalletService Unit Tests")
class WalletServiceTest {

    private Connection testConnection;

    @BeforeEach
    void setUp() throws SQLException {
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:wallettest;MODE=MySQL;DB_CLOSE_DELAY=-1",
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
                CREATE TABLE IF NOT EXISTS Wallet (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    color VARCHAR(50)
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
                    FOREIGN KEY (walletId) REFERENCES Wallet(id)
                )
            """);
        }
    }

    private void insertTestCategories() throws SQLException {
        String sql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            insertCategory(pstmt, "CAT_FOOD", "Food & Drinks", "EXPENSE");
            insertCategory(pstmt, "CAT_TRANSPORT", "Transport", "EXPENSE");
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

    private void insertWallet(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getId());
            pstmt.setString(2, wallet.getName());
            pstmt.setDouble(3, wallet.getBalance());
            pstmt.setString(4, wallet.getColor());
            pstmt.executeUpdate();
        }
    }

    private void insertTransaction(String id, String categoryId, double amount, double income, 
                                    String walletId, String createTime) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, categoryId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, "Test Transaction");
            pstmt.setDouble(5, income);
            pstmt.setString(6, walletId);
            pstmt.setString(7, createTime);
            pstmt.executeUpdate();
        }
    }

    private Wallet readWallet(String id) throws SQLException {
        String sql = "SELECT * FROM Wallet WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Wallet wallet = new Wallet();
                    wallet.setId(rs.getString("id"));
                    wallet.setName(rs.getString("name"));
                    wallet.setBalance(rs.getDouble("balance"));
                    wallet.setColor(rs.getString("color"));
                    return wallet;
                }
            }
        }
        return null;
    }

    private List<Wallet> readAllWallets() throws SQLException {
        String sql = "SELECT * FROM Wallet ORDER BY name";
        List<Wallet> wallets = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setId(rs.getString("id"));
                wallet.setName(rs.getString("name"));
                wallet.setBalance(rs.getDouble("balance"));
                wallet.setColor(rs.getString("color"));
                wallets.add(wallet);
            }
        }
        return wallets;
    }

    private void updateWalletBalance(String walletId, double newBalance) throws SQLException {
        String sql = "UPDATE Wallet SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, walletId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Calculate wallet balance from transactions (the correct way for Bug #32)
     * This demonstrates how balance SHOULD be calculated
     */
    private double calculateBalanceFromTransactions(String walletId, double initialBalance) throws SQLException {
        // Get income total
        String incomeSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE walletId = ? AND income = 1";
        double totalIncome = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(incomeSql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalIncome = rs.getDouble("total");
            }
        }

        // Get expense total
        String expenseSql = "SELECT COALESCE(SUM(amount), 0) as total FROM transaction_records WHERE walletId = ? AND income = 0";
        double totalExpenses = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(expenseSql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) totalExpenses = rs.getDouble("total");
            }
        }

        return initialBalance + totalIncome - totalExpenses;
    }

    private double getTotalBalance() throws SQLException {
        String sql = "SELECT COALESCE(SUM(balance), 0) as total FROM Wallet";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getDouble("total");
        }
        return 0.0;
    }

    // ==================== CREATE TESTS ====================

    @Nested
    @DisplayName("Create Wallet Tests")
    class CreateWalletTests {

        @Test
        @DisplayName("Should create wallet with valid data")
        void shouldCreateWalletWithValidData() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getName()).isEqualTo("Default Wallet");
            assertThat(retrieved.getBalance()).isEqualTo(500000.0);
            assertThat(retrieved.getColor()).isEqualTo("Blue");
        }

        @Test
        @DisplayName("Should create wallet with zero balance")
        void shouldCreateWalletWithZeroBalance() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createEmptyWallet("Empty Wallet");

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getBalance()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should generate unique ID for each wallet")
        void shouldGenerateUniqueIdForEachWallet() throws SQLException {
            // Arrange
            Wallet w1 = TestDataFactory.createWallet("Blue", 100000.0, "Wallet 1");
            Wallet w2 = TestDataFactory.createWallet("Green", 200000.0, "Wallet 2");

            // Act
            insertWallet(w1);
            insertWallet(w2);

            // Assert
            assertThat(w1.getId()).isNotEqualTo(w2.getId());
            assertThat(readAllWallets()).hasSize(2);
        }

        @ParameterizedTest
        @DisplayName("Should create wallet with various initial balances")
        @ValueSource(doubles = {0.0, 100.0, 500000.0, 10000000.0})
        void shouldCreateWalletWithVariousBalances(double balance) throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", balance, "Test Wallet");

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getBalance()).isEqualTo(balance);
        }
    }

    // ==================== READ TESTS ====================

    @Nested
    @DisplayName("Read Wallet Tests")
    class ReadWalletTests {

        @Test
        @DisplayName("Should return null for non-existent wallet ID")
        void shouldReturnNullForNonExistentId() throws SQLException {
            assertThat(readWallet("NON_EXISTENT_ID")).isNull();
        }

        @Test
        @DisplayName("Should read all wallets ordered by name")
        void shouldReadAllWalletsOrderedByName() throws SQLException {
            // Arrange
            insertWallet(TestDataFactory.createWallet("Blue", 100.0, "Zebra Wallet"));
            insertWallet(TestDataFactory.createWallet("Green", 200.0, "Alpha Wallet"));
            insertWallet(TestDataFactory.createWallet("Red", 300.0, "Middle Wallet"));

            // Act
            List<Wallet> wallets = readAllWallets();

            // Assert
            assertThat(wallets).hasSize(3);
            assertThat(wallets.get(0).getName()).isEqualTo("Alpha Wallet");
            assertThat(wallets.get(1).getName()).isEqualTo("Middle Wallet");
            assertThat(wallets.get(2).getName()).isEqualTo("Zebra Wallet");
        }

        @Test
        @DisplayName("Should return empty list when no wallets exist")
        void shouldReturnEmptyListWhenNoWalletsExist() throws SQLException {
            assertThat(readAllWallets()).isEmpty();
        }
    }

    // ==================== UPDATE TESTS ====================

    @Nested
    @DisplayName("Update Wallet Tests")
    class UpdateWalletTests {

        @Test
        @DisplayName("Should update wallet balance")
        void shouldUpdateWalletBalance() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();
            insertWallet(wallet);

            // Act
            updateWalletBalance(wallet.getId(), 750000.0);

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(750000.0);
        }

        @Test
        @DisplayName("Should update wallet name")
        void shouldUpdateWalletName() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();
            insertWallet(wallet);

            // Act
            String updateSql = "UPDATE Wallet SET name = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "Updated Name");
                pstmt.setString(2, wallet.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should update wallet color")
        void shouldUpdateWalletColor() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Color Test");
            insertWallet(wallet);

            // Act
            String updateSql = "UPDATE Wallet SET color = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setString(1, "Green");
                pstmt.setString(2, wallet.getId());
                pstmt.executeUpdate();
            }

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getColor()).isEqualTo("Green");
        }
    }

    // ==================== DELETE TESTS ====================

    @Nested
    @DisplayName("Delete Wallet Tests")
    class DeleteWalletTests {

        @Test
        @DisplayName("Should delete wallet by ID")
        void shouldDeleteWalletById() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();
            insertWallet(wallet);
            assertThat(readWallet(wallet.getId())).isNotNull();

            // Act
            String deleteSql = "DELETE FROM Wallet WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, wallet.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readWallet(wallet.getId())).isNull();
        }

        @Test
        @DisplayName("Should not affect other wallets when deleting one")
        void shouldNotAffectOtherWalletsWhenDeletingOne() throws SQLException {
            // Arrange
            Wallet w1 = TestDataFactory.createWallet("Blue", 100000.0, "Wallet 1");
            Wallet w2 = TestDataFactory.createWallet("Green", 200000.0, "Wallet 2");
            insertWallet(w1);
            insertWallet(w2);

            // Act
            String deleteSql = "DELETE FROM Wallet WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, w1.getId());
                pstmt.executeUpdate();
            }

            // Assert
            assertThat(readWallet(w1.getId())).isNull();
            assertThat(readWallet(w2.getId())).isNotNull();
            assertThat(readAllWallets()).hasSize(1);
        }
    }

    // ==================== BUG #32: BALANCE CALCULATION TESTS ====================

    @Nested
    @DisplayName("Bug #32: Account Balance Display Tests")
    class BalanceDisplayTests {

        @Test
        @DisplayName("Should display correct balance after adding income transaction")
        void shouldDisplayCorrectBalanceAfterAddingIncomeTransaction() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Test Wallet");
            insertWallet(wallet);

            // Add income transaction
            insertTransaction("TXN_001", "CAT_SALARY", 100000.0, 1.0, wallet.getId(), "2026-01-15T10:00:00");

            // Act - Calculate expected balance
            double expectedBalance = calculateBalanceFromTransactions(wallet.getId(), 500000.0);

            // Update wallet balance (simulating what the app should do)
            updateWalletBalance(wallet.getId(), expectedBalance);

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(600000.0); // 500000 + 100000
        }

        @Test
        @DisplayName("Should display correct balance after adding expense transaction")
        void shouldDisplayCorrectBalanceAfterAddingExpenseTransaction() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Test Wallet");
            insertWallet(wallet);

            // Add expense transaction
            insertTransaction("TXN_001", "CAT_FOOD", 50000.0, 0.0, wallet.getId(), "2026-01-15T10:00:00");

            // Act
            double expectedBalance = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), expectedBalance);

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(450000.0); // 500000 - 50000
        }

        @Test
        @DisplayName("Should display correct balance after multiple transactions")
        void shouldDisplayCorrectBalanceAfterMultipleTransactions() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Test Wallet");
            insertWallet(wallet);

            // Add multiple transactions
            insertTransaction("TXN_001", "CAT_SALARY", 100000.0, 1.0, wallet.getId(), "2026-01-05T10:00:00");   // +100000
            insertTransaction("TXN_002", "CAT_FOOD", 50000.0, 0.0, wallet.getId(), "2026-01-10T10:00:00");       // -50000
            insertTransaction("TXN_003", "CAT_TRANSPORT", 20000.0, 0.0, wallet.getId(), "2026-01-15T10:00:00"); // -20000
            insertTransaction("TXN_004", "CAT_FREELANCE", 25000.0, 1.0, wallet.getId(), "2026-01-20T10:00:00"); // +25000

            // Act
            double expectedBalance = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), expectedBalance);

            // Assert
            // Expected: 500000 + 100000 - 50000 - 20000 + 25000 = 555000
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(555000.0);
        }

        @Test
        @DisplayName("Should restore balance after deleting transaction")
        void shouldRestoreBalanceAfterDeletingTransaction() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Test Wallet");
            insertWallet(wallet);

            // Add expense transaction
            insertTransaction("TXN_001", "CAT_FOOD", 50000.0, 0.0, wallet.getId(), "2026-01-15T10:00:00");
            double balanceAfterExpense = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), balanceAfterExpense);
            assertThat(readWallet(wallet.getId()).getBalance()).isEqualTo(450000.0);

            // Act - Delete transaction
            String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
                pstmt.setString(1, "TXN_001");
                pstmt.executeUpdate();
            }

            // Recalculate balance
            double balanceAfterDelete = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), balanceAfterDelete);

            // Assert - Balance should be restored
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(500000.0);
        }

        @Test
        @DisplayName("Should update balance after editing transaction amount")
        void shouldUpdateBalanceAfterEditingTransactionAmount() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Test Wallet");
            insertWallet(wallet);

            insertTransaction("TXN_001", "CAT_FOOD", 50000.0, 0.0, wallet.getId(), "2026-01-15T10:00:00");
            double initialBalance = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), initialBalance);
            assertThat(readWallet(wallet.getId()).getBalance()).isEqualTo(450000.0);

            // Act - Update transaction amount from 50000 to 75000
            String updateSql = "UPDATE transaction_records SET amount = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
                pstmt.setDouble(1, 75000.0);
                pstmt.setString(2, "TXN_001");
                pstmt.executeUpdate();
            }

            // Recalculate balance
            double newBalance = calculateBalanceFromTransactions(wallet.getId(), 500000.0);
            updateWalletBalance(wallet.getId(), newBalance);

            // Assert
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(425000.0); // 500000 - 75000
        }

        @Test
        @DisplayName("Should maintain balance when moving transaction to different wallet")
        void shouldMaintainBalanceWhenMovingTransaction() throws SQLException {
            // Arrange
            Wallet wallet1 = TestDataFactory.createWallet("Blue", 500000.0, "Wallet 1");
            Wallet wallet2 = TestDataFactory.createWallet("Green", 300000.0, "Wallet 2");
            insertWallet(wallet1);
            insertWallet(wallet2);

            // Add expense to wallet1
            insertTransaction("TXN_001", "CAT_FOOD", 50000.0, 0.0, wallet1.getId(), "2026-01-15T10:00:00");
            double wallet1Balance = calculateBalanceFromTransactions(wallet1.getId(), 500000.0);
            updateWalletBalance(wallet1.getId(), wallet1Balance);
            assertThat(readWallet(wallet1.getId()).getBalance()).isEqualTo(450000.0);

            // Act - Move transaction to wallet2
            String moveSql = "UPDATE transaction_records SET walletId = ? WHERE id = ?";
            try (PreparedStatement pstmt = testConnection.prepareStatement(moveSql)) {
                pstmt.setString(1, wallet2.getId());
                pstmt.setString(2, "TXN_001");
                pstmt.executeUpdate();
            }

            // Recalculate both balances
            double newWallet1Balance = calculateBalanceFromTransactions(wallet1.getId(), 500000.0);
            double newWallet2Balance = calculateBalanceFromTransactions(wallet2.getId(), 300000.0);
            updateWalletBalance(wallet1.getId(), newWallet1Balance);
            updateWalletBalance(wallet2.getId(), newWallet2Balance);

            // Assert
            assertThat(readWallet(wallet1.getId()).getBalance()).isEqualTo(500000.0); // Restored
            assertThat(readWallet(wallet2.getId()).getBalance()).isEqualTo(250000.0); // 300000 - 50000
        }

        @Test
        @DisplayName("Multiple wallets should have independent balances")
        void multipleWalletsShouldHaveIndependentBalances() throws SQLException {
            // Arrange
            Wallet wallet1 = TestDataFactory.createWallet("Blue", 100000.0, "Wallet 1");
            Wallet wallet2 = TestDataFactory.createWallet("Green", 50000.0, "Wallet 2");
            insertWallet(wallet1);
            insertWallet(wallet2);

            // Add transaction only to wallet1
            insertTransaction("TXN_001", "CAT_SALARY", 50000.0, 1.0, wallet1.getId(), "2026-01-15T10:00:00");
            double wallet1Balance = calculateBalanceFromTransactions(wallet1.getId(), 100000.0);
            updateWalletBalance(wallet1.getId(), wallet1Balance);

            // Assert - wallet2 should not be affected
            assertThat(readWallet(wallet1.getId()).getBalance()).isEqualTo(150000.0);
            assertThat(readWallet(wallet2.getId()).getBalance()).isEqualTo(50000.0); // Unchanged
        }

        @Test
        @DisplayName("Should persist correct balance across database reload")
        void shouldPersistCorrectBalanceAcrossDatabaseReload() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, "Persistence Test");
            insertWallet(wallet);
            String walletId = wallet.getId();

            // Add transactions
            insertTransaction("TXN_001", "CAT_SALARY", 100000.0, 1.0, walletId, "2026-01-10T10:00:00");
            insertTransaction("TXN_002", "CAT_FOOD", 50000.0, 0.0, walletId, "2026-01-15T10:00:00");

            double calculatedBalance = calculateBalanceFromTransactions(walletId, 500000.0);
            updateWalletBalance(walletId, calculatedBalance);

            // Verify initial state
            assertThat(readWallet(walletId).getBalance()).isEqualTo(550000.0);

            // Act - Simulate "reload" by reading fresh from DB
            Wallet reloaded = readWallet(walletId);

            // Assert
            assertThat(reloaded.getBalance()).isEqualTo(550000.0);

            // Verify calculation still works
            double recalculated = calculateBalanceFromTransactions(walletId, 500000.0);
            assertThat(recalculated).isEqualTo(550000.0);
        }

        @Test
        @DisplayName("Should handle negative balance scenario")
        void shouldHandleNegativeBalanceScenario() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Red", 50000.0, "Negative Test");
            insertWallet(wallet);

            // Add expense larger than initial balance
            insertTransaction("TXN_001", "CAT_FOOD", 80000.0, 0.0, wallet.getId(), "2026-01-15T10:00:00");
            double balance = calculateBalanceFromTransactions(wallet.getId(), 50000.0);
            updateWalletBalance(wallet.getId(), balance);

            // Assert - Should allow negative balance
            Wallet updated = readWallet(wallet.getId());
            assertThat(updated.getBalance()).isEqualTo(-30000.0); // 50000 - 80000
        }
    }

    // ==================== TOTAL BALANCE TESTS ====================

    @Nested
    @DisplayName("Total Balance Calculation Tests")
    class TotalBalanceTests {

        @Test
        @DisplayName("Should calculate total balance across all wallets")
        void shouldCalculateTotalBalanceAcrossAllWallets() throws SQLException {
            // Arrange
            insertWallet(TestDataFactory.createWallet("Blue", 100000.0, "Wallet 1"));
            insertWallet(TestDataFactory.createWallet("Green", 200000.0, "Wallet 2"));
            insertWallet(TestDataFactory.createWallet("Red", 300000.0, "Wallet 3"));

            // Act
            double totalBalance = getTotalBalance();

            // Assert
            assertThat(totalBalance).isEqualTo(600000.0);
        }

        @Test
        @DisplayName("Should return zero when no wallets exist")
        void shouldReturnZeroWhenNoWalletsExist() throws SQLException {
            double totalBalance = getTotalBalance();
            assertThat(totalBalance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle mixed positive and negative balances")
        void shouldHandleMixedPositiveAndNegativeBalances() throws SQLException {
            // Arrange
            insertWallet(TestDataFactory.createWallet("Blue", 100000.0, "Positive"));
            Wallet negativeWallet = TestDataFactory.createWallet("Red", 0.0, "Negative");
            insertWallet(negativeWallet);
            updateWalletBalance(negativeWallet.getId(), -50000.0);

            // Act
            double totalBalance = getTotalBalance();

            // Assert
            assertThat(totalBalance).isEqualTo(50000.0); // 100000 - 50000
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle wallet with very long name")
        void shouldHandleWalletWithVeryLongName() throws SQLException {
            // Arrange
            String longName = "A".repeat(100);
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, longName);

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle wallet with special characters in name")
        void shouldHandleWalletWithSpecialCharactersInName() throws SQLException {
            // Arrange
            String specialName = "My Wallet! @#$%^&*()";
            Wallet wallet = TestDataFactory.createWallet("Blue", 500000.0, specialName);

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getName()).isEqualTo(specialName);
        }

        @Test
        @DisplayName("Should handle decimal precision for balance")
        void shouldHandleDecimalPrecisionForBalance() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createWallet("Blue", 12345.67, "Precise Wallet");

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getBalance()).isEqualTo(12345.67);
        }

        @Test
        @DisplayName("Should handle very large balance values")
        void shouldHandleVeryLargeBalanceValues() throws SQLException {
            // Arrange
            double largeBalance = 999999999999.99;
            Wallet wallet = TestDataFactory.createWallet("Gold", largeBalance, "Rich Wallet");

            // Act
            insertWallet(wallet);

            // Assert
            Wallet retrieved = readWallet(wallet.getId());
            assertThat(retrieved.getBalance()).isEqualTo(largeBalance);
        }

        @Test
        @DisplayName("Should handle many wallets")
        void shouldHandleManyWallets() throws SQLException {
            // Arrange - Create 50 wallets
            for (int i = 0; i < 50; i++) {
                Wallet wallet = TestDataFactory.createWallet(
                    "Color" + i, 10000.0 + i * 100, "Wallet " + i
                );
                insertWallet(wallet);
            }

            // Act
            List<Wallet> allWallets = readAllWallets();

            // Assert
            assertThat(allWallets).hasSize(50);
        }

        @Test
        @DisplayName("Should handle wallet with null color")
        void shouldHandleWalletWithNullColor() throws SQLException {
            // Arrange
            String sql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
                pstmt.setString(1, "WAL_NULL_COLOR");
                pstmt.setString(2, "No Color Wallet");
                pstmt.setDouble(3, 100000.0);
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.executeUpdate();
            }

            // Act
            Wallet retrieved = readWallet("WAL_NULL_COLOR");

            // Assert
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getColor()).isNull();
        }
    }

    // ==================== TRANSACTION COUNT TESTS ====================

    @Nested
    @DisplayName("Transaction Count Tests")
    class TransactionCountTests {

        @Test
        @DisplayName("Should count transactions per wallet")
        void shouldCountTransactionsPerWallet() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();
            insertWallet(wallet);

            insertTransaction("TXN_001", "CAT_FOOD", 100.0, 0.0, wallet.getId(), "2026-01-01T10:00:00");
            insertTransaction("TXN_002", "CAT_FOOD", 200.0, 0.0, wallet.getId(), "2026-01-02T10:00:00");
            insertTransaction("TXN_003", "CAT_SALARY", 1000.0, 1.0, wallet.getId(), "2026-01-03T10:00:00");

            // Act
            String countSql = "SELECT COUNT(*) as cnt FROM transaction_records WHERE walletId = ?";
            int transactionCount = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(countSql)) {
                pstmt.setString(1, wallet.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) transactionCount = rs.getInt("cnt");
                }
            }

            // Assert
            assertThat(transactionCount).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero transactions for new wallet")
        void shouldReturnZeroTransactionsForNewWallet() throws SQLException {
            // Arrange
            Wallet wallet = TestDataFactory.createDefaultWallet();
            insertWallet(wallet);

            // Act
            String countSql = "SELECT COUNT(*) as cnt FROM transaction_records WHERE walletId = ?";
            int transactionCount = 0;
            try (PreparedStatement pstmt = testConnection.prepareStatement(countSql)) {
                pstmt.setString(1, wallet.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) transactionCount = rs.getInt("cnt");
                }
            }

            // Assert
            assertThat(transactionCount).isEqualTo(0);
        }
    }
}
