package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Goal Model Tests")
class GoalTest {

    @Test
    @DisplayName("Should create goal with all parameters")
    void shouldCreateGoalWithAllParameters() {
        // Arrange & Act
        Goal goal = new Goal(
            "New Car",
            20000.00,
            5000.00,
            "2026-12-31",
            1.0,
            "2026-01-01"
        );

        // Assert
        assertThat(goal).isNotNull();
        assertThat(goal.getId()).isNotNull().startsWith("GOL_");
        assertThat(goal.getName()).isEqualTo("New Car");
        assertThat(goal.getTarget()).isEqualTo(20000.00);
        assertThat(goal.getBalance()).isEqualTo(5000.00);
        assertThat(goal.getDeadline()).isEqualTo("2026-12-31");
        assertThat(goal.getPriority()).isEqualTo(1.0);
        assertThat(goal.getCreateTime()).isEqualTo("2026-01-01");
    }

    @Test
    @DisplayName("Should create goal with no-arg constructor")
    void shouldCreateGoalWithNoArgConstructor() {
        // Arrange & Act
        Goal goal = new Goal();

        // Assert
        assertThat(goal).isNotNull();
        assertThat(goal.getBalance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Goal goal = new Goal();

        // Act
        goal.setId("GOL_123");
        goal.setName("Vacation");
        goal.setTarget(5000.00);
        goal.setBalance(1500.00);
        goal.setDeadline("2026-06-30");
        goal.setPriority(2.0);
        goal.setCreateTime("2026-01-15");

        // Assert
        assertThat(goal.getId()).isEqualTo("GOL_123");
        assertThat(goal.getName()).isEqualTo("Vacation");
        assertThat(goal.getTarget()).isEqualTo(5000.00);
        assertThat(goal.getBalance()).isEqualTo(1500.00);
        assertThat(goal.getDeadline()).isEqualTo("2026-06-30");
        assertThat(goal.getPriority()).isEqualTo(2.0);
        assertThat(goal.getCreateTime()).isEqualTo("2026-01-15");
    }

    @Test
    @DisplayName("Should inherit from FinancialEntity")
    void shouldInheritFromFinancialEntity() {
        // Arrange & Act
        Goal goal = new Goal("Test", 1000.0, 500.0, "2026-12-31", 1.0, "2026-01-01");

        // Assert
        assertThat(goal).isInstanceOf(FinancialEntity.class);
    }

    @ParameterizedTest
    @CsvSource({
        "10000.00, 2000.00, 20.0",
        "5000.00, 5000.00, 100.0",
        "1000.00, 0.00, 0.0",
        "2000.00, 500.00, 25.0"
    })
    @DisplayName("Should calculate progress percentage correctly")
    void shouldCalculateProgressPercentageCorrectly(double target, double current, double expectedPercentage) {
        // Arrange
        Goal goal = new Goal("Test", target, current, "2026-12-31", 1.0, "2026-01-01");

        // Act
        double progress = (goal.getBalance() / goal.getTarget()) * 100;

        // Assert
        assertThat(progress).isEqualTo(expectedPercentage);
    }

    @Test
    @DisplayName("Should calculate remaining amount to reach goal")
    void shouldCalculateRemainingAmountToReachGoal() {
        // Arrange
        Goal goal = new Goal("Emergency Fund", 10000.0, 6000.0, "2026-12-31", 1.0, "2026-01-01");

        // Act
        double remaining = goal.getTarget() - goal.getBalance();

        // Assert
        assertThat(remaining).isEqualTo(4000.0);
    }

    @Test
    @DisplayName("Should identify goal completion")
    void shouldIdentifyGoalCompletion() {
        // Arrange
        Goal goal = new Goal("Laptop", 1500.0, 1500.0, "2026-12-31", 1.0, "2026-01-01");

        // Act
        boolean isComplete = goal.getBalance() >= goal.getTarget();

        // Assert
        assertThat(isComplete).isTrue();
    }

    @Test
    @DisplayName("Should identify incomplete goal")
    void shouldIdentifyIncompleteGoal() {
        // Arrange
        Goal goal = new Goal("Laptop", 1500.0, 800.0, "2026-12-31", 1.0, "2026-01-01");

        // Act
        boolean isComplete = goal.getBalance() >= goal.getTarget();

        // Assert
        assertThat(isComplete).isFalse();
    }

    @Test
    @DisplayName("Should handle priority levels")
    void shouldHandlePriorityLevels() {
        // Arrange
        Goal highPriority = new Goal("Emergency", 10000.0, 0.0, "2026-12-31", 1.0, "2026-01-01");
        Goal mediumPriority = new Goal("Vacation", 5000.0, 0.0, "2027-06-30", 2.0, "2026-01-01");
        Goal lowPriority = new Goal("Gadget", 1000.0, 0.0, "2028-12-31", 3.0, "2026-01-01");

        // Assert
        assertThat(highPriority.getPriority()).isLessThan(mediumPriority.getPriority());
        assertThat(mediumPriority.getPriority()).isLessThan(lowPriority.getPriority());
    }

    @Test
    @DisplayName("Should generate unique IDs for different goals")
    void shouldGenerateUniqueIdsForDifferentGoals() {
        // Arrange & Act
        Goal goal1 = new Goal("Goal1", 1000.0, 100.0, "2026-12-31", 1.0, "2026-01-01");
        Goal goal2 = new Goal("Goal2", 2000.0, 200.0, "2026-12-31", 1.0, "2026-01-01");

        // Assert
        assertThat(goal1.getId()).isNotEqualTo(goal2.getId());
    }

    @Test
    @DisplayName("Should update current amount when contributions are made")
    void shouldUpdateCurrentAmountWhenContributionsAreMade() {
        // Arrange
        Goal goal = new Goal("Savings", 10000.0, 1000.0, "2026-12-31", 1.0, "2026-01-01");

        // Act - simulate contributions
        goal.setBalance(goal.getBalance() + 500.0);
        goal.setBalance(goal.getBalance() + 300.0);

        // Assert
        assertThat(goal.getBalance()).isEqualTo(1800.0);
    }

    @Test
    @DisplayName("Should handle goal with zero target")
    void shouldHandleGoalWithZeroTarget() {
        // Arrange
        Goal goal = new Goal();

        // Act
        goal.setTarget(0.0);

        // Assert
        assertThat(goal.getTarget()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle create time and deadline")
    void shouldHandleCreateTimeAndDeadline() {
        // Arrange
        Goal goal = new Goal();

        // Act
        goal.setCreateTime("2026-01-01T00:00:00");
        goal.setDeadline("2026-12-31T23:59:59");

        // Assert
        assertThat(goal.getCreateTime()).isEqualTo("2026-01-01T00:00:00");
        assertThat(goal.getDeadline()).isEqualTo("2026-12-31T23:59:59");
    }
}
