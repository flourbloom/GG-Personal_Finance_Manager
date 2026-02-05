package gitgud.pfm.Controllers;

import gitgud.pfm.services.AccountDataLoader;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Goal;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class AddTransactionCategoryController implements Initializable {
    
    @FXML private FlowPane expenseCategoriesPane;
    @FXML private FlowPane incomeCategoriesPane;
    @FXML private VBox goalsSection;
    @FXML private FlowPane goalsCategoriesPane;
    
    private AddTransactionFormController formController;
    private AccountDataLoader dataStore;
    
    // Category definitions
    private static final Map<String, CategoryInfo> EXPENSE_CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, CategoryInfo> INCOME_CATEGORIES = new LinkedHashMap<>();
    public static final CategoryInfo GOALS_CATEGORY_INFO = new CategoryInfo("goals", "#8b5cf6", "🎯");
    
    static {
        EXPENSE_CATEGORIES.put("Food & Drinks", new CategoryInfo("1", "#ef4444", "🍔"));
        EXPENSE_CATEGORIES.put("Transport", new CategoryInfo("2", "#f97316", "🚗"));
        EXPENSE_CATEGORIES.put("Home Bills", new CategoryInfo("3", "#eab308", "🏠"));
        EXPENSE_CATEGORIES.put("Self-care", new CategoryInfo("4", "#84cc16", "💆"));
        EXPENSE_CATEGORIES.put("Shopping", new CategoryInfo("5", "#22c55e", "🛒"));
        EXPENSE_CATEGORIES.put("Health", new CategoryInfo("6", "#14b8a6", "💊"));
        EXPENSE_CATEGORIES.put("Subscription", new CategoryInfo("7", "#06b6d4", "📱"));
        EXPENSE_CATEGORIES.put("Entertainment & Sport", new CategoryInfo("8", "#3b82f6", "🎮"));
        EXPENSE_CATEGORIES.put("Traveling", new CategoryInfo("9", "#8b5cf6", "✈️"));
        EXPENSE_CATEGORIES.put("Goals", GOALS_CATEGORY_INFO);
        
        INCOME_CATEGORIES.put("Salary", new CategoryInfo("10", "#10b981", "💰"));
        INCOME_CATEGORIES.put("Investment", new CategoryInfo("11", "#6366f1", "📈"));
    }
    
    public void setFormController(AddTransactionFormController controller) {
        this.formController = controller;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = AccountDataLoader.getInstance();
        loadCategoryCards();
    }
    
    private void loadCategoryCards() {
        // Load expense categories
        for (Map.Entry<String, CategoryInfo> entry : EXPENSE_CATEGORIES.entrySet()) {
            VBox card;
            if (entry.getKey().equals("Goals")) {
                card = createGoalsCategoryCard(entry.getKey(), entry.getValue());
            } else {
                card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.EXPENSE);
            }
            expenseCategoriesPane.getChildren().add(card);
        }
        
        // Load income categories
        for (Map.Entry<String, CategoryInfo> entry : INCOME_CATEGORIES.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.INCOME);
            incomeCategoriesPane.getChildren().add(card);
        }
    }
    
    private VBox createGoalsCategoryCard(String categoryName, CategoryInfo info) {
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
        
        // Click to open goal selection dialog
        card.setOnMouseClicked(e -> showGoalSelectionDialog());
        
        return card;
    }
    
    private void showGoalSelectionDialog() {
        List<Goal> goals = dataStore.getGoals();
        
        if (goals.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Goals");
            alert.setHeaderText(null);
            alert.setContentText("You don't have any goals yet. Create a goal first in the Goals tab!");
            alert.showAndWait();
            return;
        }
        
        // Create goal selection dialog
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("🎯 Select Goal");
        
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(500);
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().setMinHeight(500);
        
        // Header
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(16, 20, 12, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-background-radius: 8 8 0 0;");
        
        Label headerTitle = new Label("Select a Goal to Contribute");
        headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;");
        
        Label headerSubtitle = new Label("Choose which goal you want to add funds to");
        headerSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);
        dialog.getDialogPane().setHeader(headerBox);
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        
        // Content - Goals list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
        
        VBox goalsListBox = new VBox(12);
        goalsListBox.setPadding(new Insets(16));
        goalsListBox.setStyle("-fx-background-color: white;");
        
        for (Goal goal : goals) {
            HBox goalCard = createGoalSelectionCard(goal, dialog);
            goalsListBox.getChildren().add(goalCard);
        }
        
        scrollPane.setContent(goalsListBox);
        dialog.getDialogPane().setContent(scrollPane);
        
        dialog.showAndWait().ifPresent(selectedGoal -> {
            // Show wallet selection with goal info
            showWalletSelectionForGoal(selectedGoal);
        });
    }
    
    private HBox createGoalSelectionCard(Goal goal, Dialog<Goal> dialog) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        
        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        String color = getProgressColor(progress);
        
        card.setStyle(
            "-fx-background-color: " + color + "10; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + color + "40; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1.5; " +
            "-fx-cursor: hand;"
        );
        
        // Icon
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(48, 48);
        iconCircle.setMinSize(48, 48);
        iconCircle.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 24;");
        Label iconLabel = new Label(progress >= 1.0 ? "✓" : "🎯");
        iconLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white;");
        iconCircle.getChildren().add(iconLabel);
        
        // Goal Info
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        Label nameLabel = new Label(goal.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        Label progressLabel = new Label(String.format("$%.2f / $%.2f (%.0f%%)", 
            goal.getBalance(), goal.getTarget(), progress * 100));
        progressLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        // Progress bar
        StackPane progressContainer = new StackPane();
        progressContainer.setPrefHeight(8);
        progressContainer.setMaxWidth(Double.MAX_VALUE);
        
        Region bgBar = new Region();
        bgBar.setPrefHeight(8);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        bgBar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4;");
        
        Region progressBar = new Region();
        progressBar.setPrefHeight(8);
        progressBar.setMaxWidth(Math.min(1.0, progress) * 200);
        progressBar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
        
        progressContainer.getChildren().addAll(bgBar, progressBar);
        StackPane.setAlignment(progressBar, Pos.CENTER_LEFT);
        
        infoBox.getChildren().addAll(nameLabel, progressLabel, progressContainer);
        
        // Select button
        Button selectBtn = new Button("Select");
        selectBtn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;"
        );
        
        selectBtn.setOnMouseEntered(e -> {
            selectBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-opacity: 0.85;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), selectBtn);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        selectBtn.setOnMouseExited(e -> {
            selectBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), selectBtn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        selectBtn.setOnAction(e -> {
            dialog.setResult(goal);
            dialog.close();
        });
        
        card.getChildren().addAll(iconCircle, infoBox, selectBtn);
        
        // Card hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: " + color + "20; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1.5; " +
            "-fx-cursor: hand;"
        ));
        
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: " + color + "10; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + color + "40; " +
            "-fx-border-radius: 12; " +
            "-fx-border-width: 1.5; " +
            "-fx-cursor: hand;"
        ));
        
        card.setOnMouseClicked(e -> {
            dialog.setResult(goal);
            dialog.close();
        });
        
        return card;
    }
    
    private String getProgressColor(double progress) {
        if (progress >= 1.0) return "#22c55e";
        if (progress >= 0.75) return "#3b82f6";
        if (progress >= 0.5) return "#f59e0b";
        if (progress >= 0.25) return "#8b5cf6";
        return "#64748b";
    }
    
    public void showWalletSelectionForGoal(Goal goal) {
        formController.setGoalContribution(goal, GOALS_CATEGORY_INFO);
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
            formController.setCategory(categoryName, info, type);
        });
        
        return card;
    }
}
