package gitgud.pfm.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IdGenerator Utility Tests")
class IdGeneratorTest {

    @Test
    @DisplayName("Should generate wallet ID with correct prefix")
    void shouldGenerateWalletIdWithCorrectPrefix() {
        // Act
        String walletId = IdGenerator.generateWalletId();

        // Assert
        assertThat(walletId).startsWith("WAL_");
    }

    @Test
    @DisplayName("Should generate budget ID with correct prefix")
    void shouldGenerateBudgetIdWithCorrectPrefix() {
        // Act
        String budgetId = IdGenerator.generateBudgetId();

        // Assert
        assertThat(budgetId).startsWith("BUD_");
    }

    @Test
    @DisplayName("Should generate goal ID with correct prefix")
    void shouldGenerateGoalIdWithCorrectPrefix() {
        // Act
        String goalId = IdGenerator.generateGoalId();

        // Assert
        assertThat(goalId).startsWith("GOL_");
    }

    @Test
    @DisplayName("Should generate transaction ID with correct prefix")
    void shouldGenerateTransactionIdWithCorrectPrefix() {
        // Act
        String transactionId = IdGenerator.generateTransactionId();

        // Assert
        assertThat(transactionId).startsWith("TXN_");
    }

    @Test
    @DisplayName("Generated wallet IDs should be non-null and not empty")
    void generatedWalletIdsShouldBeNonNullAndNotEmpty() {
        // Act
        String walletId = IdGenerator.generateWalletId();

        // Assert
        assertThat(walletId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Generated IDs should contain timestamp")
    void generatedIdsShouldContainTimestamp() {
        // Arrange
        long beforeTimestamp = System.currentTimeMillis();

        // Act
        String walletId = IdGenerator.generateWalletId();

        // Assert - ID format: PREFIX_timestamp_uuid
        String[] parts = walletId.split("_");
        assertThat(parts).hasSizeGreaterThanOrEqualTo(3);

        // Verify timestamp is reasonable (within 1 second)
        long idTimestamp = Long.parseLong(parts[1]);
        long afterTimestamp = System.currentTimeMillis();
        assertThat(idTimestamp).isBetween(beforeTimestamp, afterTimestamp + 1000);
    }

    @Test
    @DisplayName("Generated IDs should contain UUID portion")
    void generatedIdsShouldContainUuidPortion() {
        // Act
        String walletId = IdGenerator.generateWalletId();

        // Assert - ID format: PREFIX_timestamp_uuid
        String[] parts = walletId.split("_");
        assertThat(parts).hasSizeGreaterThanOrEqualTo(3);

        // UUID portion should be 8 characters
        String uuidPortion = parts[2];
        assertThat(uuidPortion).hasSize(8);
    }

    @RepeatedTest(100)
    @DisplayName("Should generate unique wallet IDs")
    void shouldGenerateUniqueWalletIds() {
        // Arrange
        Set<String> ids = new HashSet<>();

        // Act
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateWalletId();
            ids.add(id);
        }

        // Assert - all IDs should be unique
        assertThat(ids).hasSize(100);
    }

    @RepeatedTest(100)
    @DisplayName("Should generate unique budget IDs")
    void shouldGenerateUniqueBudgetIds() {
        // Arrange
        Set<String> ids = new HashSet<>();

        // Act
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateBudgetId();
            ids.add(id);
        }

        // Assert - all IDs should be unique
        assertThat(ids).hasSize(100);
    }

    @RepeatedTest(100)
    @DisplayName("Should generate unique goal IDs")
    void shouldGenerateUniqueGoalIds() {
        // Arrange
        Set<String> ids = new HashSet<>();

        // Act
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateGoalId();
            ids.add(id);
        }

        // Assert - all IDs should be unique
        assertThat(ids).hasSize(100);
    }

    @RepeatedTest(100)
    @DisplayName("Should generate unique transaction IDs")
    void shouldGenerateUniqueTransactionIds() {
        // Arrange
        Set<String> ids = new HashSet<>();

        // Act
        for (int i = 0; i < 100; i++) {
            String id = IdGenerator.generateTransactionId();
            ids.add(id);
        }

        // Assert - all IDs should be unique
        assertThat(ids).hasSize(100);
    }

    @Test
    @DisplayName("Different ID types should have different prefixes")
    void differentIdTypesShouldHaveDifferentPrefixes() {
        // Act
        String walletId = IdGenerator.generateWalletId();
        String budgetId = IdGenerator.generateBudgetId();
        String goalId = IdGenerator.generateGoalId();
        String transactionId = IdGenerator.generateTransactionId();

        // Assert
        assertThat(walletId).startsWith("WAL_");
        assertThat(budgetId).startsWith("BUD_");
        assertThat(goalId).startsWith("GOL_");
        assertThat(transactionId).startsWith("TXN_");

        // Verify all are different
        assertThat(walletId.substring(0, 4)).isNotEqualTo(budgetId.substring(0, 4));
        assertThat(walletId.substring(0, 4)).isNotEqualTo(goalId.substring(0, 4));
        assertThat(walletId.substring(0, 4)).isNotEqualTo(transactionId.substring(0, 4));
    }

    @Test
    @DisplayName("Generated IDs should have consistent format")
    void generatedIdsShouldHaveConsistentFormat() {
        // Act
        String walletId = IdGenerator.generateWalletId();
        String budgetId = IdGenerator.generateBudgetId();
        String goalId = IdGenerator.generateGoalId();
        String transactionId = IdGenerator.generateTransactionId();

        // Assert - Format: PREFIX_timestamp_uuid
        // All should have exactly 3 parts when split by underscore
        assertThat(walletId.split("_")).hasSize(3);
        assertThat(budgetId.split("_")).hasSize(3);
        assertThat(goalId.split("_")).hasSize(3);
        assertThat(transactionId.split("_")).hasSize(3);
    }

    @Test
    @DisplayName("Generated IDs should have reasonable length")
    void generatedIdsShouldHaveReasonableLength() {
        // Act
        String walletId = IdGenerator.generateWalletId();

        // Assert - PREFIX(4) + _ + timestamp(13) + _ + uuid(8) = minimum 26 characters
        assertThat(walletId.length()).isGreaterThanOrEqualTo(26);
        assertThat(walletId.length()).isLessThan(50); // reasonable upper bound
    }

    @Test
    @DisplayName("Sequential ID generation should have increasing timestamps")
    void sequentialIdGenerationShouldHaveIncreasingTimestamps() {
        // Act
        String id1 = IdGenerator.generateWalletId();
        try {
            Thread.sleep(2); // Small delay to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String id2 = IdGenerator.generateWalletId();

        // Assert
        long timestamp1 = Long.parseLong(id1.split("_")[1]);
        long timestamp2 = Long.parseLong(id2.split("_")[1]);
        assertThat(timestamp2).isGreaterThanOrEqualTo(timestamp1);
    }
}
