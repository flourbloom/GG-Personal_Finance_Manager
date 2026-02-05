package gitgud.pfm.Controllers;

import gitgud.pfm.services.AccountDataLoader;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import gitgud.pfm.utils.DateFormatUtil;

public class AddTransactionFormController implements Initializable {
    
    @FXML private Button backButton;
    @FXML private StackPane iconCircle;
    @FXML private Label iconLabel;
    @FXML private Label categoryNameLabel;
    @FXML private Label categoryTypeLabel;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> walletCombo;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    private Stage dialogStage;
    private AccountDataLoader dataStore;
    private BorderPane formRoot;
    
    private String selectedCategoryId;
    private String selectedCategoryName;
    private String selectedCategoryColor;
    private Category.Type selectedCategoryType;
    private Map<String, String> walletIdMap = new HashMap<>();
    
    // Goal contribution mode
    private boolean isGoalContribution = false;
    private Goal selectedGoal = null;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = AccountDataLoader.getInstance();
        datePicker.setValue(LocalDate.now());
        
        // Configure DatePicker for UK format
        DateFormatUtil.configureDatePickerUkFormat(datePicker);
        
        // Store reference to the form root
        if (cancelButton != null && cancelButton.getScene() != null) {
            formRoot = (BorderPane) cancelButton.getScene().getRoot();
        }
        
        // Load wallets
        loadWallets();
        
        // Validate amount field input
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldVal);
            }
        });
        
        // Button handlers
        cancelButton.setOnAction(e -> dialogStage.close());
        saveButton.setOnAction(e -> handleSaveTransaction());
        backButton.setOnAction(e -> showCategorySelection());
    }
    
    public void setFormRoot(BorderPane root) {
        this.formRoot = root;
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setCategory(String categoryName, CategoryInfo categoryInfo, Category.Type type) {
        this.selectedCategoryName = categoryName;
        this.selectedCategoryType = type;
        this.selectedCategoryId = categoryInfo.id;
        this.selectedCategoryColor = categoryInfo.color;
        this.isGoalContribution = false;
        this.selectedGoal = null;
        String icon = categoryInfo.icon;
        
        // Update UI
        categoryNameLabel.setText(categoryName);
        categoryTypeLabel.setText(type == Category.Type.INCOME ? "Income" : "Expense");
        categoryTypeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + selectedCategoryColor + "; -fx-font-weight: 500;");
        iconLabel.setText(icon);
        iconCircle.setStyle("-fx-background-color: " + selectedCategoryColor + "20; -fx-background-radius: 24;");
        saveButton.setStyle(
            "-fx-background-color: " + selectedCategoryColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        
        // Switch to the form view
        showFormView();
    }
    
    public void setGoalContribution(Goal goal, CategoryInfo categoryInfo) {
        this.selectedGoal = goal;
        this.isGoalContribution = true;
        this.selectedCategoryId = categoryInfo.id;
        this.selectedCategoryName = "Goals";
        this.selectedCategoryColor = categoryInfo.color;
        this.selectedCategoryType = Category.Type.EXPENSE;
        
        // Update UI for goal contribution
        categoryNameLabel.setText("Goal: " + goal.getName());
        categoryTypeLabel.setText(String.format("Target: $%.2f | Current: $%.2f", goal.getTarget(), goal.getBalance()));
        categoryTypeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + selectedCategoryColor + "; -fx-font-weight: 500;");
        iconLabel.setText(categoryInfo.icon);
        iconCircle.setStyle("-fx-background-color: " + selectedCategoryColor + "20; -fx-background-radius: 24;");
        saveButton.setText("Contribute");
        saveButton.setStyle(
            "-fx-background-color: " + selectedCategoryColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
        
        // Set default description
        descriptionField.setText("Contribution to " + goal.getName());
        
        showFormView();
    }
    
    private void showFormView() {
        if (dialogStage != null && formRoot != null) {
            dialogStage.getScene().setRoot(formRoot);
        }
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
    
    private void handleSaveTransaction() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Error", "Please enter an amount");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Error", "Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Invalid amount format");
            return;
        }
        
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert("Error", "Please select a date");
            return;
        }
        
        String selectedWallet = walletCombo.getValue();
        if (selectedWallet == null || selectedWallet.isEmpty()) {
            showAlert("Error", "Please select a wallet");
            return;
        }
        
        String walletId = walletIdMap.get(selectedWallet);
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            description = isGoalContribution && selectedGoal != null ? 
                "Contribution to " + selectedGoal.getName() : selectedCategoryName;
        }
        
        String createTime = date.atTime(java.time.LocalTime.now())
            .format(DateFormatUtil.ISO_DATETIME_FORMAT);
        
        // For goal contributions, it's always an expense (money going out of wallet into goal)
        double incomeValue = isGoalContribution ? 0.0 : (selectedCategoryType == Category.Type.INCOME ? 1.0 : 0.0);
        
        Transaction transaction = new Transaction(
            selectedCategoryId,
            amount,
            description,
            incomeValue,
            walletId,
            createTime
        );
        
        // Set goal ID if this is a goal contribution
        if (isGoalContribution && selectedGoal != null) {
            transaction.setGoalId(selectedGoal.getId());
        }
        
        // Update wallet balance
        Wallet wallet = dataStore.getWalletById(walletId);
        if (wallet != null) {
            double newBalance;
            if (isGoalContribution) {
                // Goal contribution - deduct from wallet
                newBalance = wallet.getBalance() - amount;
            } else {
                newBalance = wallet.getBalance() + (selectedCategoryType == Category.Type.INCOME ? amount : -amount);
            }
            wallet.setBalance(newBalance);
            dataStore.updateWallet(wallet);
            dataStore.notifyWalletRefresh();
        }
        
        // Update goal balance if this is a contribution
        if (isGoalContribution && selectedGoal != null) {
            selectedGoal.setBalance(selectedGoal.getBalance() + amount);
            dataStore.updateGoal(selectedGoal);
            dataStore.notifyGoalRefresh();
        }
        
        dataStore.addTransaction(transaction);
        
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        if (isGoalContribution && selectedGoal != null) {
            successAlert.setContentText(String.format(
                "Contributed $%.2f to %s!\n\nNew goal balance: $%.2f / $%.2f",
                amount, selectedGoal.getName(), selectedGoal.getBalance(), selectedGoal.getTarget()
            ));
        } else {
            successAlert.setContentText("Transaction added successfully!");
        }
        successAlert.showAndWait();
        
        dialogStage.close();
    }
    
    private void showCategorySelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add-transaction-category.fxml"));
            BorderPane root = loader.load();
            
            AddTransactionCategoryController controller = loader.getController();
            controller.setFormController(this);
            
            dialogStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load category selection");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
