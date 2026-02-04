package gitgud.pfm.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * Explicit fields: id, name, description, type
     */
    @Override
    public void create(Category category) {
        String sql = "INSERT INTO Category (id, name, description, type) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getId());
            pstmt.setString(2, category.getName());
            pstmt.setString(3, category.getDescription());
            pstmt.setString(4, category.getType() != null ? category.getType().toString() : "EXPENSE");
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
        }
    }

    /**
     * Read a single category by id
     */
    @Override
    public Category read(String id) {
        String sql = "SELECT id, name, description, type FROM Category WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getString("id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    
                    String typeStr = rs.getString("type");
                    if (typeStr != null) {
                        try {
                            category.setType(Category.Type.valueOf(typeStr));
                        } catch (IllegalArgumentException e) {
                            category.setType(Category.Type.EXPENSE);
                        }
                    }
                    return category;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading category: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update an existing category
     */
    @Override
    public void update(Category category) {
        String sql = "UPDATE Category SET name = ?, description = ?, type = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setString(3, category.getType() != null ? category.getType().toString() : "EXPENSE");
            pstmt.setString(4, category.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
        }
    }
    
    /**
     * Delete a category by id
     */
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM Category WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    /**
     * Get all categories from the database
     * Falls back to default categories if database is empty
     */
    public List<Category> getAllCategories() {
        String sql = "SELECT id, name, description, type FROM Category ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getString("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                
                String typeStr = rs.getString("type");
                if (typeStr != null) {
                    try {
                        category.setType(Category.Type.valueOf(typeStr));
                    } catch (IllegalArgumentException e) {
                        category.setType(Category.Type.EXPENSE);
                    }
                }
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all categories: " + e.getMessage());
        }
        
        // If no categories in database, return the hardcoded defaults
        // This provides a fallback but the database should be seeded on init
        if (categories.isEmpty()) {
            return new ArrayList<>(getDefaultCategories());
        }
        
        return categories;
    }
    
    /**
     * Get categories by type (INCOME or EXPENSE)
     */
    public List<Category> getCategoriesByType(Category.Type type) {
        String sql = "SELECT id, name, description, type FROM Category WHERE type = ? ORDER BY name";
        List<Category> categories = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getString("id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    category.setType(type);
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading categories by type: " + e.getMessage());
        }
        
        return categories;
    }
    
    /**
     * Check if a category exists in the database
     */
    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM Category WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking category existence: " + e.getMessage());
        }
        return false;
    }
}
