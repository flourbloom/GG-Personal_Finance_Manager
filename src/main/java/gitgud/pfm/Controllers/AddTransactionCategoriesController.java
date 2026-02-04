package gitgud.pfm.Controllers;

import gitgud.pfm.Models.Category;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for the category selection view when adding a transaction.
 * Uses FXML for layout definition.
 */
public class AddTransactionCategoriesController implements Initializable {

    @FXML private VBox mainContent;
    @FXML private FlowPane expenseCategoriesPane;
    @FXML private FlowPane incomeCategoriesPane;

    private Stage popupStage;
    private Consumer<Void> onTransactionAdded;

    // Category info helper class
    public static class CategoryInfo {
        public final String id;
        public final String color;
        public final String icon;

        public CategoryInfo(String id, String color, String icon) {
            this.id = id;
            this.color = color;
            this.icon = icon;
        }
    }

    // Category definitions
    public static final Map<String, CategoryInfo> EXPENSE_CATEGORIES = new LinkedHashMap<>();
    public static final Map<String, CategoryInfo> INCOME_CATEGORIES = new LinkedHashMap<>();

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateCategories();
    }

    /**
     * Sets the popup stage reference for navigation.
     */
    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    /**
     * Sets the callback to be invoked when a transaction is successfully added.
     */
    public void setOnTransactionAdded(Consumer<Void> callback) {
        this.onTransactionAdded = callback;
    }

    private void populateCategories() {
        // Populate expense categories
        for (Map.Entry<String, CategoryInfo> entry : EXPENSE_CATEGORIES.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.EXPENSE);
            expenseCategoriesPane.getChildren().add(card);
        }

        // Populate income categories
        for (Map.Entry<String, CategoryInfo> entry : INCOME_CATEGORIES.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), Category.Type.INCOME);
            incomeCategoriesPane.getChildren().add(card);
        }
    }

    private VBox createCategoryCard(String categoryName, CategoryInfo info, Category.Type type) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setPrefHeight(120);
        card.setStyle(getCardDefaultStyle(info.color));

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
        card.setOnMouseEntered(e -> card.setStyle(getCardHoverStyle(info.color)));
        card.setOnMouseExited(e -> card.setStyle(getCardDefaultStyle(info.color)));

        // Click handler - navigate to form
        card.setOnMouseClicked(e -> navigateToForm(categoryName, info, type));

        return card;
    }

    private String getCardDefaultStyle(String color) {
        return "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: " + color + "40; " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 2; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    private String getCardHoverStyle(String color) {
        return "-fx-background-color: " + color + "10; " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 2; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, " + color + "40, 12, 0, 0, 4);";
    }

    private void navigateToForm(String categoryName, CategoryInfo info, Category.Type type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add_transaction_form.fxml"));
            Parent formView = loader.load();

            AddTransactionFormController formController = loader.getController();
            formController.setPopupStage(popupStage);
            formController.setCategoryInfo(categoryName, info, type);
            formController.setOnTransactionAdded(onTransactionAdded);
            formController.setOnBackPressed(() -> {
                // Navigate back to categories view
                try {
                    FXMLLoader categoriesLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add_transaction_categories.fxml"));
                    Parent categoriesView = categoriesLoader.load();
                    
                    AddTransactionCategoriesController categoriesController = categoriesLoader.getController();
                    categoriesController.setPopupStage(popupStage);
                    categoriesController.setOnTransactionAdded(onTransactionAdded);
                    
                    popupStage.getScene().setRoot(categoriesView);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            popupStage.getScene().setRoot(formView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
