package gitgud.pfm.integration;

import gitgud.pfm.Models.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Database Integration Tests")
class TransactionIntegrationTest extends IntegrationTestBase {

    @BeforeEach
    @Override
    protected void setUpDatabase() throws SQLException {
        super.setUpDatabase();
        // Insert test categories for foreign key constraint
        insertTestCategory("CAT_001", "Groceries", "EXPENSE");
        insertTestCategory("CAT_002", "Salary", "INCOME");
        insertTestCategory("CAT_003", "Utilities", "EXPENSE");
    }

    private void insertTestCategory(String id, String name, String type) throws SQLException {
        String sql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, "Test category");
            pstmt.setString(4, type);
            pstmt.executeUpdate();
        }
    }

    @Test
    @DisplayName("Should insert and retrieve transaction from database")
    void shouldInsertAndRetrieveTransaction() throws SQLException {
        // Arrange
        Transaction transaction = new Transaction(
            "CAT_001",
            150.50,
            "Grocery Shopping",
            0.0,
            "WAL_001",
            "2026-01-31T10:30:00"
        );

        // Act - Insert
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setString(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getName());
            pstmt.setDouble(5, transaction.getIncome());
            pstmt.setString(6, transaction.getWalletId());
            pstmt.setString(7, transaction.getCreateTime());
            pstmt.executeUpdate();
        }

        // Act - Retrieve
        String selectSql = "SELECT * FROM transaction_records WHERE id = ?";
        Transaction retrieved;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, transaction.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                retrieved = new Transaction();
                retrieved.setId(rs.getString("id"));
                retrieved.setCategoryId(rs.getString("categoryId"));
                retrieved.setAmount(rs.getDouble("amount"));
                retrieved.setName(rs.getString("name"));
                retrieved.setIncome(rs.getDouble("income"));
                retrieved.setWalletId(rs.getString("walletId"));
                retrieved.setCreateTime(rs.getString("createTime"));
            }
        }

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(transaction.getId());
        assertThat(retrieved.getCategoryId()).isEqualTo(transaction.getCategoryId());
        assertThat(retrieved.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(retrieved.getName()).isEqualTo(transaction.getName());
        assertThat(retrieved.getIncome()).isEqualTo(transaction.getIncome());
        assertThat(retrieved.getWalletId()).isEqualTo(transaction.getWalletId());
        assertThat(retrieved.getCreateTime()).isEqualTo(transaction.getCreateTime());
    }

    @Test
    @DisplayName("Should update transaction in database")
    void shouldUpdateTransaction() throws SQLException {
        // Arrange - Insert initial transaction
        Transaction transaction = new Transaction(
            "CAT_001",
            100.00,
            "Initial Name",
            0.0,
            "WAL_001",
            "2026-01-31T10:00:00"
        );

        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setString(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getName());
            pstmt.setDouble(5, transaction.getIncome());
            pstmt.setString(6, transaction.getWalletId());
            pstmt.setString(7, transaction.getCreateTime());
            pstmt.executeUpdate();
        }

        // Act - Update transaction
        String updateSql = "UPDATE transaction_records SET amount = ?, name = ? WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(updateSql)) {
            pstmt.setDouble(1, 200.00);
            pstmt.setString(2, "Updated Name");
            pstmt.setString(3, transaction.getId());
            pstmt.executeUpdate();
        }

        // Assert - Verify update
        String selectSql = "SELECT * FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, transaction.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble("amount")).isEqualTo(200.00);
                assertThat(rs.getString("name")).isEqualTo("Updated Name");
            }
        }
    }

    @Test
    @DisplayName("Should delete transaction from database")
    void shouldDeleteTransaction() throws SQLException {
        // Arrange - Insert transaction
        Transaction transaction = new Transaction(
            "CAT_001",
            100.00,
            "To Be Deleted",
            0.0,
            "WAL_001",
            "2026-01-31T10:00:00"
        );

        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setString(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getName());
            pstmt.setDouble(5, transaction.getIncome());
            pstmt.setString(6, transaction.getWalletId());
            pstmt.setString(7, transaction.getCreateTime());
            pstmt.executeUpdate();
        }

        // Act - Delete transaction
        String deleteSql = "DELETE FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(deleteSql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.executeUpdate();
        }

        // Assert - Verify deletion
        String selectSql = "SELECT * FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, transaction.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Should retrieve all transactions ordered by create time")
    void shouldRetrieveAllTransactionsOrderedByCreateTime() throws SQLException {
        // Arrange - Insert multiple transactions
        insertTransaction("TXN_001", "CAT_001", 100.00, "First", "2026-01-31T10:00:00");
        insertTransaction("TXN_002", "CAT_001", 200.00, "Second", "2026-01-31T11:00:00");
        insertTransaction("TXN_003", "CAT_001", 300.00, "Third", "2026-01-31T09:00:00");

        // Act - Retrieve all ordered by createTime DESC
        String selectSql = "SELECT * FROM transaction_records ORDER BY createTime DESC";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {

            // Assert
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Second"); // 11:00:00

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("First"); // 10:00:00

            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Third"); // 09:00:00

            assertThat(rs.next()).isFalse();
        }
    }

    @Test
    @DisplayName("Should filter transactions by wallet ID")
    void shouldFilterTransactionsByWalletId() throws SQLException {
        // Arrange
        insertTransactionWithWallet("TXN_001", "WAL_001", 100.00);
        insertTransactionWithWallet("TXN_002", "WAL_002", 200.00);
        insertTransactionWithWallet("TXN_003", "WAL_001", 300.00);

        // Act
        String selectSql = "SELECT * FROM transaction_records WHERE walletId = ?";
        int count = 0;
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, "WAL_001");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    count++;
                    assertThat(rs.getString("walletId")).isEqualTo("WAL_001");
                }
            }
        }

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate total expenses for a wallet")
    void shouldCalculateTotalExpensesForWallet() throws SQLException {
        // Arrange
        insertTransactionWithIncomeFlag("TXN_001", "WAL_001", 100.00, 0.0); // expense
        insertTransactionWithIncomeFlag("TXN_002", "WAL_001", 200.00, 0.0); // expense
        insertTransactionWithIncomeFlag("TXN_003", "WAL_001", 500.00, 1.0); // income

        // Act
        String selectSql = "SELECT SUM(amount) as total FROM transaction_records WHERE walletId = ? AND income = 0";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql)) {
            pstmt.setString(1, "WAL_001");
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                double totalExpenses = rs.getDouble("total");

                // Assert
                assertThat(totalExpenses).isEqualTo(300.00);
            }
        }
    }

    // Helper methods
    private void insertTransaction(String id, String categoryId, double amount, String name, String createTime) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                    "VALUES (?, ?, ?, ?, 0, 'WAL_001', ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, categoryId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, name);
            pstmt.setString(5, createTime);
            pstmt.executeUpdate();
        }
    }

    private void insertTransactionWithWallet(String id, String walletId, double amount) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                    "VALUES (?, 'CAT_001', ?, 'Test', 0, ?, '2026-01-31T10:00:00')";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, walletId);
            pstmt.executeUpdate();
        }
    }

    private void insertTransactionWithIncomeFlag(String id, String walletId, double amount, double income) throws SQLException {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                    "VALUES (?, 'CAT_001', ?, 'Test', ?, ?, '2026-01-31T10:00:00')";
        try (PreparedStatement pstmt = testConnection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setDouble(2, amount);
            pstmt.setDouble(3, income);
            pstmt.setString(4, walletId);
            pstmt.executeUpdate();
        }
    }
}
