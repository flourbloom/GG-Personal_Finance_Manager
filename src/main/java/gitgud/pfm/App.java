package gitgud.pfm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import gitgud.pfm.Controllers.SidebarController;
import gitgud.pfm.infrastructure.ApplicationContext;
import gitgud.pfm.infrastructure.ServiceLocator;
import gitgud.pfm.services.navigation.NavigationService;

import java.io.IOException;

/**
 * JavaFX App - Personal Finance Manager
 * Modern GUI with sidebar navigation (FXML-based)
 * REFACTORED: Uses dependency injection and NavigationService
 */
public class App extends Application {

    private BorderPane root;
    private SidebarController sidebarController;
    private NavigationService navigationService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize application context and dependency injection
        ApplicationContext.initialize();
        
        root = new BorderPane();
        
        // Get NavigationService from ServiceLocator
        navigationService = ServiceLocator.get(NavigationService.class);
        navigationService.setNavigationHandler(this::navigateToView);
        
        // Load sidebar from FXML
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/sidebar.fxml"));
            VBox sidebar = sidebarLoader.load();
            sidebarController = sidebarLoader.getController();
            
            // Set navigation actions using NavigationService
            sidebarController.setOnDashboardClick(() -> navigationService.navigateTo("dashboard"));
            sidebarController.setOnTransactionsClick(() -> navigationService.navigateTo("transactions"));
            sidebarController.setOnReportsClick(() -> navigationService.navigateTo("reports"));
            sidebarController.setOnGoalsClick(() -> navigationService.navigateTo("goals"));
            sidebarController.setOnAccountsClick(() -> navigationService.navigateTo("accounts"));
            sidebarController.setOnBudgetClick(() -> navigationService.navigateTo("budget"));
            
            root.setLeft(sidebar);
        } catch (IOException e) {
            System.err.println("Failed to load sidebar FXML: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Show dashboard by default
        navigationService.navigateTo("dashboard");
        
        // Create scene with custom stylesheet
        Scene scene = new Scene(root, 1400, 900);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        } catch (Exception e) {
            // Stylesheet not found, continue without it
        }
        
        primaryStage.setTitle("Personal Finance Manager");
        primaryStage.setScene(scene);

        // Ensure the window fits the visual bounds (excludes taskbar)
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1400, visualBounds.getWidth());
        double height = Math.min(900, visualBounds.getHeight());
        primaryStage.setX(visualBounds.getMinX());
        primaryStage.setY(visualBounds.getMinY());
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);

        // Handle close request
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });

        primaryStage.show();
    }
    
    /**
     * Handle navigation - called by NavigationService
     */
    private void navigateToView(Node view) {
        if (view != null) {
            root.setCenter(view);
        }
    }
    
    /**
     * Legacy FXML loader (kept for backward compatibility)
     * NOTE: Use NavigationService.navigateTo() for new code
     */
    @Deprecated
    private Node loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gitgud/pfm/" + fxmlFile));
            return loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlFile + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Legacy navigation methods (kept for backward compatibility)
     * NOTE: Use NavigationService for new code
     */
    @Deprecated
    public void showDashboard() {
        Node view = loadFXML("dashboard.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Dashboard");
        }
    }
    
    public void showTransactions() {
        Node view = loadFXML("transactions.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Transactions");
        }
    }
    
    public void showReports() {
        Node view = loadFXML("reports.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Reports");
        }
    }
    
    public void showGoals() {
        Node view = loadFXML("goals.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Goals");
        }
    }
    
    public void showAccounts() {
        Node view = loadFXML("accounts.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Accounts");
        }
    }
    
    public void showBudget() {
        Node view = loadFXML("budget.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Budget");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}