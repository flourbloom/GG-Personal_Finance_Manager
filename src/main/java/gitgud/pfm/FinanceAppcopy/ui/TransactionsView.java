package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.data.DataStore;
import gitgud.pfm.FinanceAppcopy.model.Account;
import gitgud.pfm.FinanceAppcopy.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
// FontAwesome removed: using simple Region for header icon

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionsView extends BorderPane {
    
    private DataStore dataStore;
    private TableView<Transaction> table;
    private ObservableList<Transaction> transactionData;
    
    public TransactionsView() {
        dataStore = DataStore.getInstance();
        
        setStyle("-fx-background-color: #f0f2f5;");
        setPadding(new Insets(28));
        
        // Header
        HBox header = createHeader();
        setTop(header);
        
        // Table
        VBox tableContainer = createTableView();
        setCenter(tableContainer);
        
        loadData();
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 24, 0));
        
        
        Label title = new Label("All Transactions");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("Add Transaction");
        addBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                       "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                       "-fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddTransactionDialog());
        
        header.getChildren().addAll(title, spacer, addBtn);
        return header;
    }
    
    private VBox createTableView() {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        container.setPadding(new Insets(24));
        
        table = new TableView<>();
        table.setStyle("-fx-background-color: transparent;");
        
        // ID Column
        TableColumn<Transaction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        // Type Column
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        typeCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if (item.equals("income")) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else if (item.equals("expense")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Title Column
        TableColumn<Transaction, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        // Category Column
        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        
        // Amount Column
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(120);
        amountCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });
        
        // Date Column
        TableColumn<Transaction, LocalDateTime> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        dateCol.setPrefWidth(200);
        dateCol.setCellFactory(col -> new TableCell<Transaction, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                }
            }
        });
        
        // Account Column
        TableColumn<Transaction, Integer> accountCol = new TableColumn<>("Account");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        accountCol.setPrefWidth(150);
        accountCol.setCellFactory(col -> new TableCell<Transaction, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Account account = dataStore.getAccountById(item);
                    setText(account != null ? account.getName() : "Unknown");
                }
            }
        });
        
        table.getColumns().addAll(idCol, typeCol, titleCol, categoryCol, amountCol, dateCol, accountCol);
        
        transactionData = FXCollections.observableArrayList();
        table.setItems(transactionData);
        
        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().add(table);
        
        return container;
    }
    
    private void loadData() {
        List<Transaction> transactions = dataStore.getTransactions();
        transactions.sort((a, b) -> b.getTime().compareTo(a.getTime()));
        transactionData.clear();
        transactionData.addAll(transactions);
    }
    
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
        
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("expense", "income", "transfer");
        typeBox.setValue("expense");
        
        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Grocery Shopping");
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("food", "transport", "shopping", "bills", "entertainment", "income", "other");
        categoryBox.setValue("food");
        
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        
        ComboBox<Account> accountBox = new ComboBox<>();
        accountBox.getItems().addAll(dataStore.getAccounts());
        accountBox.setConverter(new javafx.util.StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getName() : "";
            }
            
            @Override
            public Account fromString(String string) {
                return null;
            }
        });
        if (!dataStore.getAccounts().isEmpty()) {
            accountBox.setValue(dataStore.getAccounts().get(0));
        }
        
        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryBox, 1, 2);
        grid.add(new Label("Amount:"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Account:"), 0, 4);
        grid.add(accountBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    int newId = dataStore.getNextTransactionId();
                    
                    Transaction transaction = new Transaction(
                        newId,
                        typeBox.getValue(),
                        titleField.getText(),
                        categoryBox.getValue(),
                        amount,
                        accountBox.getValue().getId(),
                        null,
                        LocalDateTime.now()
                    );
                    
                    return transaction;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(transaction -> {
            dataStore.addTransaction(transaction);
            
            // Update account balance
            Account account = dataStore.getAccountById(transaction.getAccountId());
            if (account != null) {
                if (transaction.getType().equals("income")) {
                    account.setBalance(account.getBalance() + transaction.getAmount());
                } else if (transaction.getType().equals("expense")) {
                    account.setBalance(account.getBalance() - transaction.getAmount());
                }
                dataStore.updateAccount(account);
            }
            
            loadData();
        });
    }
}
