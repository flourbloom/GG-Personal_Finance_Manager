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
import gitgud.pfm.Controllers.DashboardController;
import gitgud.pfm.Controllers.SidebarController;

import java.io.IOException;

/**
 * JavaFX App - Personal Finance Manager
 * Modern GUI with sidebar navigation (FXML-based)
 */
public class App extends Application {

    private BorderPane root;
    private SidebarController sidebarController;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        
        // Set up navigation callbacks for DashboardController
        DashboardController.setOnNavigateToGoals(this::showGoals);
        DashboardController.setOnNavigateToTransactions(this::showTransactions);
        
        // Load sidebar from FXML
        try {
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/sidebar.fxml"));
            VBox sidebar = sidebarLoader.load();
            sidebarController = sidebarLoader.getController();
            
            // Set navigation actions
            sidebarController.setOnDashboardClick(this::showDashboard);
            sidebarController.setOnTransactionsClick(this::showTransactions);
            sidebarController.setOnReportsClick(this::showReports);
            sidebarController.setOnGoalsClick(this::showGoals);
            sidebarController.setOnWalletsClick(this::showWallets);
            sidebarController.setOnBudgetClick(this::showBudget);
            
            root.setLeft(sidebar);
        } catch (IOException e) {
            System.err.println("Failed to load sidebar FXML: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Show dashboard by default
        showDashboard();
        
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
    
    public void showWallets() {
        Node view = loadFXML("wallets.fxml");
        if (view != null) {
            root.setCenter(view);
        }
        if (sidebarController != null) {
            sidebarController.setActiveItem("Wallets");
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