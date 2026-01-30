package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Budget Model Tests")
class BudgetTest {

    @Test
    @DisplayName("Should create budget with all parameters")
    void shouldCreateBudgetWithAllParameters() {
        // Arrange & Act
        Budget budget = new Budget(
            "Monthly Food Budget",
            500.00,
            150.00,
            "2026-01-01",
            "2026-01-31"
        );

        // Assert
        assertThat(budget).isNotNull();
        assertThat(budget.getId()).isNotNull().startsWith("BUD_");
        assertThat(budget.getName()).isEqualTo("Monthly Food Budget");
        assertThat(budget.getLimitAmount()).isEqualTo(500.00);
        assertThat(budget.getBalance()).isEqualTo(150.00);
        assertThat(budget.getStartDate()).isEqualTo("2026-01-01");
        assertThat(budget.getEndDate()).isEqualTo("2026-01-31");
    }

    @Test
    @DisplayName("Should create budget with no-arg constructor")
    void shouldCreateBudgetWithNoArgConstructor() {
        // Arrange & Act
        Budget budget = new Budget();

        // Assert
        assertThat(budget).isNotNull();
        assertThat(budget.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Budget budget = new Budget();

        // Act
        budget.setId("BUD_123");
        budget.setName("Entertainment Budget");
        budget.setLimitAmount(300.00);
        budget.setBalance(75.00);
        budget.setStartDate("2026-02-01");
        budget.setEndDate("2026-02-28");

        // Assert
        assertThat(budget.getId()).isEqualTo("BUD_123");
        assertThat(budget.getName()).isEqualTo("Entertainment Budget");
        assertThat(budget.getLimitAmount()).isEqualTo(300.00);
        assertThat(budget.getBalance()).isEqualTo(75.00);
        assertThat(budget.getStartDate()).isEqualTo("2026-02-01");
        assertThat(budget.getEndDate()).isEqualTo("2026-02-28");
    }

    @Test
    @DisplayName("Should inherit from FinancialEntity")
    void shouldInheritFromFinancialEntity() {
        // Arrange & Act
        Budget budget = new Budget("Test", 1000.0, 500.0, "2026-01-01", "2026-12-31");

        // Assert
        assertThat(budget).isInstanceOf(FinancialEntity.class);
    }

    @ParameterizedTest
    @CsvSource({
        "1000.00, 0.00, true",
        "1000.00, 500.00, true",
        "1000.00, 1000.00, false",
        "1000.00, 1200.00, false"
    })
    @DisplayName("Should correctly identify if budget is under limit")
    void shouldIdentifyIfBudgetIsUnderLimit(double limit, double balance, boolean underLimit) {
        // Arrange
        Budget budget = new Budget("Test", limit, balance, "2026-01-01", "2026-01-31");

        // Act
        boolean result = budget.getBalance() < budget.getLimitAmount();

        // Assert
        assertThat(result).isEqualTo(underLimit);
    }

    @Test
    @DisplayName("Should calculate remaining budget")
    void shouldCalculateRemainingBudget() {
        // Arrange
        Budget budget = new Budget("Food", 500.0, 300.0, "2026-01-01", "2026-01-31");

        // Act
        double remaining = budget.getLimitAmount() - budget.getBalance();

        // Assert
        assertThat(remaining).isEqualTo(200.0);
    }

    @Test
    @DisplayName("Should handle budget overrun")
    void shouldHandleBudgetOverrun() {
        // Arrange
        Budget budget = new Budget("Shopping", 200.0, 250.0, "2026-01-01", "2026-01-31");

        // Act
        double overrun = budget.getBalance() - budget.getLimitAmount();

        // Assert
        assertThat(overrun).isPositive();
        assertThat(overrun).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Should generate unique IDs for different budgets")
    void shouldGenerateUniqueIdsForDifferentBudgets() {
        // Arrange & Act
        Budget budget1 = new Budget("Budget1", 100.0, 50.0, "2026-01-01", "2026-01-31");
        Budget budget2 = new Budget("Budget2", 200.0, 100.0, "2026-02-01", "2026-02-28");

        // Assert
        assertThat(budget1.getId()).isNotEqualTo(budget2.getId());
    }

    @Test
    @DisplayName("Should handle date range correctly")
    void shouldHandleDateRangeCorrectly() {
        // Arrange
        Budget budget = new Budget();

        // Act
        budget.setStartDate("2026-01-01");
        budget.setEndDate("2026-12-31");

        // Assert
        assertThat(budget.getStartDate()).isEqualTo("2026-01-01");
        assertThat(budget.getEndDate()).isEqualTo("2026-12-31");
    }

    @Test
    @DisplayName("Should handle zero limit amount")
    void shouldHandleZeroLimitAmount() {
        // Arrange
        Budget budget = new Budget();

        // Act
        budget.setLimitAmount(0.0);

        // Assert
        assertThat(budget.getLimitAmount()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should update balance as expenses are added")
    void shouldUpdateBalanceAsExpensesAreAdded() {
        // Arrange
        Budget budget = new Budget("Monthly", 1000.0, 0.0, "2026-01-01", "2026-01-31");

        // Act - simulate adding expenses
        budget.setBalance(budget.getBalance() + 50.0);
        budget.setBalance(budget.getBalance() + 75.0);
        budget.setBalance(budget.getBalance() + 100.0);

        // Assert
        assertThat(budget.getBalance()).isEqualTo(225.0);
    }
}
