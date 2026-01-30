package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transaction Model Tests")
class TransactionTest {

    @Test
    @DisplayName("Should create transaction with all parameters")
    void shouldCreateTransactionWithAllParameters() {
        // Arrange & Act
        Transaction transaction = new Transaction(
            "CAT_123",
            150.75,
            "Grocery Shopping",
            0.0,
            "WAL_456",
            "2026-01-31T10:30:00"
        );

        // Assert
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isNotNull().startsWith("TXN_");
        assertThat(transaction.getCategoryId()).isEqualTo("CAT_123");
        assertThat(transaction.getAmount()).isEqualTo(150.75);
        assertThat(transaction.getName()).isEqualTo("Grocery Shopping");
        assertThat(transaction.getIncome()).isEqualTo(0.0);
        assertThat(transaction.getWalletId()).isEqualTo("WAL_456");
        assertThat(transaction.getCreateTime()).isEqualTo("2026-01-31T10:30:00");
    }

    @Test
    @DisplayName("Should create transaction with no-arg constructor")
    void shouldCreateTransactionWithNoArgConstructor() {
        // Arrange & Act
        Transaction transaction = new Transaction();

        // Assert
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getAmount()).isEqualTo(0.0);
        assertThat(transaction.getIncome()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        transaction.setId("TXN_999");
        transaction.setCategoryId("CAT_789");
        transaction.setAmount(500.00);
        transaction.setName("Salary");
        transaction.setIncome(1.0);
        transaction.setWalletId("WAL_111");
        transaction.setCreateTime("2026-01-31T12:00:00");

        // Assert
        assertThat(transaction.getId()).isEqualTo("TXN_999");
        assertThat(transaction.getCategoryId()).isEqualTo("CAT_789");
        assertThat(transaction.getAmount()).isEqualTo(500.00);
        assertThat(transaction.getName()).isEqualTo("Salary");
        assertThat(transaction.getIncome()).isEqualTo(1.0);
        assertThat(transaction.getWalletId()).isEqualTo("WAL_111");
        assertThat(transaction.getCreateTime()).isEqualTo("2026-01-31T12:00:00");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.01, 10.50, 100.00, 999999.99})
    @DisplayName("Should handle various amount values")
    void shouldHandleVariousAmountValues(double amount) {
        // Arrange & Act
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);

        // Assert
        assertThat(transaction.getAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Should distinguish between income and expense")
    void shouldDistinguishBetweenIncomeAndExpense() {
        // Arrange
        Transaction income = new Transaction("CAT_1", 1000.0, "Salary", 1.0, "WAL_1", "2026-01-31");
        Transaction expense = new Transaction("CAT_2", 50.0, "Coffee", 0.0, "WAL_1", "2026-01-31");

        // Assert
        assertThat(income.getIncome()).isEqualTo(1.0);
        assertThat(expense.getIncome()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void shouldHandleNullValuesInSetters() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        transaction.setId(null);
        transaction.setCategoryId(null);
        transaction.setName(null);
        transaction.setWalletId(null);
        transaction.setCreateTime(null);

        // Assert
        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getCategoryId()).isNull();
        assertThat(transaction.getName()).isNull();
        assertThat(transaction.getWalletId()).isNull();
        assertThat(transaction.getCreateTime()).isNull();
    }

    @Test
    @DisplayName("Should generate unique IDs for different transactions")
    void shouldGenerateUniqueIdsForDifferentTransactions() {
        // Arrange & Act
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        // Assert
        assertThat(transaction1.getId()).isNotEqualTo(transaction2.getId());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
        // Arrange
        Transaction transaction = new Transaction();

        // Act
        transaction.setName("");
        transaction.setCategoryId("");
        transaction.setWalletId("");

        // Assert
        assertThat(transaction.getName()).isEmpty();
        assertThat(transaction.getCategoryId()).isEmpty();
        assertThat(transaction.getWalletId()).isEmpty();
    }
}
