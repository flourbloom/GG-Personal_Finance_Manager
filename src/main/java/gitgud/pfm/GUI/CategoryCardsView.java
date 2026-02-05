package gitgud.pfm.GUI;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * CategoryCardsView - Displays clickable category cards for expense and income tracking
 * Each card has a unique color and opens a transaction form when clicked
 */
public class CategoryCardsView extends ScrollPane {
    
    private DataStore dataStore;
    private VBox mainContent;
    
    // Category definitions with colors (name -> CategoryInfo with id, color, icon)
    private static final Map<String, CategoryInfo> EXPENSE_CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, CategoryInfo> INCOME_CATEGORIES = new LinkedHashMap<>();
    
    static {
        // Expense categories with IDs matching CategoryService default categories
        EXPENSE_CATEGORIES.put("Food & Drinks", new CategoryInfo("1", "#ef4444", "🍔"));
        EXPENSE_CATEGORIES.put("Transport", new CategoryInfo("2", "#f97316", "🚗"));
        EXPENSE_CATEGORIES.put("Home Bills", new CategoryInfo("3", "#eab308", "🏠"));
        EXPENSE_CATEGORIES.put("Self-care", new CategoryInfo("4", "#84cc16", "💆"));
        EXPENSE_CATEGORIES.put("Shopping", new CategoryInfo("5", "#22c55e", "🛒"));
        EXPENSE_CATEGORIES.put("Health", new CategoryInfo("6", "#14b8a6", "💊"));
        EXPENSE_CATEGORIES.put("Subscription", new CategoryInfo("9", "#06b6d4", "📱"));
        EXPENSE_CATEGORIES.put("Entertainment & Sport", new CategoryInfo("10", "#3b82f6", "🎮"));
        EXPENSE_CATEGORIES.put("Traveling", new CategoryInfo("11", "#8b5cf6", "✈️"));
        
        // Income categories with IDs matching CategoryService default categories
        INCOME_CATEGORIES.put("Salary", new CategoryInfo("7", "#10b981", "💰"));
        INCOME_CATEGORIES.put("Investment", new CategoryInfo("8", "#6366f1", "📈"));
    }
    
    public CategoryCardsView() {
        dataStore = DataStore.getInstance();
        
        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        
        // Header
        Label header = new Label("Add Transaction");
        header.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Select a category to add a new transaction");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        VBox headerBox = new VBox(8);
        headerBox.getChildren().addAll(header, subtitle);
        
        // Expense Categories Section
        VBox expenseSection = createCategorySection("Expenses", EXPENSE_CATEGORIES, Category.Type.EXPENSE);
        
        // Income Categories Section
        VBox incomeSection = createCategorySection("Income", INCOME_CATEGORIES, Category.Type.INCOME);
        
        mainContent.getChildren().addAll(headerBox, expenseSection, incomeSection);
        
        setContent(mainContent);
        setFitToWidth(true);
        setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
    }
    
    private VBox createCategorySection(String title, Map<String, CategoryInfo> categories, Category.Type type) {
        VBox section = new VBox(16);
        
        // Section header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Region iconView = new Region();
        iconView.setPrefSize(18, 18);
        String iconColor = type == Category.Type.EXPENSE ? "#ef4444" : "#10b981";
        iconView.setStyle("-fx-background-color: " + iconColor + "; -fx-background-radius: 4;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        headerBox.getChildren().addAll(iconView, titleLabel);
        
        // Cards grid
        FlowPane cardsGrid = new FlowPane();
        cardsGrid.setHgap(16);
        cardsGrid.setVgap(16);
        cardsGrid.setPadding(new Insets(8, 0, 0, 0));
        
        for (Map.Entry<String, CategoryInfo> entry : categories.entrySet()) {
            VBox card = createCategoryCard(entry.getKey(), entry.getValue(), type);
            cardsGrid.getChildren().add(card);
        }
        
        section.getChildren().addAll(headerBox, cardsGrid);
        return section;
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
        
        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(48, 48);
        iconCircle.setStyle(
            "-fx-background-color: " + info.color + "20; " +
            "-fx-background-radius: 24;"
        );
        
        Label iconLabel = new Label(info.icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconCircle.getChildren().add(iconLabel);
        
        // Category name
        Label nameLabel = new Label(categoryName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(120);
        
        card.getChildren().addAll(iconCircle, nameLabel);
        
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + info.color + "10; " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: " + info.color + "; " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 2; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + info.color + "40, 12, 0, 0, 4);"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-border-color: " + info.color + "40; " +
                "-fx-border-radius: 16; " +
                "-fx-border-width: 2; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
            );
        });
        
        // Click handler - open transaction form
        card.setOnMouseClicked(e -> showTransactionForm(categoryName, info, type));
        
