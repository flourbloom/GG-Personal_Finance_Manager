package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Wallet Model Tests")
class WalletTest {

    @Test
    @DisplayName("Should create wallet with all parameters")
    void shouldCreateWalletWithAllParameters() {
        // Arrange & Act
        Wallet wallet = new Wallet("purple", 2500.00, "Main Wallet");

        // Assert
        assertThat(wallet).isNotNull();
        assertThat(wallet.getId()).isNotNull().startsWith("WAL_");
        assertThat(wallet.getColor()).isEqualTo("purple");
        assertThat(wallet.getBalance()).isEqualTo(2500.00);
        assertThat(wallet.getName()).isEqualTo("Main Wallet");
    }

    @Test
    @DisplayName("Should create wallet with no-arg constructor")
    void shouldCreateWalletWithNoArgConstructor() {
        // Arrange & Act
        Wallet wallet = new Wallet();

        // Assert
        assertThat(wallet).isNotNull();
        assertThat(wallet.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Wallet wallet = new Wallet();

        // Act
        wallet.setId("WAL_456");
        wallet.setColor("orange");
        wallet.setBalance(3000.00);
        wallet.setName("Secondary Wallet");

        // Assert
        assertThat(wallet.getId()).isEqualTo("WAL_456");
        assertThat(wallet.getColor()).isEqualTo("orange");
        assertThat(wallet.getBalance()).isEqualTo(3000.00);
        assertThat(wallet.getName()).isEqualTo("Secondary Wallet");
    }

    @Test
    @DisplayName("Should inherit from FinancialEntity")
    void shouldInheritFromFinancialEntity() {
        // Arrange & Act
        Wallet wallet = new Wallet("blue", 1000.0, "Test Wallet");

        // Assert
        assertThat(wallet).isInstanceOf(FinancialEntity.class);
    }

    @Test
    @DisplayName("Should generate unique IDs for different wallets")
    void shouldGenerateUniqueIdsForDifferentWallets() {
        // Arrange & Act
        Wallet wallet1 = new Wallet("red", 100.0, "Wallet1");
        Wallet wallet2 = new Wallet("blue", 200.0, "Wallet2");

        // Assert
        assertThat(wallet1.getId()).isNotEqualTo(wallet2.getId());
    }

    @Test
    @DisplayName("Should handle different color formats")
    void shouldHandleDifferentColorFormats() {
        // Arrange & Act
        Wallet hexColor = new Wallet("#FF5733", 100.0, "Hex");
        Wallet rgbColor = new Wallet("rgb(255,87,51)", 200.0, "RGB");
        Wallet namedColor = new Wallet("blue", 300.0, "Named");

        // Assert
        assertThat(hexColor.getColor()).isEqualTo("#FF5733");
        assertThat(rgbColor.getColor()).isEqualTo("rgb(255,87,51)");
        assertThat(namedColor.getColor()).isEqualTo("blue");
    }

    @Test
    @DisplayName("Should handle balance updates")
    void shouldHandleBalanceUpdates() {
        // Arrange
        Wallet wallet = new Wallet("green", 1000.0, "Main");

        // Act - simulate income
        wallet.setBalance(wallet.getBalance() + 500.0);

        // Assert
        assertThat(wallet.getBalance()).isEqualTo(1500.0);

        // Act - simulate expense
        wallet.setBalance(wallet.getBalance() - 200.0);

        // Assert
        assertThat(wallet.getBalance()).isEqualTo(1300.0);
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Arrange
        Wallet wallet = new Wallet("red", 100.0, "Overdraft");

        // Act
        wallet.setBalance(-50.0);

        // Assert
        assertThat(wallet.getBalance()).isEqualTo(-50.0);
    }

    @Test
    @DisplayName("Should handle null color")
    void shouldHandleNullColor() {
        // Arrange
        Wallet wallet = new Wallet();

        // Act
        wallet.setColor(null);

        // Assert
        assertThat(wallet.getColor()).isNull();
    }

    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Arrange
        Wallet wallet = new Wallet("black", 0.0, "Empty");

        // Assert
        assertThat(wallet.getBalance()).isEqualTo(0.0);
    }
}
