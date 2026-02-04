package gitgud.pfm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import gitgud.pfm.GUI.Sidebar;
import gitgud.pfm.GUI.DashboardView;
import gitgud.pfm.GUI.TransactionsView;
import gitgud.pfm.GUI.ReportsView;
import gitgud.pfm.GUI.GoalsView;
import gitgud.pfm.GUI.AccountsView;

/**
 * JavaFX App - Personal Finance Manager
 * Modern GUI with sidebar navigation
 */
public class App extends Application {

    private BorderPane root;
    private Sidebar sidebar;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        
        // Create sidebar
        sidebar = new Sidebar(this);
        root.setLeft(sidebar);
        
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
    
    public void showDashboard() {
        root.setCenter(new DashboardView());
        sidebar.setActiveItem("Dashboard");
    }
    
    public void showTransactions() {
        root.setCenter(new TransactionsView());
        sidebar.setActiveItem("Transactions");
    }
    
    public void showReports() {
        root.setCenter(new ReportsView());
        sidebar.setActiveItem("Reports");
    }
    
    public void showGoals() {
        root.setCenter(new GoalsView());
        sidebar.setActiveItem("Goals");
    }
    
    public void showAccounts() {
        root.setCenter(new AccountsView());
        sidebar.setActiveItem("Accounts");
    }

    public static void main(String[] args) {
        launch(args);
    }

}