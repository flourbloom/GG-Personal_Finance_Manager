package gitgud.pfm.Controllers;

import gitgud.pfm.Models.Category;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class EditTransactionCategoryController implements Initializable {
    
    @FXML private Button backButton;
    @FXML private FlowPane expenseCategoriesPane;
    @FXML private FlowPane incomeCategoriesPane;
    
    private Stage dialogStage;
    private EditTransactionFormController formController;
    private BorderPane formRoot;
    
    // Category definitions (same as AddTransactionCategoryController)
    private static final Map<String, CategoryInfo> EXPENSE_CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, CategoryInfo> INCOME_CATEGORIES = new LinkedHashMap<>();
    
    static {
        EXPENSE_CATEGORIES.put("Food & Drinks", new CategoryInfo("1", "#ef4444", "🍔"));
        EXPENSE_CATEGORIES.put("Transport", new CategoryInfo("2", "#f97316", "🚗"));
        EXPENSE_CATEGORIES.put("Home Bills", new CategoryInfo("3", "#eab308", "🏠"));
        EXPENSE_CATEGORIES.put("Self-care", new CategoryInfo("4", "#84cc16", "💆"));
        EXPENSE_CATEGORIES.put("Shopping", new CategoryInfo("5", "#22c55e", "🛒"));
        EXPENSE_CATEGORIES.put("Health", new CategoryInfo("6", "#14b8a6", "💊"));
        EXPENSE_CATEGORIES.put("Subscription", new CategoryInfo("9", "#06b6d4", "📱"));
        EXPENSE_CATEGORIES.put("Entertainment & Sport", new CategoryInfo("10", "#3b82f6", "🎮"));
        EXPENSE_CATEGORIES.put("Traveling", new CategoryInfo("11", "#8b5cf6", "✈️"));
        
        INCOME_CATEGORIES.put("Salary", new CategoryInfo("7", "#10b981", "💰"));
        INCOME_CATEGORIES.put("Investment", new CategoryInfo("8", "#6366f1", "📈"));
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setFormController(EditTransactionFormController controller) {
        this.formController = controller;
    }
    
    public void setFormRoot(BorderPane root) {
        this.formRoot = root;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategoryCards();
        
        backButton.setOnAction(e -> goBackToForm());
        
        // Hover effects for back button
        backButton.setOnMouseEntered(ev -> 
            backButton.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #3b82f6; -fx-font-size: 14px; -fx-font-weight: 500; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;"));
        backButton.setOnMouseExited(ev -> 
            backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-font-size: 14px; -fx-font-weight: 500; -fx-cursor: hand; -fx-padding: 8 0;"));
    }
    
    private void goBackToForm() {
        if (dialogStage != null && formRoot != null) {
            dialogStage.getScene().setRoot(formRoot);
        }
    }
    
    private void loadCategoryCards() {
        // Load expense categories
        for (Map.Entry<String, CategoryInfo> entry : EXPENSE_CATEGORIES.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.EXPENSE);
            expenseCategoriesPane.getChildren().add(card);
        }
        
        // Load income categories
        for (Map.Entry<String, CategoryInfo> entry : INCOME_CATEGORIES.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.INCOME);
            incomeCategoriesPane.getChildren().add(card);
        }
    }
    
    private VBox createCategoryCard(String categoryName, CategoryInfo info, Category.Type type) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(140);
        card.setPrefHeight(120);
        
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-color: " + info.color + "40; " +
            "-fx-border-radius: 16; " +
            "-fx-border-width: 2; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(48, 48);
        iconCircle.setStyle("-fx-background-color: " + info.color + "20; -fx-background-radius: 24;");
        Label iconLabel = new Label(info.icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconCircle.getChildren().add(iconLabel);
        
        Label nameLabel = new Label(categoryName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(120);
        
        card.getChildren().addAll(iconCircle, nameLabel);
        
        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + info.color + "10; " +
            "-fx-background-radius: 16; " +
            "-fx-border-color: " + info.color + "; " +
            "-fx-border-radius: 16; " +
            "-fx-border-width: 2; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, " + info.color + "40, 12, 0, 0, 4);"
        ));
        
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 16; " +
            "-fx-border-color: " + info.color + "40; " +
            "-fx-border-radius: 16; " +
            "-fx-border-width: 2; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        ));
        
        card.setOnMouseClicked(e -> {
            formController.updateCategory(categoryName, info, type);
        });
        
        return card;
    }
}