        return card;
    }
    
    private void showTransactionForm(String categoryName, CategoryInfo info, Category.Type type) {
        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Add " + (type == Category.Type.EXPENSE ? "Expense" : "Income"));
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setStyle("-fx-background-color: white;");
        dialogContent.setPrefWidth(400);
        
        // Header with category info
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(48, 48);
        iconCircle.setStyle(
            "-fx-background-color: " + info.color + "20; " +
            "-fx-background-radius: 24;"
        );
        Label iconLabel = new Label(info.icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconCircle.getChildren().add(iconLabel);
        
        VBox headerText = new VBox(4);
        Label titleLabel = new Label(categoryName);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        Label typeLabel = new Label(type == Category.Type.EXPENSE ? "Expense" : "Income");
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + info.color + "; -fx-font-weight: 500;");
        headerText.getChildren().addAll(titleLabel, typeLabel);
        
        header.getChildren().addAll(iconCircle, headerText);
        
        // Form fields
        VBox formFields = new VBox(16);
        
        // Amount field
        VBox amountBox = createFormField("Amount");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount...");
        amountField.setStyle(
            "-fx-background-color: #f8fafc; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 10; " +
            "-fx-padding: 12 16; " +
            "-fx-font-size: 14px;"
        );
        // Only allow numbers and decimal point
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldVal);
            }
        });
        amountBox.getChildren().add(amountField);
        
        // Date field with DatePicker
        VBox dateBox = createFormField("Date");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle(
            "-fx-background-color: #f8fafc; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 14px;"
        );
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("Select date...");
        dateBox.getChildren().add(datePicker);
        
        // Description/Name field
        VBox descBox = createFormField("Description");
        TextField descField = new TextField();
        descField.setPromptText("Enter description (optional)...");
        descField.setStyle(
            "-fx-background-color: #f8fafc; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 10; " +
            "-fx-padding: 12 16; " +
            "-fx-font-size: 14px;"
        );
        descBox.getChildren().add(descField);
        
        // Wallet selector
        VBox walletBox = createFormField("Wallet");
        ComboBox<String> walletCombo = new ComboBox<>();
        walletCombo.setPromptText("Select wallet...");
        walletCombo.setMaxWidth(Double.MAX_VALUE);
        walletCombo.setStyle(
            "-fx-background-color: #f8fafc; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 14px;"
        );
        
        // Load wallets
        List<Wallet> wallets = dataStore.getWallets();
        Map<String, String> walletIdMap = new HashMap<>();
        for (Wallet wallet : wallets) {
            String displayName = wallet.getName() + " ($" + String.format("%.2f", wallet.getBalance()) + ")";
            walletCombo.getItems().add(displayName);
            walletIdMap.put(displayName, wallet.getId());
        }
        if (!wallets.isEmpty()) {
            walletCombo.getSelectionModel().selectFirst();
        }
        walletBox.getChildren().add(walletCombo);
        
        formFields.getChildren().addAll(amountBox, dateBox, descBox, walletBox);
        
        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #f1f5f9; " +
            "-fx-text-fill: #64748b; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #e2e8f0; " +
            "-fx-text-fill: #475569; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        ));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: #f1f5f9; " +
            "-fx-text-fill: #64748b; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        ));
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        Button saveBtn = new Button("Save Transaction");
        saveBtn.setStyle(
            "-fx-background-color: " + info.color + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
            "-fx-background-color: derive(" + info.color + ", -10%); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        ));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
            "-fx-background-color: " + info.color + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        ));
        
        saveBtn.setOnAction(e -> {
            // Validate form
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                showError("Please enter an amount");
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showError("Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Invalid amount format");
                return;
            }
            
            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Please select a date");
                return;
            }
            
            String selectedWallet = walletCombo.getValue();
            if (selectedWallet == null || selectedWallet.isEmpty()) {
                showError("Please select a wallet");
                return;
            }
            
            // Get wallet ID
            String walletId = walletIdMap.get(selectedWallet);
            
            // Create description
            String description = descField.getText().trim();
            if (description.isEmpty()) {
                description = categoryName;
            }
            
            // Format date with current time
            String createTime = date.atTime(java.time.LocalTime.now())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Create transaction using category ID
            double incomeValue = type == Category.Type.INCOME ? 1.0 : 0.0;
            Transaction transaction = new Transaction(
                info.id, // Use the category ID for proper lookup
                amount,
                description,
                incomeValue,
                walletId,
                createTime
            );
            
            // Update wallet balance
            Wallet wallet = dataStore.getWalletById(walletId);
            if (wallet != null) {
                double newBalance = wallet.getBalance() + (type == Category.Type.INCOME ? amount : -amount);
                wallet.setBalance(newBalance);
                dataStore.updateWallet(wallet);
            }
            
            // Save transaction
            dataStore.addTransaction(transaction);
            
            // Show success message
            showSuccess("Transaction added successfully!");
            
            dialogStage.close();
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        dialogContent.getChildren().addAll(header, new Separator(), formFields, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent);
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }
    
    private VBox createFormField(String label) {
        VBox box = new VBox(8);
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        box.getChildren().add(fieldLabel);
        return box;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class to hold category info
    private static class CategoryInfo {
        String id;
        String color;
        String icon;
        
        CategoryInfo(String id, String color, String icon) {
            this.id = id;
            this.color = color;
            this.icon = icon;
        }
    }
}
