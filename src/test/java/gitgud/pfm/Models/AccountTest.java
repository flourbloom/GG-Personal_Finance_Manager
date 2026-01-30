package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Account Model Tests")
class AccountTest {

    @Test
    @DisplayName("Should create account with all parameters")
    void shouldCreateAccountWithAllParameters() {
        // Arrange & Act
        Account account = new Account("blue", 1000.50, "Savings");

        // Assert
        assertThat(account).isNotNull();
        assertThat(account.getId()).isNotNull().startsWith("WAL_");
        assertThat(account.getColor()).isEqualTo("blue");
        assertThat(account.getBalance()).isEqualTo(1000.50);
        assertThat(account.getName()).isEqualTo("Savings");
    }

    @Test
    @DisplayName("Should create account with no-arg constructor")
    void shouldCreateAccountWithNoArgConstructor() {
        // Arrange & Act
        Account account = new Account();

        // Assert
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Account account = new Account();

        // Act
        account.setId("WAL_123");
        account.setColor("red");
        account.setBalance(5000.00);
        account.setName("Checking");

        // Assert
        assertThat(account.getId()).isEqualTo("WAL_123");
        assertThat(account.getColor()).isEqualTo("red");
        assertThat(account.getBalance()).isEqualTo(5000.00);
        assertThat(account.getName()).isEqualTo("Checking");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 100.0, 1000.50, 99999.99})
    @DisplayName("Should handle various balance values")
    void shouldHandleVariousBalanceValues(double balance) {
        // Arrange
        Account account = new Account();

        // Act
        account.setBalance(balance);

        // Assert
        assertThat(account.getBalance()).isEqualTo(balance);
    }

    @Test
    @DisplayName("Should inherit from FinancialEntity")
    void shouldInheritFromFinancialEntity() {
        // Arrange & Act
        Account account = new Account("green", 500.00, "Emergency Fund");

        // Assert
        assertThat(account).isInstanceOf(FinancialEntity.class);
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Arrange
        Account account = new Account();

        // Act
        account.setBalance(-100.00);

        // Assert
        assertThat(account.getBalance()).isEqualTo(-100.00);
    }

    @Test
    @DisplayName("Should update balance correctly")
    void shouldUpdateBalanceCorrectly() {
        // Arrange
        Account account = new Account("blue", 1000.00, "Main");

        // Act
        double newBalance = account.getBalance() + 500.00;
        account.setBalance(newBalance);

        // Assert
        assertThat(account.getBalance()).isEqualTo(1500.00);
    }

    @Test
    @DisplayName("Should generate unique IDs for different accounts")
    void shouldGenerateUniqueIdsForDifferentAccounts() {
        // Arrange & Act
        Account account1 = new Account("blue", 100.0, "Account1");
        Account account2 = new Account("red", 200.0, "Account2");

        // Assert
        assertThat(account1.getId()).isNotEqualTo(account2.getId());
    }

    @Test
    @DisplayName("Should handle different color values")
    void shouldHandleDifferentColorValues() {
        // Arrange
        String[] colors = {"red", "blue", "green", "#FF5733", "rgb(255,0,0)"};

        for (String color : colors) {
            // Act
            Account account = new Account(color, 100.0, "Test");

            // Assert
            assertThat(account.getColor()).isEqualTo(color);
        }
    }

    @Test
    @DisplayName("Should handle null color")
    void shouldHandleNullColor() {
        // Arrange
        Account account = new Account();

        // Act
        account.setColor(null);

        // Assert
        assertThat(account.getColor()).isNull();
    }
}
