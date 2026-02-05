package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.CategoryService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @FXML private Button clearFiltersButton;
    @FXML private VBox transactionsList;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private DataStore dataStore;
    private CategoryService categoryService;
    private Map<String, String> categoryIdToNameMap;
    private int currentPage = 1;
    private int itemsPerPage = 20;
    private List<Transaction> filteredTransactions;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        categoryService = new CategoryService();
        
        // Build category ID to name mapping
        buildCategoryMap();
        
        addTransactionButton.setOnAction(e -> showAddTransactionDialog());
        prevPageButton.setOnAction(e -> previousPage());
        nextPageButton.setOnAction(e -> nextPage());
        
        // Clear filters button
        if (clearFiltersButton != null) {
            clearFiltersButton.setOnAction(e -> clearFilters());
        }
        
        // Filter listeners
        categoryFilter.setOnAction(e -> applyFilters());
        typeFilter.setOnAction(e -> applyFilters());
        fromDatePicker.setOnAction(e -> applyFilters());
        toDatePicker.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        loadTransactions();
    }
    
    private void clearFilters() {
        categoryFilter.setValue("All Categories");
        typeFilter.setValue("All Types");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        searchField.clear();
        currentPage = 1;
        loadTransactions();
    }
    
    private void buildCategoryMap() {
        categoryIdToNameMap = new HashMap<>();
        List<Category> categories = categoryService.getDefaultCategories();
        for (Category cat : categories) {
            categoryIdToNameMap.put(cat.getId(), cat.getName());
        }
    }
    
    private String getCategoryNameById(String categoryId) {
        if (categoryId == null) return "Other";
        return categoryIdToNameMap.getOrDefault(categoryId, categoryId);
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
                    // Category filter - match by category name
                    String categoryFilterValue = categoryFilter.getValue();
                    if (categoryFilterValue != null && !categoryFilterValue.equals("All Categories")) {
                        String txCategoryName = getCategoryNameById(tx.getCategoryId());
                        // Match if category name contains filter value (case insensitive)
                        if (!txCategoryName.toLowerCase().contains(categoryFilterValue.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // Type filter
                    String type = typeFilter.getValue();
                    if (type != null && !type.equals("All Types")) {
                        if (type.equals("Income") && tx.getIncome() <= 0) return false;
                        if (type.equals("Expense") && tx.getIncome() > 0) return false;
                    }
                    
                    // Date filter
                    LocalDate fromDate = fromDatePicker.getValue();
                    LocalDate toDate = toDatePicker.getValue();
                    if (fromDate != null || toDate != null) {
                        try {
                            String createTime = tx.getCreateTime();
                            LocalDate txDate;
                            try {
                                txDate = LocalDate.parse(createTime.substring(0, 10));
                            } catch (Exception e) {
                                return true; // Include if date can't be parsed
                            }
                            
                            if (fromDate != null && txDate.isBefore(fromDate)) return false;
                            if (toDate != null && txDate.isAfter(toDate)) return false;
                        } catch (Exception e) {
                            // Include transaction if date parsing fails
                        }
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
        item.setPadding(new Insets(14, 18, 14, 18));
        item.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10;");
        VBox.setMargin(item, new Insets(0, 0, 8, 0));

        // Transaction name only (no category below since there's a dedicated category column)
        Label nameLabel = new Label(tx.getName());
        nameLabel.setPrefWidth(180);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");

        // Category
        String categoryName = getCategoryNameById(tx.getCategoryId());
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setPrefWidth(140);
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Date
        Label dateLabel = new Label(tx.getCreateTime());
        dateLabel.setPrefWidth(140);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Wallet
        Label walletLabel = new Label(tx.getWalletId() != null ? tx.getWalletId() : "—");
        walletLabel.setPrefWidth(140);
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

        item.getChildren().addAll(nameLabel, categoryLabel, dateLabel, walletLabel, spacer, amountLabel, editBtn);
        return item;
    }

    private void showEditTransactionDialog(Transaction tx) {
        try {
            // Create popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Edit Transaction");
            
            // Load the edit form FXML
            FXMLLoader formLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/edit-transaction-form.fxml"));
            BorderPane formRoot = formLoader.load();
            EditTransactionFormController formController = formLoader.getController();
            formController.setDialogStage(popupStage);
            formController.setFormRoot(formRoot);
            formController.setTransaction(tx);
            formController.setOnSaveCallback(() -> refresh());
            
            // Show the dialog
            Scene scene = new Scene(formRoot, 720, 520);
            popupStage.setScene(scene);
            popupStage.showAndWait();
            
            // Refresh transactions after popup closes
            refresh();
        } catch (IOException e) {
            System.err.println("Error loading Edit Transaction dialog: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open Edit Transaction dialog");
            alert.showAndWait();
        }
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
            formController.setFormRoot(formRoot);
            formController.setDialogStage(popupStage);
            
            // Load the category selection FXML
            FXMLLoader categoryLoader = new FXMLLoader(getClass().getResource("/gitgud/pfm/add-transaction-category.fxml"));
            BorderPane categoryRoot = categoryLoader.load();
            AddTransactionCategoryController categoryController = categoryLoader.getController();
            categoryController.setFormController(formController);
            
            // Start with category selection
            Scene scene = new Scene(categoryRoot, 720, 600);
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
