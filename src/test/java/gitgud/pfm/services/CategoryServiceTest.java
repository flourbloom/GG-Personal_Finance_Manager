package gitgud.pfm.services;

import gitgud.pfm.Models.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService();
    }

    @Test
    @DisplayName("Should return default categories")
    void shouldReturnDefaultCategories() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories).isNotEmpty();
        assertThat(categories).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("Default categories should contain income and expense types")
    void defaultCategoriesShouldContainIncomeAndExpenseTypes() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();

        // Assert
        boolean hasIncome = categories.stream()
            .anyMatch(cat -> cat.getType() == Category.Type.INCOME);
        boolean hasExpense = categories.stream()
            .anyMatch(cat -> cat.getType() == Category.Type.EXPENSE);

        assertThat(hasIncome).isTrue();
        assertThat(hasExpense).isTrue();
    }

    @Test
    @DisplayName("Default categories should have valid IDs")
    void defaultCategoriesShouldHaveValidIds() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();

        // Assert
        categories.forEach(category -> {
            assertThat(category.getId()).isNotNull();
            assertThat(category.getId()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("Default categories should have names")
    void defaultCategoriesShouldHaveNames() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();

        // Assert
        categories.forEach(category -> {
            assertThat(category.getName()).isNotNull();
            assertThat(category.getName()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("Default categories should have descriptions")
    void defaultCategoriesShouldHaveDescriptions() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();

        // Assert
        categories.forEach(category -> {
            assertThat(category.getDescription()).isNotNull();
            assertThat(category.getDescription()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("Should return expected default category names")
    void shouldReturnExpectedDefaultCategoryNames() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();
        List<String> categoryNames = categories.stream()
            .map(Category::getName)
            .toList();

        // Assert
        assertThat(categoryNames).contains(
            "Food & Drinks",
            "Transport",
            "Home Bills",
            "Self-care",
            "Shopping",
            "Health",
            "Salary",
            "Investment"
        );
    }

    @Test
    @DisplayName("GetAllCategories should return default categories")
    void getAllCategoriesShouldReturnDefaultCategories() {
        // Act
        List<Category> categories = categoryService.getAllCategories();

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isEqualTo(categoryService.getDefaultCategories().size());
    }

    @Test
    @DisplayName("Default categories should be immutable")
    void defaultCategoriesShouldBeImmutable() {
        // Act
        List<Category> categories1 = categoryService.getDefaultCategories();
        List<Category> categories2 = categoryService.getDefaultCategories();

        // Assert - Should return same content each time
        assertThat(categories1.size()).isEqualTo(categories2.size());
        for (int i = 0; i < categories1.size(); i++) {
            assertThat(categories1.get(i).getId()).isEqualTo(categories2.get(i).getId());
            assertThat(categories1.get(i).getName()).isEqualTo(categories2.get(i).getName());
        }
    }

    @Test
    @DisplayName("Should handle food category correctly")
    void shouldHandleFoodCategoryCorrectly() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();
        Category foodCategory = categories.stream()
            .filter(cat -> "Food & Drinks".equals(cat.getName()))
            .findFirst()
            .orElse(null);

        // Assert
        assertThat(foodCategory).isNotNull();
        assertThat(foodCategory.getType()).isEqualTo(Category.Type.EXPENSE);
        assertThat(foodCategory.getDescription()).contains("Meals");
    }

    @Test
    @DisplayName("Should handle salary category correctly")
    void shouldHandleSalaryCategoryCorrectly() {
        // Act
        List<Category> categories = categoryService.getDefaultCategories();
        Category salaryCategory = categories.stream()
            .filter(cat -> "Salary".equals(cat.getName()))
            .findFirst()
            .orElse(null);

        // Assert
        assertThat(salaryCategory).isNotNull();
        assertThat(salaryCategory.getType()).isEqualTo(Category.Type.INCOME);
        assertThat(salaryCategory.getDescription()).contains("salary");
    }
}
