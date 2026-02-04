package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Style the delete button
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    double income = typeBox.getValue().equals("Income") ? amount : 0;
                    tx.setName(nameField.getText());
                    tx.setAmount(amount);
                    tx.setIncome(income);
                    tx.setCategoryId(categoryBox.getValue());
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
                        dataStore.deleteTransaction(tx.getId());
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedTx -> {
            dataStore.updateTransaction(updatedTx);
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
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Add New Transaction");
        dialog.setHeaderText("Enter transaction details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Transaction name");

        TextField amountField = new TextField();
        amountField.setPromptText("0.00");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Expense", "Income");
        typeBox.setValue("Expense");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("food", "transport", "shopping", "bills", "entertainment", "income", "other");
        categoryBox.setValue("other");

        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    double income = typeBox.getValue().equals("Income") ? amount : 0;
                    Transaction tx = new Transaction(
                        categoryBox.getValue(),
                        amount,
                        nameField.getText(),
                        income,
                        null, // walletId
                        java.time.LocalDateTime.now().toString()
                    );
                    return tx;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tx -> {
            dataStore.addTransaction(tx);
            refresh();
        });
    }

    public void refresh() {
        currentPage = 1;
        loadTransactions();
    }
}
