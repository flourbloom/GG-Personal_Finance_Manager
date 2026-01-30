package gitgud.pfm.integration;

import gitgud.pfm.Models.Wallet;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Wallet/Account management.
 * Tests complete workflows including CRUD operations and balance management.
 * Focus on Bug #32: Account balance display issues.
 */
@DisplayName("Wallet/Account Integration Tests")
class WalletIntegrationTest extends IntegrationTestBase {

    @BeforeEach
    @Override
    protected void setUpDatabase() throws SQLException {
        super.setUpDatabase();
        insertTestCategories();
    }

    private void insertTestCategories() throws SQLException {
        String sql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            insertCategory(pstmt, "CAT_SALARY", "Salary", "INCOME");
            insertCategory(pstmt, "CAT_FOOD", "Food", "EXPENSE");
            insertCategory(pstmt, "CAT_TRANSPORT", "Transport", "EXPENSE");
        }
    }

    private void insertCategory(PreparedStatement pstmt, String id, String name, String type) throws SQLException {
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, name + " description");
        pstmt.setString(4, type);
        pstmt.executeUpdate();
    }

    private String insertWallet(String name, double balance, String color) throws SQLException {
        String id = "WAL_" + System.nanoTime();
        String sql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setDouble(3, balance);
            pstmt.setString(4, color);
            pstmt.executeUpdate();
        }
        return id;
    }

    private void insertTransaction(String walletId, String categoryId, double amount, 
                                    double income, String createTime) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, "TXN_" + System.nanoTime());
            pstmt.setString(2, categoryId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, "Test Transaction");
            pstmt.setDouble(5, income);
            pstmt.setString(6, walletId);
            pstmt.setString(7, createTime);
            pstmt.executeUpdate();
        }
    }

    private double getWalletBalance(String walletId) throws SQLException {
        String sql = "SELECT balance FROM Wallet WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        }
        return 0.0;
    }

    private void updateWalletBalance(String walletId, double balance) throws SQLException {
        String sql = "UPDATE Wallet SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setDouble(1, balance);
            pstmt.setString(2, walletId);
            pstmt.executeUpdate();
        }
    }

    private double calculateBalanceFromTransactions(String walletId, double initialBalance) throws SQLException {
        String incomeSql = "SELECT COALESCE(SUM(amount), 0) FROM transaction_records WHERE walletId = ? AND income = 1";
        String expenseSql = "SELECT COALESCE(SUM(amount), 0) FROM transaction_records WHERE walletId = ? AND income = 0";
        
        double income = 0, expense = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(incomeSql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) income = rs.getDouble(1);
            }
        }
        try (PreparedStatement pstmt = testConnection.prepareStatement(expenseSql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) expense = rs.getDouble(1);
            }
        }
        return initialBalance + income - expense;
    }

    // ==================== WALLET CRUD WORKFLOW TESTS ====================

    @Test
    @DisplayName("Integration: Complete wallet lifecycle - Create, Read, Update, Delete")
    void shouldHandleCompleteWalletLifecycle() throws SQLException {
        // Create
        String walletId = insertWallet("My Wallet", 500000.0, "Blue");
        assertThat(getWalletBalance(walletId)).isEqualTo(500000.0);

        // Read
        String readSql = "SELECT * FROM Wallet WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(readSql)) {
            pstmt.setString(1, walletId);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("name")).isEqualTo("My Wallet");
                assertThat(rs.getString("color")).isEqualTo("Blue");
            }
        }

        // Update
        String updateSql = "UPDATE Wallet SET name = ?, balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
            pstmt.setString(1, "Updated Wallet");
            pstmt.setDouble(2, 750000.0);
            pstmt.setString(3, walletId);
            pstmt.executeUpdate();
        }
        assertThat(getWalletBalance(walletId)).isEqualTo(750000.0);

        // Delete
        String deleteSql = "DELETE FROM Wallet WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
            pstmt.setString(1, walletId);
            pstmt.executeUpdate();
        }
        assertThat(getWalletBalance(walletId)).isEqualTo(0.0); // Returns 0 if not found
    }

    // ==================== BUG #32: BALANCE CALCULATION WORKFLOW ====================

    @Test
    @DisplayName("Integration: Bug #32 - Balance updates correctly after transaction sequence")
    void shouldUpdateBalanceCorrectlyAfterTransactionSequence() throws SQLException {
        // Setup wallet with initial balance
        String walletId = insertWallet("Test Wallet", 500000.0, "Blue");

        // Add transactions
        insertTransaction(walletId, "CAT_SALARY", 100000.0, 1.0, "2026-01-05T10:00:00");  // +100000
        insertTransaction(walletId, "CAT_FOOD", 50000.0, 0.0, "2026-01-10T10:00:00");     // -50000
        insertTransaction(walletId, "CAT_TRANSPORT", 25000.0, 0.0, "2026-01-15T10:00:00"); // -25000

        // Calculate and update balance
        double expectedBalance = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, expectedBalance);

        // Verify
        assertThat(getWalletBalance(walletId)).isEqualTo(525000.0); // 500000 + 100000 - 50000 - 25000
    }

    @Test
    @DisplayName("Integration: Bug #32 - Balance persists correctly after simulated app restart")
    void shouldPersistBalanceCorrectlyAfterRestart() throws SQLException {
        // Setup
        String walletId = insertWallet("Persistence Test", 500000.0, "Green");
        insertTransaction(walletId, "CAT_SALARY", 100000.0, 1.0, "2026-01-10T10:00:00");

        double calculatedBalance = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, calculatedBalance);

        // First check
        assertThat(getWalletBalance(walletId)).isEqualTo(600000.0);

        // Simulate "restart" - query again (in real app, this would be new connection)
        double reloadedBalance = getWalletBalance(walletId);
        double recalculatedBalance = calculateBalanceFromTransactions(walletId, 500000.0);

        // Both should match
        assertThat(reloadedBalance).isEqualTo(recalculatedBalance);
        assertThat(reloadedBalance).isEqualTo(600000.0);
    }

    @Test
    @DisplayName("Integration: Bug #32 - Transaction edit updates balance correctly")
    void shouldUpdateBalanceWhenTransactionEdited() throws SQLException {
        // Setup
        String walletId = insertWallet("Edit Test", 500000.0, "Blue");
        
        // Insert transaction
        String txnId = "TXN_EDIT_" + System.nanoTime();
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, txnId);
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, "Original");
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, walletId);
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        double initialBalance = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, initialBalance);
        assertThat(getWalletBalance(walletId)).isEqualTo(450000.0);

        // Edit transaction amount
        String updateSql = "UPDATE transaction_records SET amount = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
            pstmt.setDouble(1, 75000.0);
            pstmt.setString(2, txnId);
            pstmt.executeUpdate();
        }

        // Recalculate and update
        double newBalance = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, newBalance);

        // Verify - balance should reflect the change
        assertThat(getWalletBalance(walletId)).isEqualTo(425000.0); // 500000 - 75000
    }

    @Test
    @DisplayName("Integration: Bug #32 - Transaction delete restores balance correctly")
    void shouldRestoreBalanceWhenTransactionDeleted() throws SQLException {
        // Setup
        String walletId = insertWallet("Delete Test", 500000.0, "Red");
        
        // Insert transaction
        String txnId = "TXN_DEL_" + System.nanoTime();
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, txnId);
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, "To Delete");
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, walletId);
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        double balanceWithTxn = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, balanceWithTxn);
        assertThat(getWalletBalance(walletId)).isEqualTo(450000.0);

        // Delete transaction
        String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
            pstmt.setString(1, txnId);
            pstmt.executeUpdate();
        }

        // Recalculate and update
        double restoredBalance = calculateBalanceFromTransactions(walletId, 500000.0);
        updateWalletBalance(walletId, restoredBalance);

        // Verify - balance should be restored
        assertThat(getWalletBalance(walletId)).isEqualTo(500000.0);
    }

    // ==================== MULTIPLE WALLET TESTS ====================

    @Test
    @DisplayName("Integration: Multiple wallets maintain independent balances")
    void shouldMaintainIndependentBalancesForMultipleWallets() throws SQLException {
        // Setup multiple wallets
        String wallet1Id = insertWallet("Checking", 1000000.0, "Blue");
        String wallet2Id = insertWallet("Savings", 500000.0, "Green");

        // Add transactions to wallet1 only
        insertTransaction(wallet1Id, "CAT_FOOD", 200000.0, 0.0, "2026-01-10T10:00:00");
        insertTransaction(wallet1Id, "CAT_SALARY", 150000.0, 1.0, "2026-01-15T10:00:00");

        // Add transaction to wallet2
        insertTransaction(wallet2Id, "CAT_SALARY", 50000.0, 1.0, "2026-01-10T10:00:00");

        // Calculate and update balances
        double balance1 = calculateBalanceFromTransactions(wallet1Id, 1000000.0);
        double balance2 = calculateBalanceFromTransactions(wallet2Id, 500000.0);
        updateWalletBalance(wallet1Id, balance1);
        updateWalletBalance(wallet2Id, balance2);

        // Verify - balances should be independent
        assertThat(getWalletBalance(wallet1Id)).isEqualTo(950000.0); // 1000000 - 200000 + 150000
        assertThat(getWalletBalance(wallet2Id)).isEqualTo(550000.0); // 500000 + 50000
    }

    @Test
    @DisplayName("Integration: Moving transaction between wallets updates both balances")
    void shouldUpdateBothBalancesWhenMovingTransaction() throws SQLException {
        // Setup
        String wallet1Id = insertWallet("Source Wallet", 500000.0, "Blue");
        String wallet2Id = insertWallet("Target Wallet", 300000.0, "Green");

        // Add transaction to wallet1
        String txnId = "TXN_MOVE_" + System.nanoTime();
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, txnId);
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, "Moveable");
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, wallet1Id);
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        // Update initial balances
        updateWalletBalance(wallet1Id, calculateBalanceFromTransactions(wallet1Id, 500000.0));
        updateWalletBalance(wallet2Id, calculateBalanceFromTransactions(wallet2Id, 300000.0));
        assertThat(getWalletBalance(wallet1Id)).isEqualTo(450000.0);
        assertThat(getWalletBalance(wallet2Id)).isEqualTo(300000.0);

        // Move transaction to wallet2
        String moveSql = "UPDATE transaction_records SET walletId = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(moveSql)) {
            pstmt.setString(1, wallet2Id);
            pstmt.setString(2, txnId);
            pstmt.executeUpdate();
        }

        // Recalculate both
        updateWalletBalance(wallet1Id, calculateBalanceFromTransactions(wallet1Id, 500000.0));
        updateWalletBalance(wallet2Id, calculateBalanceFromTransactions(wallet2Id, 300000.0));

        // Verify
        assertThat(getWalletBalance(wallet1Id)).isEqualTo(500000.0); // Transaction removed
        assertThat(getWalletBalance(wallet2Id)).isEqualTo(250000.0); // Transaction added (300000 - 50000)
    }

    // ==================== DATA INTEGRITY TESTS ====================

    @Test
    @DisplayName("Integration: Total balance across all wallets is accurate")
    void shouldCalculateAccurateTotalBalance() throws SQLException {
        // Setup multiple wallets with transactions
        String wallet1Id = insertWallet("Wallet 1", 100000.0, "Blue");
        String wallet2Id = insertWallet("Wallet 2", 200000.0, "Green");
        String wallet3Id = insertWallet("Wallet 3", 300000.0, "Red");

        insertTransaction(wallet1Id, "CAT_SALARY", 50000.0, 1.0, "2026-01-10T10:00:00");
        insertTransaction(wallet2Id, "CAT_FOOD", 30000.0, 0.0, "2026-01-15T10:00:00");

        // Update balances
        updateWalletBalance(wallet1Id, calculateBalanceFromTransactions(wallet1Id, 100000.0));
        updateWalletBalance(wallet2Id, calculateBalanceFromTransactions(wallet2Id, 200000.0));
        updateWalletBalance(wallet3Id, calculateBalanceFromTransactions(wallet3Id, 300000.0));

        // Calculate total
        String totalSql = "SELECT SUM(balance) FROM Wallet";
        double totalBalance = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(totalSql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) totalBalance = rs.getDouble(1);
        }

        // Verify
        // Wallet1: 100000 + 50000 = 150000
        // Wallet2: 200000 - 30000 = 170000
        // Wallet3: 300000 (no transactions) = 300000
        // Total: 620000
        assertThat(totalBalance).isEqualTo(620000.0);
    }

    @Test
    @DisplayName("Integration: Wallet count remains consistent after operations")
    void shouldMaintainConsistentWalletCount() throws SQLException {
        // Create 5 wallets
        for (int i = 0; i < 5; i++) {
            insertWallet("Wallet " + i, 100000.0 + i * 10000, "Color" + i);
        }

        String countSql = "SELECT COUNT(*) FROM Wallet";
        int count;
        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
        assertThat(count).isEqualTo(5);

        // Delete one wallet
        String deleteSql = "DELETE FROM Wallet WHERE name = 'Wallet 0'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
            pstmt.executeUpdate();
        }

        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
        assertThat(count).isEqualTo(4);
    }
}
