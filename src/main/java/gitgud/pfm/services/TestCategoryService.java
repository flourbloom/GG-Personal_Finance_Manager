package gitgud.pfm.services;

import java.util.List;

import gitgud.pfm.Models.Category;

// TODO : This could be expanded with proper unit testing frameworks later
public class TestCategoryService {
    public static void main(String[] args) {
        CategoryService service = new CategoryService();

        System.out.println("Default categories:");
        List<Category> defaults = service.getDefaultCategories();
        for (Category c : defaults) {
            System.out.println(c);
        }
        
        System.out.println("\nAll categories (default + custom):");
        List<Category> all = service.getAllCategories();
        for (Category c : all) {
            System.out.println(c);
        }
    }
}
