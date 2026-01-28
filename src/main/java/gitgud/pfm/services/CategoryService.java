package gitgud.pfm.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import gitgud.pfm.Models.Category;

public class CategoryService {
    private final List<Category> customCategories = new ArrayList<>();
    private final AtomicInteger customIdCounter = new AtomicInteger(1000);

    public List<Category> getDefaultCategories() {
        return List.of(
            new Category(1, "Food & Drinks", "Meals, groceries, and beverages", Category.Type.EXPENSE, 0.0, false),
            new Category(2, "Transport", "Public transport, fuel, taxis, etc.", Category.Type.EXPENSE, 0.0, false),
            new Category(3, "Home Bills", "Rent, electricity, water, gas, etc.", Category.Type.EXPENSE, 0.0, false),
            new Category(4, "Self-care", "Personal care, beauty, spa, etc.", Category.Type.EXPENSE, 0.0, false),
            new Category(5, "Shopping", "Clothes, gadgets, and other shopping", Category.Type.EXPENSE, 0.0, false),
            new Category(6, "Health", "Medical, pharmacy, insurance", Category.Type.EXPENSE, 0.0, false),
            new Category(7, "Salary", "Monthly salary income", Category.Type.INCOME, 0.0, false),
            new Category(8, "Investment", "Investment returns, dividends, etc.", Category.Type.INCOME, 0.0, false)
        );
    }

    public void addCustomCategory(Category category) {
        // Assign a unique id and mark as custom
        category.setId(customIdCounter.getAndIncrement());
        category.setCustom(true);
        customCategories.add(category);
    }

    public List<Category> getCustomCategories() {
        return Collections.unmodifiableList(customCategories);
    }

    public List<Category> getAllCategories() {
        List<Category> all = new ArrayList<>(getDefaultCategories());
        all.addAll(customCategories);
        return all;
    }
}
