package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.Models.Wallet;
import gitgud.pfm.services.CategoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionController implements Initializable {
    
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> nameColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> accountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> amountColumn;
    @FXML private TableColumn<Transaction, Void> actionsColumn;
    @FXML private Label balanceLabel;
    @FXML private Button addButton;
    
    private DataStore dataStore;
    private CategoryService categoryService;
    private ObservableList<Transaction> transactionData;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        categoryService = new CategoryService();
        transactionData = FXCollections.observableArrayList();
        
        setupTableColumns();
        transactionTable.setItems(transactionData);
        
        loadTransactions();
    }
    
    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getCreateTime();
            return new SimpleStringProperty(date != null ? date : "");
        });
        
        // Name column
        nameColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getName();
            return new SimpleStringProperty(name != null ? name : "");
        });
        
        // Category column
        categoryColumn.setCellValueFactory(cellData -> {
            String categoryId = cellData.getValue().getCategoryId();
            String categoryName = getCategoryName(categoryId);
            return new SimpleStringProperty(categoryName);
        });
        
        // Account column
        accountColumn.setCellValueFactory(cellData -> {
            String walletId = cellData.getValue().getWalletId();
            String walletName = getWalletName(walletId);
            return new SimpleStringProperty(walletName);
        });
        
        // Type column
        typeColumn.setCellValueFactory(cellData -> {
            double income = cellData.getValue().getIncome();
            return new SimpleStringProperty(income == 1.0 ? "Income" : "Expense");
        });
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Income")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Amount column
        amountColumn.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            return new SimpleStringProperty(String.format("$%.2f", amount));
        });
        amountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<Transaction, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                               "-fx-font-size: 12px; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                                  "-fx-font-size: 12px; -fx-padding: 5 10; -fx-cursor: hand;");
                pane.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(e -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    handleEditTransaction(transaction);
                });
                
                deleteBtn.setOnAction(e -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    handleDeleteTransaction(transaction);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }
    
    @FXML
    private void handleAddTransaction() {
        try {
            // Load the FXML for category selection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add_transaction_categories.fxml"));
            Parent categoriesView = loader.load();
            
            // Create popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add Transaction");
            
            // Configure the controller
            AddTransactionCategoriesController controller = loader.getController();
            controller.setPopupStage(popupStage);
            controller.setOnTransactionAdded(v -> {
                loadTransactions();
                updateBalanceDisplay();
            });
            
            Scene scene = new Scene(categoriesView, 700, 600);
            popupStage.setScene(scene);
            popupStage.showAndWait();
            
            // Refresh transactions after popup closes
            loadTransactions();
            updateBalanceDisplay();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the add transaction dialog");
        }
    }
    
    private void handleEditTransaction(Transaction transaction) {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Edit transaction details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(transaction.getName());
        TextField amountField = new TextField(String.valueOf(transaction.getAmount()));
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Income", "Expense");
        typeCombo.setValue(transaction.getIncome() == 1.0 ? "Income" : "Expense");
        
        ComboBox<Category> categoryCombo = new ComboBox<>();
        List<Category> allCategories = categoryService.getAllCategories();
        
        // Helper method to filter categories by type
        Runnable updateCategoryCombo = () -> {
            String selectedType = typeCombo.getValue();
            Category.Type categoryType = selectedType.equals("Income") ? Category.Type.INCOME : Category.Type.EXPENSE;
            Category currentSelection = categoryCombo.getValue();
            categoryCombo.getItems().clear();
            for (Category cat : allCategories) {
                if (cat.getType() == categoryType) {
                    categoryCombo.getItems().add(cat);
                }
            }
            // Try to keep current selection if still valid
            if (currentSelection != null && categoryCombo.getItems().contains(currentSelection)) {
                categoryCombo.setValue(currentSelection);
            } else if (!categoryCombo.getItems().isEmpty()) {
                categoryCombo.setValue(categoryCombo.getItems().get(0));
            }
        };
        
        // Set up listener for type changes
        typeCombo.setOnAction(e -> updateCategoryCombo.run());
        
        categoryCombo.setConverter(new javafx.util.StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null ? category.getName() : "";
            }
            @Override
            public Category fromString(String string) {
                return null;
            }
        });
        
        // Initialize categories based on transaction's type
        updateCategoryCombo.run();
        
        // Set the current category
        for (Category cat : categoryCombo.getItems()) {
            if (cat.getId().equals(transaction.getCategoryId())) {
                categoryCombo.setValue(cat);
                break;
            }
        }
        
        ComboBox<Wallet> accountCombo = new ComboBox<>();
        List<Wallet> wallets = dataStore.getWallets();
        accountCombo.getItems().addAll(wallets);
        accountCombo.setConverter(new javafx.util.StringConverter<Wallet>() {
            @Override
            public String toString(Wallet wallet) {
                return wallet != null ? wallet.getName() : "";
            }
            @Override
            public Wallet fromString(String string) {
                return null;
            }
        });
        for (Wallet wallet : wallets) {
            if (wallet.getId().equals(transaction.getWalletId())) {
                accountCombo.setValue(wallet);
                break;
            }
        }
        
        DatePicker datePicker = new DatePicker();
        try {
            String dateStr = transaction.getCreateTime().split(" ")[0];
            datePicker.setValue(java.time.LocalDate.parse(dateStr));
        } catch (Exception e) {
            datePicker.setValue(java.time.LocalDate.now());
        }
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(new Label("Account:"), 0, 4);
        grid.add(accountCombo, 1, 4);
        grid.add(new Label("Date:"), 0, 5);
        grid.add(datePicker, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Revert old balance
                    updateWalletBalance(transaction.getWalletId(), transaction.getAmount(), 
                                      transaction.getIncome() != 1.0);
                    
                    transaction.setName(nameField.getText());
                    transaction.setAmount(Double.parseDouble(amountField.getText()));
                    transaction.setIncome(typeCombo.getValue().equals("Income") ? 1.0 : 0.0);
                    transaction.setCategoryId(categoryCombo.getValue().getId());
                    transaction.setWalletId(accountCombo.getValue().getId());
                    transaction.setCreateTime(datePicker.getValue().atStartOfDay()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    return transaction;
                } catch (Exception e) {
                    showAlert("Invalid input", "Please check your input values.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(updatedTransaction -> {
            // Apply new balance
            updateWalletBalance(updatedTransaction.getWalletId(), updatedTransaction.getAmount(), 
                              updatedTransaction.getIncome() == 1.0);
            
            dataStore.updateTransaction(updatedTransaction);
            loadTransactions();
            updateBalanceDisplay();
        });
    }
    
    private void handleDeleteTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setHeaderText("Are you sure you want to delete this transaction?");
        alert.setContentText(transaction.getName() + " - $" + transaction.getAmount());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Revert wallet balance
            updateWalletBalance(transaction.getWalletId(), transaction.getAmount(), 
                              transaction.getIncome() != 1.0);
            
            dataStore.deleteTransaction(transaction.getId());
            loadTransactions();
            updateBalanceDisplay();
        }
    }
    
    private void updateWalletBalance(String walletId, double amount, boolean isIncome) {
        Wallet wallet = dataStore.getWalletById(walletId);
        if (wallet != null) {
            double newBalance = wallet.getBalance() + (isIncome ? amount : -amount);
            wallet.setBalance(newBalance);
            dataStore.updateWallet(wallet);
        }
    }
    
    private void loadTransactions() {
        List<Transaction> transactions = dataStore.getTransactions();
        transactionData.clear();
        transactionData.addAll(transactions);
        updateBalanceDisplay();
    }
    
    private void updateBalanceDisplay() {
        double income = dataStore.getTotalIncome();
        double expenses = dataStore.getTotalExpenses();
        double balance = income - expenses;
        
        balanceLabel.setText(String.format("Balance: $%.2f", balance));
        
        if (balance >= 0) {
            balanceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        } else {
            balanceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        }
    }
    
    private String getCategoryName(String categoryId) {
        if (categoryId == null) return "Unknown";
        List<Category> categories = categoryService.getAllCategories();
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category.getName();
            }
        }
        return "Unknown";
    }
    
    private String getWalletName(String walletId) {
        if (walletId == null) return "Unknown";
        Wallet wallet = dataStore.getWalletById(walletId);
        return wallet != null ? wallet.getName() : "Unknown";
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
