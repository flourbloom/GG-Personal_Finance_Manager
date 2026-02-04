package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private AddTransactionCategoryController categoryController;
    private DataStore dataStore;
    private BorderPane formRoot;
    
    private String selectedCategoryId;
    private String selectedCategoryName;
    private String selectedCategoryColor;
    private Category.Type selectedCategoryType;
    private Map<String, String> walletIdMap = new HashMap<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        datePicker.setValue(LocalDate.now());
        
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
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setFormRoot(BorderPane root) {
        this.formRoot = root;
    }
    
    public void setCategoryController(AddTransactionCategoryController controller) {
        this.categoryController = controller;
    }
    
    public void setCategory(String categoryName, CategoryInfo categoryInfo, Category.Type type) {
        this.selectedCategoryName = categoryName;
        this.selectedCategoryType = type;
        this.selectedCategoryId = categoryInfo.id;
        this.selectedCategoryColor = categoryInfo.color;
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
            description = selectedCategoryName;
        }
        
        String createTime = date.atTime(java.time.LocalTime.now())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        double incomeValue = selectedCategoryType == Category.Type.INCOME ? 1.0 : 0.0;
        Transaction transaction = new Transaction(
            selectedCategoryId,
            amount,
            description,
            incomeValue,
            walletId,
            createTime
        );
        
        // Update wallet balance
        Wallet wallet = dataStore.getWalletById(walletId);
        if (wallet != null) {
            double newBalance = wallet.getBalance() + (selectedCategoryType == Category.Type.INCOME ? amount : -amount);
            wallet.setBalance(newBalance);
            dataStore.updateWallet(wallet);
        }
        
        dataStore.addTransaction(transaction);
        
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Transaction added successfully!");
        successAlert.showAndWait();
        
        dialogStage.close();
    }
    
    private void showCategorySelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add-transaction-category.fxml"));
            BorderPane root = loader.load();
            
            AddTransactionCategoryController controller = loader.getController();
            controller.setDialogStage(dialogStage);
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
