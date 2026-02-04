package gitgud.pfm.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import gitgud.pfm.Models.Category;
import gitgud.pfm.interfaces.CRUDInterface;

public class CategoryService implements CRUDInterface<Category> {
    private final Connection connection;

    public CategoryService() {
        this.connection = Database.getInstance().getConnection();
    }

    public List<Category> getDefaultCategories() {
        return List.of(
            new Category("1", "Food & Drinks", "Meals, groceries, and beverages", Category.Type.EXPENSE),
            new Category("2", "Transport", "Public transport, fuel, taxis, etc.", Category.Type.EXPENSE),
            new Category("3", "Home Bills", "Rent, electricity, water, gas, etc.", Category.Type.EXPENSE),
            new Category("4", "Self-care", "Personal care, beauty, spa, etc.", Category.Type.EXPENSE),
            new Category("5", "Shopping", "Clothes, gadgets, and other shopping", Category.Type.EXPENSE),
            new Category("6", "Health", "Medical, pharmacy, insurance", Category.Type.EXPENSE),
            new Category("7", "Salary", "Monthly salary income", Category.Type.INCOME),
            new Category("8", "Investment", "Investment returns, dividends, etc.", Category.Type.INCOME),
            new Category("9", "Subscription", "Streaming, software, memberships", Category.Type.EXPENSE),
            new Category("10", "Entertainment & Sport", "Movies, games, sports activities", Category.Type.EXPENSE),
            new Category("11", "Traveling", "Flights, hotels, vacation expenses", Category.Type.EXPENSE)
        );
    }

    /**
     * Create a new category in the database
     * Explicit fields: id, name, description, type, budget, isDefault
     */
    @Override
    public void create(Category category) {
        String sql = "INSERT INTO categories (id, name, description, type) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getDescription());
            pstmt.setString(4, category.getType().toString());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
        }
    }

    @Override
    public Category read(String id) {
        // Implementation omitted for brevity
        return null;
    }

    @Override
    public void update(Category category) {
        // Implementation omitted for brevity
    }
    
    @Override
    public void delete(String id) {
        // Implementation omitted for brevity
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(getDefaultCategories());
    }
}
