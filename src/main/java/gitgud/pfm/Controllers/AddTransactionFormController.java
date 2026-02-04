package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for the transaction form view when adding a transaction.
 * Uses FXML for layout definition.
 */
public class AddTransactionFormController implements Initializable {

    @FXML private VBox formContent;
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

    private Stage popupStage;
    private DataStore dataStore;
    private String categoryId;
    private String categoryName;
    private Category.Type categoryType;
    private Map<String, String> walletIdMap;
    private Consumer<Void> onTransactionAdded;
    private Runnable onBackPressed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        walletIdMap = new HashMap<>();
        
        // Set up date picker with current date
        datePicker.setValue(LocalDate.now());
        
        // Set up amount field validation
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldVal);
            }
        });
        
        // Populate wallet combo
        populateWallets();
        
        // Set up hover effects for back button
        setupBackButtonHover();
    }

    /**
     * Sets the popup stage reference.
     */
    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    /**
     * Sets the category information for the form.
     */
    public void setCategoryInfo(String name, AddTransactionCategoriesController.CategoryInfo info, Category.Type type) {
        this.categoryId = info.id;
        this.categoryName = name;
        this.categoryType = type;
        
        // Update UI elements
        categoryNameLabel.setText(name);
        categoryTypeLabel.setText(type == Category.Type.EXPENSE ? "Expense" : "Income");
        categoryTypeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + info.color + "; -fx-font-weight: 500;");
        
        iconLabel.setText(info.icon);
        iconCircle.setStyle("-fx-background-color: " + info.color + "20; -fx-background-radius: 24;");
        
        // Update save button color
        saveButton.setStyle(
            "-fx-background-color: " + info.color + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 24; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600; " +
            "-fx-cursor: hand;"
        );
    }

    /**
     * Sets the callback to be invoked when a transaction is successfully added.
     */
    public void setOnTransactionAdded(Consumer<Void> callback) {
        this.onTransactionAdded = callback;
    }

    /**
     * Sets the callback for when the back button is pressed.
     */
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    private void populateWallets() {
        List<Wallet> wallets = dataStore.getWallets();
        for (Wallet wallet : wallets) {
            String displayName = wallet.getName() + " ($" + String.format("%.2f", wallet.getBalance()) + ")";
            walletCombo.getItems().add(displayName);
            walletIdMap.put(displayName, wallet.getId());
        }
        if (!wallets.isEmpty()) {
            walletCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupBackButtonHover() {
        backButton.setOnMouseEntered(e -> backButton.setStyle(
            "-fx-background-color: #eff6ff; " +
            "-fx-text-fill: #2563eb; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 8 12; " +
            "-fx-background-radius: 8;"
        ));
        backButton.setOnMouseExited(e -> backButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3b82f6; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 8 0;"
        ));
    }

    @FXML
    private void handleBackButton() {
        if (onBackPressed != null) {
            onBackPressed.run();
        }
    }

    @FXML
    private void handleCancel() {
        if (popupStage != null) {
            popupStage.close();
        }
    }

    @FXML
    private void handleSave() {
        // Validate amount
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

        // Validate date
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert("Error", "Please select a date");
            return;
        }

        // Validate wallet
        String selectedWallet = walletCombo.getValue();
        if (selectedWallet == null || selectedWallet.isEmpty()) {
            showAlert("Error", "Please select a wallet");
            return;
        }

        String walletId = walletIdMap.get(selectedWallet);
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            description = categoryName;
        }

        // Create transaction
        String createTime = date.atTime(LocalTime.now())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        double incomeValue = categoryType == Category.Type.INCOME ? 1.0 : 0.0;
        Transaction transaction = new Transaction(
            categoryId,
            amount,
            description,
            incomeValue,
            walletId,
            createTime
        );

        // Update wallet balance
        Wallet wallet = dataStore.getWalletById(walletId);
        if (wallet != null) {
            double newBalance = wallet.getBalance() + (categoryType == Category.Type.INCOME ? amount : -amount);
            wallet.setBalance(newBalance);
            dataStore.updateWallet(wallet);
        }

        // Save transaction
        dataStore.addTransaction(transaction);

        // Show success message
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Success");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Transaction added successfully!");
        successAlert.showAndWait();

        // Notify callback
        if (onTransactionAdded != null) {
            onTransactionAdded.accept(null);
        }

        // Close popup
        if (popupStage != null) {
            popupStage.close();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
