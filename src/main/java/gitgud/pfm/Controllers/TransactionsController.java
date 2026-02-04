package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TransactionsController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private Button addTransactionButton;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TextField searchField;
    @FXML private VBox transactionsList;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private DataStore dataStore;
    private int currentPage = 1;
    private int itemsPerPage = 15;
    private List<Transaction> filteredTransactions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        
        addTransactionButton.setOnAction(e -> showAddTransactionDialog());
        prevPageButton.setOnAction(e -> previousPage());
        nextPageButton.setOnAction(e -> nextPage());
        
        // Filter listeners
        categoryFilter.setOnAction(e -> applyFilters());
        typeFilter.setOnAction(e -> applyFilters());
        fromDatePicker.setOnAction(e -> applyFilters());
        toDatePicker.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        loadTransactions();
    }

    private void applyFilters() {
        currentPage = 1;
        loadTransactions();
    }

    private void loadTransactions() {
        transactionsList.getChildren().clear();
        
        List<Transaction> allTransactions = dataStore.getTransactions();
        
        // Apply filters
        filteredTransactions = allTransactions.stream()
                .filter(tx -> {
                    // Category filter
                    String category = categoryFilter.getValue();
                    if (category != null && !category.equals("All Categories")) {
                        if (!category.equalsIgnoreCase(tx.getCategoryId())) {
                            return false;
                        }
                    }
                    
                    // Type filter
                    String type = typeFilter.getValue();
                    if (type != null && !type.equals("All Types")) {
                        if (type.equals("Income") && tx.getIncome() <= 0) return false;
                        if (type.equals("Expense") && tx.getIncome() > 0) return false;
                    }
                    
                    // Search filter
                    String search = searchField.getText();
                    if (search != null && !search.isEmpty()) {
                        if (!tx.getName().toLowerCase().contains(search.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
        
        // Pagination
        int totalPages = Math.max(1, (int) Math.ceil((double) filteredTransactions.size() / itemsPerPage));
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredTransactions.size());
        
        List<Transaction> pageTransactions = filteredTransactions.subList(startIndex, endIndex);
        
        for (Transaction tx : pageTransactions) {
            HBox txItem = createTransactionItem(tx);
            transactionsList.getChildren().add(txItem);
        }
        
        if (pageTransactions.isEmpty()) {
            Label emptyLabel = new Label("No transactions found");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            transactionsList.getChildren().add(emptyLabel);
        }
        
        // Update pagination info
        pageInfoLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    private HBox createTransactionItem(Transaction tx) {
        HBox item = new HBox(16);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Transaction name and details
        VBox nameBox = new VBox(2);
        nameBox.setPrefWidth(250);
        Label nameLabel = new Label(tx.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
        Label descLabel = new Label(tx.getCategoryId() != null ? tx.getCategoryId() : "");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        nameBox.getChildren().addAll(nameLabel, descLabel);

        // Category
        Label categoryLabel = new Label(tx.getCategoryId() != null ? tx.getCategoryId() : "Other");
        categoryLabel.setPrefWidth(120);
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Date
        Label dateLabel = new Label(tx.getCreateTime());
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Wallet
        Label walletLabel = new Label(tx.getWalletId() != null ? tx.getWalletId() : "—");
        walletLabel.setPrefWidth(120);
        walletLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Amount
        String sign = tx.getIncome() > 0 ? "+" : "-";
        String color = tx.getIncome() > 0 ? "#22c55e" : "#ef4444";
        Label amountLabel = new Label(sign + String.format("$%.2f", tx.getAmount()));
        amountLabel.setPrefWidth(100);
        amountLabel.setAlignment(Pos.CENTER_RIGHT);
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");

        // Edit button with pencil icon
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;"));
        editBtn.setOnAction(e -> showEditTransactionDialog(tx));

        item.getChildren().addAll(nameBox, categoryLabel, dateLabel, walletLabel, spacer, amountLabel, editBtn);
        return item;
    }

    private void showEditTransactionDialog(Transaction tx) {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Modify transaction details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(tx.getName());
        nameField.setPromptText("Transaction name");

        TextField amountField = new TextField(String.valueOf(tx.getAmount()));
        amountField.setPromptText("0.00");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Expense", "Income");
        typeBox.setValue(tx.getIncome() > 0 ? "Income" : "Expense");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("food", "transport", "shopping", "bills", "entertainment", "income", "other");
        categoryBox.setValue(tx.getCategoryId() != null ? tx.getCategoryId() : "other");

        // Wallet selector
        ComboBox<String> walletBox = new ComboBox<>();
        java.util.Map<String, String> walletIdMap = new java.util.HashMap<>();
        for (var wallet : dataStore.getWallets()) {
            String displayName = wallet.getName() + " ($" + String.format("%.2f", wallet.getBalance()) + ")";
            walletBox.getItems().add(displayName);
            walletIdMap.put(displayName, wallet.getId());
        }
        // Select current wallet
        for (var entry : walletIdMap.entrySet()) {
            if (entry.getValue().equals(tx.getWalletId())) {
                walletBox.setValue(entry.getKey());
                break;
            }
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Account:"), 0, 4);
        grid.add(walletBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Style the delete button
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");

        // Store original values for balance adjustment
        final String originalWalletId = tx.getWalletId();
        final double originalAmount = tx.getAmount();
        final boolean originalWasIncome = tx.getIncome() > 0;

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double newAmount = Double.parseDouble(amountField.getText());
                    boolean newIsIncome = typeBox.getValue().equals("Income");
                    String newWalletId = walletIdMap.get(walletBox.getValue());
                    
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
                    
                    tx.setName(nameField.getText());
                    tx.setAmount(newAmount);
                    tx.setIncome(newIsIncome ? 1.0 : 0.0);
                    tx.setCategoryId(categoryBox.getValue());
                    tx.setWalletId(newWalletId);
                    return tx;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
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
                        dataStore.deleteTransaction(tx.getId());
                        dataStore.notifyWalletRefresh();
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedTx -> {
            dataStore.updateTransaction(updatedTx);
            dataStore.notifyWalletRefresh();
            refresh();
        });
    }
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadTransactions();
        }
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredTransactions.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            loadTransactions();
        }
    }

    @FXML
    private void showAddTransactionDialog() {
        try {
            // Create popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add Transaction");
            
            // Load the form FXML
            FXMLLoader formLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add-transaction-form.fxml"));
            BorderPane formRoot = formLoader.load();
            AddTransactionFormController formController = formLoader.getController();
            formController.setDialogStage(popupStage);
            formController.setFormRoot(formRoot);
            
            // Load the category selection FXML
            FXMLLoader categoryLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add-transaction-category.fxml"));
            BorderPane categoryRoot = categoryLoader.load();
            AddTransactionCategoryController categoryController = categoryLoader.getController();
            categoryController.setDialogStage(popupStage);
            categoryController.setFormController(formController);
            formController.setCategoryController(categoryController);
            
            // Start with category selection
            Scene scene = new Scene(categoryRoot, 700, 600);
            popupStage.setScene(scene);
            popupStage.showAndWait();
            
            // Refresh transactions after popup closes
            refresh();
        } catch (IOException e) {
            System.err.println("Error loading Add Transaction dialog: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open Add Transaction dialog");
            alert.showAndWait();
        }
    }

    public void refresh() {
        currentPage = 1;
        loadTransactions();
    }
}
