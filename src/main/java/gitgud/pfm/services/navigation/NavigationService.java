package gitgud.pfm.services.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Navigation Service - Handles navigation between views
 * Replaces static callback methods in controllers
 * Follows Single Responsibility Principle
 */
public class NavigationService {
    
    private Consumer<Node> onNavigate;
    private final Map<String, String> viewPaths = new HashMap<>();
    
    public NavigationService() {
        // Register view paths
        registerDefaultViews();
    }
    
    private void registerDefaultViews() {
        viewPaths.put("dashboard", "dashboard.fxml");
        viewPaths.put("transactions", "transactions.fxml");
        viewPaths.put("reports", "reports.fxml");
        viewPaths.put("goals", "goals.fxml");
        viewPaths.put("accounts", "accounts.fxml");
        viewPaths.put("budget", "budget.fxml");
        viewPaths.put("category", "category.fxml");
    }
    
    /**
     * Set the navigation handler (called by App.java)
     */
    public void setNavigationHandler(Consumer<Node> handler) {
        this.onNavigate = handler;
    }
    
    /**
     * Navigate to a view by name
     */
    public void navigateTo(String viewName) {
        if (onNavigate == null) {
            throw new IllegalStateException("Navigation handler not set. Call setNavigationHandler first.");
        }
        
        String fxmlPath = viewPaths.get(viewName.toLowerCase());
        if (fxmlPath == null) {
            throw new IllegalArgumentException("Unknown view: " + viewName);
        }
        
        Node view = loadFXML(fxmlPath);
        if (view != null) {
            onNavigate.accept(view);
        }
    }
    
    /**
     * Navigate to a view with custom FXML path
     */
    public void navigateToPath(String fxmlPath) {
        if (onNavigate == null) {
            throw new IllegalStateException("Navigation handler not set.");
        }
        
        Node view = loadFXML(fxmlPath);
        if (view != null) {
            onNavigate.accept(view);
        }
    }
    
    /**
     * Register a custom view path
     */
    public void registerView(String viewName, String fxmlPath) {
        viewPaths.put(viewName.toLowerCase(), fxmlPath);
    }
    
    /**
     * Load FXML file
     */
    private Node loadFXML(String fxmlFile) {
        try {
            // Ensure path starts with /gitgud/pfm/ if not absolute
            String fullPath = fxmlFile.startsWith("/") ? fxmlFile : "/gitgud/pfm/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fullPath));
            return loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if a view is registered
     */
    public boolean isViewRegistered(String viewName) {
        return viewPaths.containsKey(viewName.toLowerCase());
    }
}
