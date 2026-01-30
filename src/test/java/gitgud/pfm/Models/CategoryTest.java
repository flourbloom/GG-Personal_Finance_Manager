package gitgud.pfm.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Category Model Tests")
class CategoryTest {

    @Test
    @DisplayName("Should create category with all parameters")
    void shouldCreateCategoryWithAllParameters() {
        // Arrange & Act
        Category category = new Category("CAT_001", "Food", "Food expenses", Category.Type.EXPENSE);

        // Assert
        assertThat(category).isNotNull();
        assertThat(category.getId()).isEqualTo("CAT_001");
        assertThat(category.getName()).isEqualTo("Food");
        assertThat(category.getDescription()).isEqualTo("Food expenses");
        assertThat(category.getType()).isEqualTo(Category.Type.EXPENSE);
    }

    @Test
    @DisplayName("Should create category with no-arg constructor")
    void shouldCreateCategoryWithNoArgConstructor() {
        // Arrange & Act
        Category category = new Category();

        // Assert
        assertThat(category).isNotNull();
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Arrange
        Category category = new Category();

        // Act
        category.setId("CAT_002");
        category.setName("Salary");
        category.setDescription("Monthly income");
        category.setType(Category.Type.INCOME);

        // Assert
        assertThat(category.getId()).isEqualTo("CAT_002");
        assertThat(category.getName()).isEqualTo("Salary");
        assertThat(category.getDescription()).isEqualTo("Monthly income");
        assertThat(category.getType()).isEqualTo(Category.Type.INCOME);
    }

    @ParameterizedTest
    @EnumSource(Category.Type.class)
    @DisplayName("Should support both INCOME and EXPENSE types")
    void shouldSupportBothTypes(Category.Type type) {
        // Arrange
        Category category = new Category();

        // Act
        category.setType(type);

        // Assert
        assertThat(category.getType()).isEqualTo(type);
    }

    @Test
    @DisplayName("Should create income category")
    void shouldCreateIncomeCategory() {
        // Arrange & Act
        Category category = new Category("1", "Salary", "Monthly salary", Category.Type.INCOME);

        // Assert
        assertThat(category.getType()).isEqualTo(Category.Type.INCOME);
    }

    @Test
    @DisplayName("Should create expense category")
    void shouldCreateExpenseCategory() {
        // Arrange & Act
        Category category = new Category("2", "Transport", "Travel expenses", Category.Type.EXPENSE);

        // Assert
        assertThat(category.getType()).isEqualTo(Category.Type.EXPENSE);
    }

    @Test
    @DisplayName("Should handle equals method correctly")
    void shouldHandleEqualsCorrectly() {
        // Arrange
        Category category1 = new Category("1", "Food", "Food expenses", Category.Type.EXPENSE);
        Category category2 = new Category("1", "Food", "Food expenses", Category.Type.EXPENSE);
        Category category3 = new Category("2", "Transport", "Transport expenses", Category.Type.EXPENSE);

        // Assert
        assertThat(category1).isEqualTo(category2);
        assertThat(category1).isNotEqualTo(category3);
    }

    @Test
    @DisplayName("Should handle hashCode correctly")
    void shouldHandleHashCodeCorrectly() {
        // Arrange
        Category category1 = new Category("1", "Food", "Food expenses", Category.Type.EXPENSE);
        Category category2 = new Category("1", "Food", "Food expenses", Category.Type.EXPENSE);

        // Assert
        assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        // Arrange
        Category category = new Category();

        // Act
        category.setId(null);
        category.setName(null);
        category.setDescription(null);
        category.setType(null);

        // Assert
        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
        assertThat(category.getType()).isNull();
    }

    @Test
    @DisplayName("Should handle toString method")
    void shouldHandleToString() {
        // Arrange
        Category category = new Category("1", "Food", "Daily food", Category.Type.EXPENSE);

        // Act
        String result = category.toString();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("Food");
    }
}
