package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FinancialEntity Abstract Class Tests")
class FinancialEntityTest {

    // Concrete implementation for testing purposes
    private static class TestFinancialEntity extends FinancialEntity {
        public TestFinancialEntity(String id, String name, double balance) {
            super(id, name, balance);
        }
    }

    @Test
    @DisplayName("Should create financial entity with all parameters")
    void shouldCreateFinancialEntityWithAllParameters() {
        // Arrange & Act
        TestFinancialEntity entity = new TestFinancialEntity("TEST_123", "Test Entity", 1000.0);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo("TEST_123");
        assertThat(entity.getName()).isEqualTo("Test Entity");
        assertThat(entity.getBalance()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Should set and get ID")
    void shouldSetAndGetId() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Test", 0.0);

        // Act
        entity.setId("ID_2");

        // Assert
        assertThat(entity.getId()).isEqualTo("ID_2");
    }

    @Test
    @DisplayName("Should set and get name")
    void shouldSetAndGetName() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Original", 0.0);

        // Act
        entity.setName("Updated Name");

        // Assert
        assertThat(entity.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should set and get balance")
    void shouldSetAndGetBalance() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Test", 100.0);

        // Act
        entity.setBalance(500.0);

        // Assert
        assertThat(entity.getBalance()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity(null, null, 0.0);

        // Assert
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isNull();
        assertThat(entity.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Arrange & Act
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Test", -100.0);

        // Assert
        assertThat(entity.getBalance()).isEqualTo(-100.0);
    }

    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Arrange & Act
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Test", 0.0);

        // Assert
        assertThat(entity.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should be extendable by subclasses")
    void shouldBeExtendableBySubclasses() {
        // Arrange & Act
        Account account = new Account("blue", 100.0, "Account");
        Budget budget = new Budget("Budget", 500.0, 100.0, "2026-01-01", "2026-01-31");
        Goal goal = new Goal("Goal", 1000.0, 200.0, "2026-12-31", 1.0, "2026-01-01");
        Wallet wallet = new Wallet("green", 300.0, "Wallet");

        // Assert
        assertThat(account).isInstanceOf(FinancialEntity.class);
        assertThat(budget).isInstanceOf(FinancialEntity.class);
        assertThat(goal).isInstanceOf(FinancialEntity.class);
        assertThat(wallet).isInstanceOf(FinancialEntity.class);
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStringValues() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity("", "", 0.0);

        // Assert
        assertThat(entity.getId()).isEmpty();
        assertThat(entity.getName()).isEmpty();
    }

    @Test
    @DisplayName("Should update balance correctly")
    void shouldUpdateBalanceCorrectly() {
        // Arrange
        TestFinancialEntity entity = new TestFinancialEntity("ID_1", "Test", 1000.0);

        // Act - add income
        entity.setBalance(entity.getBalance() + 500.0);

        // Assert
        assertThat(entity.getBalance()).isEqualTo(1500.0);

        // Act - subtract expense
        entity.setBalance(entity.getBalance() - 200.0);

        // Assert
        assertThat(entity.getBalance()).isEqualTo(1300.0);
    }
}
