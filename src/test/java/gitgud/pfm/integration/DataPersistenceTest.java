package gitgud.pfm.integration;

import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for data persistence and integrity.
 * Tests database operations, constraints, and data survival scenarios.
 */
@DisplayName("Data Persistence Integration Tests")
class DataPersistenceTest extends IntegrationTestBase {

    @BeforeEach
    @Override
    protected void setUpDatabase() throws SQLException {
        super.setUpDatabase();
        insertTestData();
    }

    private void insertTestData() throws SQLException {
        // Insert categories
        String catSql = "INSERT INTO categories (id, name, description, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(catSql)) {
            pstmt.setString(1, "CAT_FOOD");
            pstmt.setString(2, "Food");
            pstmt.setString(3, "Food expenses");
            pstmt.setString(4, "EXPENSE");
            pstmt.executeUpdate();
            
            pstmt.setString(1, "CAT_SALARY");
            pstmt.setString(2, "Salary");
            pstmt.setString(3, "Salary income");
            pstmt.setString(4, "INCOME");
            pstmt.executeUpdate();
        }

        // Insert wallet
        String walletSql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(walletSql)) {
            pstmt.setString(1, "WAL_TEST");
            pstmt.setString(2, "Test Wallet");
            pstmt.setDouble(3, 500000.0);
            pstmt.setString(4, "Blue");
            pstmt.executeUpdate();
        }
    }

    // ==================== DATA PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Should persist transaction data correctly")
    void shouldPersistTransactionDataCorrectly() throws SQLException {
        // Insert transaction
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "TXN_001");
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, "Grocery Shopping");
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, "WAL_TEST");
            pstmt.setString(7, "2026-01-31T10:30:00");
            pstmt.executeUpdate();
        }

        // Read back and verify
        String selectSql = "SELECT * FROM transaction_records WHERE id = 'TXN_001'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("categoryId")).isEqualTo("CAT_FOOD");
            assertThat(rs.getDouble("amount")).isEqualTo(50000.0);
            assertThat(rs.getString("name")).isEqualTo("Grocery Shopping");
            assertThat(rs.getDouble("income")).isEqualTo(0.0);
            assertThat(rs.getString("walletId")).isEqualTo("WAL_TEST");
            assertThat(rs.getString("createTime")).isEqualTo("2026-01-31T10:30:00");
        }
    }

    @Test
    @DisplayName("Should persist budget data correctly")
    void shouldPersistBudgetDataCorrectly() throws SQLException {
        // Insert budget
        String insertSql = "INSERT INTO Budget (id, name, limitAmount, balance, startDate, endDate) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "BUD_001");
            pstmt.setString(2, "Monthly Food Budget");
            pstmt.setDouble(3, 500000.0);
            pstmt.setDouble(4, 100000.0);
            pstmt.setString(5, "2026-01-01");
            pstmt.setString(6, "2026-01-31");
            pstmt.executeUpdate();
        }

        // Read back and verify
        String selectSql = "SELECT * FROM Budget WHERE id = 'BUD_001'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Monthly Food Budget");
            assertThat(rs.getDouble("limitAmount")).isEqualTo(500000.0);
            assertThat(rs.getDouble("balance")).isEqualTo(100000.0);
            assertThat(rs.getString("startDate")).isEqualTo("2026-01-01");
            assertThat(rs.getString("endDate")).isEqualTo("2026-01-31");
        }
    }

    @Test
    @DisplayName("Should persist goal data correctly")
    void shouldPersistGoalDataCorrectly() throws SQLException {
        // Insert goal
        String insertSql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "GOAL_001");
            pstmt.setString(2, "Emergency Fund");
            pstmt.setDouble(3, 1000000.0);
            pstmt.setDouble(4, 250000.0);
            pstmt.setString(5, "2026-12-31");
            pstmt.setDouble(6, 1.0);
            pstmt.setString(7, "2026-01-01T00:00:00");
            pstmt.executeUpdate();
        }

        // Read back and verify
        String selectSql = "SELECT * FROM Goal WHERE id = 'GOAL_001'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo("Emergency Fund");
            assertThat(rs.getDouble("target")).isEqualTo(1000000.0);
            assertThat(rs.getDouble("balance")).isEqualTo(250000.0);
            assertThat(rs.getString("deadline")).isEqualTo("2026-12-31");
            assertThat(rs.getDouble("priority")).isEqualTo(1.0);
        }
    }

    // ==================== UPDATE PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Should persist updates correctly")
    void shouldPersistUpdatesCorrectly() throws SQLException {
        // Insert initial data
        String insertSql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                          "VALUES ('GOAL_UPD', 'Update Test', 500000, 100000, '2026-12-31', 0.5, '2026-01-01T00:00:00')";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(insertSql);
        }

        // Update
        String updateSql = "UPDATE Goal SET balance = 200000, priority = 1.0 WHERE id = 'GOAL_UPD'";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(updateSql);
        }

        // Verify update persisted
        String selectSql = "SELECT balance, priority FROM Goal WHERE id = 'GOAL_UPD'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getDouble("balance")).isEqualTo(200000.0);
            assertThat(rs.getDouble("priority")).isEqualTo(1.0);
        }
    }

    // ==================== DELETE PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Should remove deleted data completely")
    void shouldRemoveDeletedDataCompletely() throws SQLException {
        // Insert
        String insertSql = "INSERT INTO Goal (id, name, target, balance, deadline, priority, createAt) " +
                          "VALUES ('GOAL_DEL', 'Delete Test', 500000, 0, '2026-12-31', 1.0, '2026-01-01T00:00:00')";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(insertSql);
        }

        // Verify exists
        String countSql = "SELECT COUNT(*) FROM Goal WHERE id = 'GOAL_DEL'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }

        // Delete
        String deleteSql = "DELETE FROM Goal WHERE id = 'GOAL_DEL'";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(deleteSql);
        }

        // Verify deleted
        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(0);
        }
    }

    // ==================== DATA INTEGRITY TESTS ====================

    @Test
    @DisplayName("Should maintain referential integrity - valid foreign key")
    void shouldMaintainReferentialIntegrityValidFK() throws SQLException {
        // Insert transaction with valid category FK
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES ('TXN_FK', 'CAT_FOOD', 50000, 'Valid FK', 0, 'WAL_TEST', '2026-01-15T10:00:00')";
        
        // Should succeed
        try (Statement stmt = testConnection.createStatement()) {
            int rows = stmt.executeUpdate(insertSql);
            assertThat(rows).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Should handle large dataset without corruption")
    void shouldHandleLargeDatasetWithoutCorruption() throws SQLException {
        // Insert 1000 transactions
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            for (int i = 0; i < 1000; i++) {
                pstmt.setString(1, "TXN_BULK_" + i);
                pstmt.setString(2, "CAT_FOOD");
                pstmt.setDouble(3, 1000.0 + i);
                pstmt.setString(4, "Transaction " + i);
                pstmt.setDouble(5, i % 2); // Alternating income/expense
                pstmt.setString(6, "WAL_TEST");
                pstmt.setString(7, "2026-01-15T10:00:00");
                pstmt.executeUpdate();
            }
        }

        // Count and verify
        String countSql = "SELECT COUNT(*) FROM transaction_records";
        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getInt(1)).isEqualTo(1000);
        }

        // Verify data integrity - check a few random records
        String verifySql = "SELECT amount, name FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = testConnection.prepareStatement(verifySql)) {
            pstmt.setString(1, "TXN_BULK_500");
            try (ResultSet rs = pstmt.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble("amount")).isEqualTo(1500.0);
                assertThat(rs.getString("name")).isEqualTo("Transaction 500");
            }
        }
    }

    // ==================== DECIMAL PRECISION TESTS ====================

    @Test
    @DisplayName("Should maintain decimal precision for currency values")
    void shouldMaintainDecimalPrecisionForCurrencyValues() throws SQLException {
        // Insert transaction with precise decimal
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES ('TXN_PREC', 'CAT_FOOD', 12345.67, 'Precision Test', 0, 'WAL_TEST', '2026-01-15T10:00:00')";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(insertSql);
        }

        // Verify precision maintained
        String selectSql = "SELECT amount FROM transaction_records WHERE id = 'TXN_PREC'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getDouble("amount")).isEqualTo(12345.67);
        }
    }

    @Test
    @DisplayName("Should handle very large amounts correctly")
    void shouldHandleVeryLargeAmountsCorrectly() throws SQLException {
        // Cambodian Riel - might have large numbers
        double largeAmount = 9999999999999.99;
        
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "TXN_LARGE");
            pstmt.setString(2, "CAT_SALARY");
            pstmt.setDouble(3, largeAmount);
            pstmt.setString(4, "Large Amount");
            pstmt.setDouble(5, 1.0);
            pstmt.setString(6, "WAL_TEST");
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        // Verify
        String selectSql = "SELECT amount FROM transaction_records WHERE id = 'TXN_LARGE'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getDouble("amount")).isEqualTo(largeAmount);
        }
    }

    // ==================== STRING DATA TESTS ====================

    @Test
    @DisplayName("Should handle special characters in text fields")
    void shouldHandleSpecialCharactersInTextFields() throws SQLException {
        String specialName = "Food & Drinks! @#$%^&*() 'Test' \"Quotes\"";
        
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "TXN_SPECIAL");
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, specialName);
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, "WAL_TEST");
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        // Verify special characters preserved
        String selectSql = "SELECT name FROM transaction_records WHERE id = 'TXN_SPECIAL'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo(specialName);
        }
    }

    @Test
    @DisplayName("Should handle Unicode characters")
    void shouldHandleUnicodeCharacters() throws SQLException {
        // Khmer script (for Cambodian context)
        String unicodeName = "ការចំណាយអាហារ"; // "Food expenses" in Khmer
        
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "TXN_UNICODE");
            pstmt.setString(2, "CAT_FOOD");
            pstmt.setDouble(3, 50000.0);
            pstmt.setString(4, unicodeName);
            pstmt.setDouble(5, 0.0);
            pstmt.setString(6, "WAL_TEST");
            pstmt.setString(7, "2026-01-15T10:00:00");
            pstmt.executeUpdate();
        }

        // Verify Unicode preserved
        String selectSql = "SELECT name FROM transaction_records WHERE id = 'TXN_UNICODE'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("name")).isEqualTo(unicodeName);
        }
    }

    // ==================== NULL HANDLING TESTS ====================

    @Test
    @DisplayName("Should handle nullable fields correctly")
    void shouldHandleNullableFieldsCorrectly() throws SQLException {
        // Insert with null color for wallet
        String insertSql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            pstmt.setString(1, "WAL_NULL");
            pstmt.setString(2, "Null Color Wallet");
            pstmt.setDouble(3, 100000.0);
            pstmt.setNull(4, java.sql.Types.VARCHAR);
            pstmt.executeUpdate();
        }

        // Verify null handled
        String selectSql = "SELECT color FROM Wallet WHERE id = 'WAL_NULL'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("color")).isNull();
            assertThat(rs.wasNull()).isTrue();
        }
    }

    // ==================== AGGREGATION TESTS ====================

    @Test
    @DisplayName("Should calculate aggregations correctly")
    void shouldCalculateAggregationsCorrectly() throws SQLException {
        // Insert test transactions
        String insertSql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = testConnection.prepareStatement(insertSql)) {
            // Income transactions
            pstmt.setString(1, "TXN_INC1"); pstmt.setString(2, "CAT_SALARY"); pstmt.setDouble(3, 100000);
            pstmt.setString(4, "Inc1"); pstmt.setDouble(5, 1.0); pstmt.setString(6, "WAL_TEST"); pstmt.setString(7, "2026-01-01T10:00:00");
            pstmt.executeUpdate();
            
            pstmt.setString(1, "TXN_INC2"); pstmt.setDouble(3, 50000);
            pstmt.setString(4, "Inc2");
            pstmt.executeUpdate();
            
            // Expense transactions
            pstmt.setString(1, "TXN_EXP1"); pstmt.setString(2, "CAT_FOOD"); pstmt.setDouble(3, 30000);
            pstmt.setString(4, "Exp1"); pstmt.setDouble(5, 0.0);
            pstmt.executeUpdate();
            
            pstmt.setString(1, "TXN_EXP2"); pstmt.setDouble(3, 20000);
            pstmt.setString(4, "Exp2");
            pstmt.executeUpdate();
        }

        // Test SUM for income
        String incomeSql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 1";
        try (PreparedStatement pstmt = testConnection.prepareStatement(incomeSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getDouble("total")).isEqualTo(150000.0);
        }

        // Test SUM for expenses
        String expenseSql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 0";
        try (PreparedStatement pstmt = testConnection.prepareStatement(expenseSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getDouble("total")).isEqualTo(50000.0);
        }

        // Test COUNT
        String countSql = "SELECT COUNT(*) as cnt FROM transaction_records WHERE walletId = 'WAL_TEST'";
        try (PreparedStatement pstmt = testConnection.prepareStatement(countSql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            assertThat(rs.getInt("cnt")).isEqualTo(4);
        }
    }
}
