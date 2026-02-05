package gitgud.pfm.Controllers;

import gitgud.pfm.services.AccountDataLoader;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class EditTransactionFormController implements Initializable {
    
    @FXML private StackPane iconCircle;
    @FXML private Label iconLabel;
    @FXML private Label categoryNameLabel;
    @FXML private Label categoryTypeLabel;
    @FXML private Button changeCategoryButton;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> walletCombo;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    private Stage dialogStage;
    private AccountDataLoader dataStore;
    private Transaction transaction;
    private BorderPane formRoot;
    private Runnable onSaveCallback;
    
    private String selectedCategoryId;
    private String selectedCategoryName;
    private String selectedCategoryColor;
    private Category.Type selectedCategoryType;
    private Map<String, String> walletIdMap = new HashMap<>();
    
    // Store original values for balance adjustment
    private String originalWalletId;
    private double originalAmount;
    private boolean originalWasIncome;
    
    // Category definitions (same as AddTransactionCategoryController)
    private static final Map<String, CategoryInfo> ALL_CATEGORIES = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_NAME_BY_ID = new HashMap<>();
    private static final Map<String, Category.Type> CATEGORY_TYPE_BY_ID = new HashMap<>();
    
    static {
        ALL_CATEGORIES.put("Food & Drinks", new CategoryInfo("1", "#ef4444", "🍔"));
        ALL_CATEGORIES.put("Transport", new CategoryInfo("2", "#f97316", "🚗"));
        ALL_CATEGORIES.put("Home Bills", new CategoryInfo("3", "#eab308", "🏠"));
        ALL_CATEGORIES.put("Self-care", new CategoryInfo("4", "#84cc16", "💆"));
        ALL_CATEGORIES.put("Shopping", new CategoryInfo("5", "#22c55e", "🛒"));
        ALL_CATEGORIES.put("Health", new CategoryInfo("6", "#14b8a6", "💊"));
        ALL_CATEGORIES.put("Salary", new CategoryInfo("7", "#10b981", "💰"));
        ALL_CATEGORIES.put("Investment", new CategoryInfo("8", "#6366f1", "📈"));
        ALL_CATEGORIES.put("Subscription", new CategoryInfo("9", "#06b6d4", "📱"));
        ALL_CATEGORIES.put("Entertainment & Sport", new CategoryInfo("10", "#3b82f6", "🎮"));
        ALL_CATEGORIES.put("Traveling", new CategoryInfo("11", "#8b5cf6", "✈️"));
        
        // Build reverse lookup maps
        for (Map.Entry<String, CategoryInfo> entry : ALL_CATEGORIES.entrySet()) {
            CATEGORY_NAME_BY_ID.put(entry.getValue().id, entry.getKey());
        }
        
        // Set category types
        CATEGORY_TYPE_BY_ID.put("1", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("2", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("3", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("4", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("5", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("6", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("7", Category.Type.INCOME);
        CATEGORY_TYPE_BY_ID.put("8", Category.Type.INCOME);
        CATEGORY_TYPE_BY_ID.put("9", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("10", Category.Type.EXPENSE);
        CATEGORY_TYPE_BY_ID.put("11", Category.Type.EXPENSE);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = AccountDataLoader.getInstance();
        
        // Validate amount field input
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldVal);
            }
        });
        
        // Button handlers
        cancelButton.setOnAction(e -> dialogStage.close());
        saveButton.setOnAction(e -> handleSaveTransaction());
        deleteButton.setOnAction(e -> handleDeleteTransaction());
        changeCategoryButton.setOnAction(e -> showCategorySelection());
        
        // Button hover effects
        changeCategoryButton.setOnMouseEntered(e -> 
            changeCategoryButton.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #3b82f6; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;"));
        changeCategoryButton.setOnMouseExited(e -> 
            changeCategoryButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600; -fx-cursor: hand;"));
        
        deleteButton.setOnMouseEntered(e -> 
            deleteButton.setStyle("-fx-background-color: #fecaca; -fx-text-fill: #dc2626; -fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> 
            deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 10; -fx-padding: 12 20; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand;"));
        
        loadWallets();
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setFormRoot(BorderPane root) {
        this.formRoot = root;
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    public void setTransaction(Transaction tx) {
        this.transaction = tx;
        
        // Store original values
        this.originalWalletId = tx.getWalletId();
        this.originalAmount = tx.getAmount();
        this.originalWasIncome = tx.getIncome() > 0;
        
        // Populate form fields
        descriptionField.setText(tx.getName());
        amountField.setText(String.valueOf(tx.getAmount()));
        
        // Select the wallet
        for (var entry : walletIdMap.entrySet()) {
            if (entry.getValue().equals(tx.getWalletId())) {
                walletCombo.setValue(entry.getKey());
                break;
            }
        }
        
        // Set category info
        String categoryId = tx.getCategoryId();
        if (categoryId != null && CATEGORY_NAME_BY_ID.containsKey(categoryId)) {
            selectedCategoryId = categoryId;
            selectedCategoryName = CATEGORY_NAME_BY_ID.get(categoryId);
            selectedCategoryType = CATEGORY_TYPE_BY_ID.get(categoryId);
            CategoryInfo info = ALL_CATEGORIES.get(selectedCategoryName);
            if (info != null) {
                selectedCategoryColor = info.color;
                updateCategoryUI(selectedCategoryName, info, selectedCategoryType);
            }
        } else {
            // Fallback for unknown categories
            selectedCategoryId = categoryId != null ? categoryId : "other";
            selectedCategoryName = categoryId != null ? categoryId : "Other";
            selectedCategoryType = tx.getIncome() > 0 ? Category.Type.INCOME : Category.Type.EXPENSE;
            selectedCategoryColor = tx.getIncome() > 0 ? "#10b981" : "#64748b";
            
            categoryNameLabel.setText(selectedCategoryName);
            categoryTypeLabel.setText(selectedCategoryType == Category.Type.INCOME ? "Income" : "Expense");
            categoryTypeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + selectedCategoryColor + "; -fx-font-weight: 500;");
            iconLabel.setText(tx.getIncome() > 0 ? "💰" : "📋");
            iconCircle.setStyle("-fx-background-color: " + selectedCategoryColor + "20; -fx-background-radius: 24;");
        }
        
        updateSaveButtonStyle();
    }
    
    public void updateCategory(String categoryName, CategoryInfo info, Category.Type type) {
        this.selectedCategoryId = info.id;
        this.selectedCategoryName = categoryName;
        this.selectedCategoryColor = info.color;
        this.selectedCategoryType = type;
        
        updateCategoryUI(categoryName, info, type);
        updateSaveButtonStyle();
        
        // Switch back to form view
        if (dialogStage != null && formRoot != null) {
            dialogStage.getScene().setRoot(formRoot);
        }
    }
    
    private void updateCategoryUI(String categoryName, CategoryInfo info, Category.Type type) {
        categoryNameLabel.setText(categoryName);
        categoryTypeLabel.setText(type == Category.Type.INCOME ? "Income" : "Expense");
        categoryTypeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + info.color + "; -fx-font-weight: 500;");
        iconLabel.setText(info.icon);
        iconCircle.setStyle("-fx-background-color: " + info.color + "20; -fx-background-radius: 24;");
    }
    
    private void updateSaveButtonStyle() {
        saveButton.setStyle(
            "-fx-background-color: " + selectedCategoryColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
    }
    
    private void loadWallets() {
        List<Wallet> wallets = dataStore.getWallets();
        walletIdMap.clear();
        walletCombo.getItems().clear();
        
        for (Wallet wallet : wallets) {
            String displayName = wallet.getName() + " ($" + String.format("%.2f", wallet.getBalance()) + ")";
            walletCombo.getItems().add(displayName);
            walletIdMap.put(displayName, wallet.getId());
        }
        
        if (!wallets.isEmpty()) {
            walletCombo.getSelectionModel().selectFirst();
        }
    }
    
    private void showCategorySelection() {
        try {
            FXMLLoader categoryLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/edit-transaction-category.fxml"));
            BorderPane categoryRoot = categoryLoader.load();
            EditTransactionCategoryController categoryController = categoryLoader.getController();
            categoryController.setDialogStage(dialogStage);
            categoryController.setFormController(this);
            categoryController.setFormRoot(formRoot);
            
            dialogStage.getScene().setRoot(categoryRoot);
        } catch (IOException e) {
            System.err.println("Error loading category selection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleSaveTransaction() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Error", "Please enter an amount");
            return;
        }
        
        double newAmount;
        try {
            newAmount = Double.parseDouble(amountText);
            if (newAmount <= 0) {
                showAlert("Error", "Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Invalid amount format");
            return;
        }
        
        String selectedWallet = walletCombo.getValue();
        if (selectedWallet == null || selectedWallet.isEmpty()) {
            showAlert("Error", "Please select a wallet");
            return;
        }
        
        String newWalletId = walletIdMap.get(selectedWallet);
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            description = selectedCategoryName;
        }
        
        boolean newIsIncome = selectedCategoryType == Category.Type.INCOME;
        
        // Revert old transaction from old wallet
        if (originalWalletId != null) {
            var oldWallet = dataStore.getWalletById(originalWalletId);
            if (oldWallet != null) {
                double revertAmount = originalWasIncome ? -originalAmount : originalAmount;
                oldWallet.setBalance(oldWallet.getBalance() + revertAmount);
                dataStore.updateWallet(oldWallet);
            }
        }
        
        // Apply new transaction to new wallet
        if (newWalletId != null) {
            var newWallet = dataStore.getWalletById(newWalletId);
            if (newWallet != null) {
                double applyAmount = newIsIncome ? newAmount : -newAmount;
                newWallet.setBalance(newWallet.getBalance() + applyAmount);
                dataStore.updateWallet(newWallet);
            }
        }
        
        // Update transaction
        transaction.setName(description);
        transaction.setAmount(newAmount);
        transaction.setIncome(newIsIncome ? 1.0 : 0.0);
        transaction.setCategoryId(selectedCategoryId);
        transaction.setWalletId(newWalletId);
        
        dataStore.updateTransaction(transaction);
        dataStore.notifyWalletRefresh();
        
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Transaction updated successfully!");
        successAlert.showAndWait();
        
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        dialogStage.close();
    }
    
    private void handleDeleteTransaction() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Transaction");
        confirm.setHeaderText("Are you sure you want to delete this transaction?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Revert transaction from wallet before deleting
                if (originalWalletId != null) {
                    var wallet = dataStore.getWalletById(originalWalletId);
                    if (wallet != null) {
                        double revertAmount = originalWasIncome ? -originalAmount : originalAmount;
                        wallet.setBalance(wallet.getBalance() + revertAmount);
                        dataStore.updateWallet(wallet);
                    }
                }
                
                dataStore.deleteTransaction(transaction.getId());
                dataStore.notifyWalletRefresh();
                
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                
                dialogStage.close();
            }
        });
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
