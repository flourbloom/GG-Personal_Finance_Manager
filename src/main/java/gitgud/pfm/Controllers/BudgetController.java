package gitgud.pfm.Controllers;

import gitgud.pfm.services.AccountDataLoader;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.CategoryService;
import gitgud.pfm.services.BudgetService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import gitgud.pfm.utils.DateFormatUtil;

public class BudgetController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private Button addBudgetButton;
    // Removed UI elements - no longer in FXML
    // @FXML private Label totalBudgetLabel;
    // @FXML private Label totalSpentLabel;
    // @FXML private Label remainingLabel;
    // @FXML private Label activeBudgetsLabel;
    // @FXML private ComboBox<String> monthSelector;
    // @FXML private Label budgetSpentLabel;
    // @FXML private Label budgetLimitLabel;
    // @FXML private Label budgetPercentLabel;
    // @FXML private ProgressBar budgetProgress;
    // @FXML private Label budgetHintLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private VBox budgetsList;

    private AccountDataLoader dataStore;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private Map<String, String> categoryIdToNameMap;
    private Map<String, String> categoryNameToIdMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = AccountDataLoader.getInstance();
        categoryService = new CategoryService();
        budgetService = new BudgetService();
        categoryIdToNameMap = new HashMap<>();
        categoryNameToIdMap = new HashMap<>();
        
        // Build category maps
        for (Category cat : categoryService.getDefaultCategories()) {
            categoryIdToNameMap.put(cat.getId(), cat.getName());
            categoryNameToIdMap.put(cat.getName(), cat.getId());
        }
        
        // Register for budget refresh notifications
        dataStore.addBudgetRefreshListener(this::refresh);
        
        // Month selector removed from UI
        // if (monthSelector != null) {
        //     monthSelector.setItems(FXCollections.observableArrayList(
        //         "This Month", "Last Month", "Last 3 Months"
        //     ));
        //     monthSelector.setValue("This Month");
        //     monthSelector.setOnAction(e -> updateMonthlyOverview());
        // }
        
        // Setup filter combo
        if (filterCombo != null) {
            filterCombo.setItems(FXCollections.observableArrayList(
                "All", "Monthly", "Weekly", "Yearly"
            ));
            filterCombo.setValue("All");
            filterCombo.setOnAction(e -> loadBudgets());
        }
        
        // Setup add budget button
        if (addBudgetButton != null) {
            styleAddBudgetButton();
            addBudgetButton.setOnAction(e -> showAddBudgetDialog());
        }
        
        // Summary and monthly overview sections removed from UI
        // updateSummary();
        // updateMonthlyOverview();
        loadBudgets();
    }

    private void styleAddBudgetButton() {
        addBudgetButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 12 24;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 8, 0, 0, 2);"
        );
        
        addBudgetButton.setOnMouseEntered(e -> {
            addBudgetButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 12, 0, 0, 4);"
            );
        });
        
        addBudgetButton.setOnMouseExited(e -> {
            addBudgetButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 8, 0, 0, 2);"
            );
        });
    }

    private void updateSummary() {
        // Summary section removed from UI - method no longer needed
        // List<Budget> budgets = dataStore.getBudgets();
        // 
        // // Calculate expenses for current month (This Month)
        // LocalDate[] currentMonthRange = getDateRangeFromSelector("This Month");
        // String startDate = DateFormatUtil.formatToIso(currentMonthRange[0]);
        // String endDate = DateFormatUtil.formatToIso(currentMonthRange[1]);
        // double totalExpenses = calculateTotalExpensesInDateRange(startDate, endDate);
        // 
        // double totalBudget = budgets.stream().mapToDouble(Budget::getLimitAmount).sum();
        // double remaining = Math.max(0, totalBudget - totalExpenses);
        // 
        // totalBudgetLabel.setText(String.format("$%.2f", totalBudget));
        // totalSpentLabel.setText(String.format("$%.2f", totalExpenses));
        // remainingLabel.setText(String.format("$%.2f", remaining));
        // activeBudgetsLabel.setText(String.valueOf(budgets.size()));
    }

    private void updateMonthlyOverview() {
        // Monthly overview section removed from UI - method no longer needed
        // List<Budget> budgets = dataStore.getBudgets();
        // 
        // // Get selected time period
        // String selectedPeriod = monthSelector != null ? monthSelector.getValue() : "This Month";
        // LocalDate[] dateRange = getDateRangeFromSelector(selectedPeriod);
        // String startDate = DateFormatUtil.formatToIso(dateRange[0]);
        // String endDate = DateFormatUtil.formatToIso(dateRange[1]);
        // 
        // // Calculate expenses for selected period
        // double totalExpenses = calculateTotalExpensesInDateRange(startDate, endDate);
        // 
        // // Find the monthly budget limit
        // double monthlyLimit = 0;
        // for (Budget budget : budgets) {
        //     if (budget.getPeriodType() == Budget.PeriodType.MONTHLY) {
        //         monthlyLimit = budget.getLimitAmount();
        //         break;
        //     }
        // }
        // 
        // // If no monthly budget, use total or default
        // if (monthlyLimit == 0 && !budgets.isEmpty()) {
        //     monthlyLimit = budgets.get(0).getLimitAmount();
        // }
        // if (monthlyLimit == 0) {
        //     monthlyLimit = 3000.0; // Default
        // }
        // 
        // double percent = monthlyLimit > 0 ? Math.min(100, (totalExpenses / monthlyLimit) * 100) : 0;
        // double remaining = Math.max(0, monthlyLimit - totalExpenses);
        // 
        // budgetSpentLabel.setText(String.format("$%.2f", totalExpenses));
        // budgetLimitLabel.setText(String.format("$%.2f", monthlyLimit));
        // budgetPercentLabel.setText(String.format("%.0f%%", percent));
        // budgetProgress.setProgress(percent / 100.0);
        // 
        // // Update hint based on spending
        // if (percent >= 100) {
        //     budgetHintLabel.setText("⚠️ Budget exceeded!");
        //     budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
        // } else if (percent >= 80) {
        //     budgetHintLabel.setText(String.format("⚠️ $%.2f remaining - approaching limit", remaining));
        //     budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
        // } else {
        //     budgetHintLabel.setText(String.format("$%.2f remaining this month", remaining));
        //     budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        // }
    }

    private void loadBudgets() {
        budgetsList.getChildren().clear();
        
        List<Budget> budgets = dataStore.getBudgets();
        double totalExpenses = dataStore.getTotalExpenses();
        
        // Apply filter
        String filter = filterCombo != null ? filterCombo.getValue() : "All";
        if (filter != null && !filter.equals("All")) {
            Budget.PeriodType filterType = Budget.PeriodType.valueOf(filter.toUpperCase());
            budgets = budgets.stream()
                .filter(b -> b.getPeriodType() == filterType)
                .toList();
        }
        
        for (Budget budget : budgets) {
            HBox budgetItem = createBudgetItem(budget, totalExpenses);
            budgetsList.getChildren().add(budgetItem);
        }
        
        if (budgets.isEmpty()) {
            VBox emptyState = createEmptyState();
            budgetsList.getChildren().add(emptyState);
        }
    }

    private HBox createBudgetItem(Budget budget, double totalExpenses) {
        HBox item = new HBox(16);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(16));
        
        // Calculate spending: check for multiple categories from junction table
        List<Category> budgetCategories = budgetService.getCategoriesForBudget(budget.getId());
        double spent;
        
        if (!budgetCategories.isEmpty()) {
            // Sum spending across all categories in this budget within the budget's date range
            spent = budgetCategories.stream()
                .mapToDouble(cat -> calculateCategorySpending(cat.getId(), budget.getStartDate(), budget.getEndDate()))
                .sum();
        } else {
            // No specific categories - calculate total expenses within budget's date range
            spent = calculateTotalExpensesInDateRange(budget.getStartDate(), budget.getEndDate());
        }
        
        double percent = budget.getLimitAmount() > 0 ? 
            Math.min(100, (spent / budget.getLimitAmount()) * 100) : 0;
        
        // Color based on percentage
        String bgColor, borderColor;
        if (percent >= 100) {
            bgColor = "linear-gradient(to right, #fef2f2, #fee2e2)";
            borderColor = "#ef4444";
        } else if (percent >= 80) {
            bgColor = "linear-gradient(to right, #fffbeb, #fef3c7)";
            borderColor = "#f59e0b";
        } else {
            bgColor = "linear-gradient(to right, #f0fdf4, #dcfce7)";
            borderColor = "#22c55e";
        }
        
        item.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-background-radius: 12; -fx-border-color: " + borderColor + "; " +
                     "-fx-border-width: 0 0 0 4; -fx-border-radius: 12;");

        // Budget info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(budget.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label(budget.getPeriodType().toString());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-background-color: #f1f5f9; " +
                          "-fx-padding: 2 8; -fx-background-radius: 4;");
        
        // Show categories for this budget
        if (!budgetCategories.isEmpty()) {
            if (budgetCategories.size() == 1) {
                // Show single category name
                Label categoryLabel = new Label(budgetCategories.get(0).getName());
                categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6; -fx-background-color: #eff6ff; " +
                                  "-fx-padding: 2 8; -fx-background-radius: 4;");
                meta.getChildren().add(categoryLabel);
            } else {
                // Show count of categories
                Label categoryLabel = new Label(budgetCategories.size() + " categories");
                categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b5cf6; -fx-background-color: #f3e8ff; " +
                                  "-fx-padding: 2 8; -fx-background-radius: 4;");
                meta.getChildren().add(categoryLabel);
            }
        }

        String displayStartDate = budget.getStartDate() != null ? 
            DateFormatUtil.isoToUkDate(budget.getStartDate()) : null;
        Label dateLabel = new Label(displayStartDate != null ? 
            "From: " + displayStartDate : "No date set");
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        meta.getChildren().addAll(typeLabel, dateLabel);
        info.getChildren().addAll(nameLabel, meta);

        // Progress section
        VBox progressSection = new VBox(4);
        progressSection.setAlignment(Pos.CENTER_RIGHT);
        progressSection.setPrefWidth(200);

        Label amountLabel = new Label(String.format("$%.2f / $%.2f", spent, budget.getLimitAmount()));
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");

        ProgressBar progress = new ProgressBar(Math.min(1.0, percent / 100.0));
        progress.setPrefHeight(8);
        progress.setPrefWidth(180);
        progress.setStyle("-fx-accent: " + borderColor + ";");

        Label percentLabel = new Label(String.format("%.0f%%", percent));
        percentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + borderColor + ";");

        progressSection.getChildren().addAll(amountLabel, progress, percentLabel);

        // Edit button
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;"));
        editBtn.setOnAction(e -> showEditBudgetDialog(budget));

        item.getChildren().addAll(info, progressSection, editBtn);

        item.setOnMouseClicked(event -> {
            if (isPrimaryDoubleClick(event)) {
                showEditBudgetDialog(budget);
            }
        });

        return item;
    }

    /**
     * Calculate total spending for a specific category within a date range
     */
    private double calculateCategorySpending(String categoryId, String startDate, String endDate) {
        List<Transaction> transactions = dataStore.getTransactions();
        return transactions.stream()
            .filter(t -> t.getIncome() <= 0) // Only expenses (income = 0 means expense)
            .filter(t -> categoryId.equals(t.getCategoryId()))
            .filter(t -> isTransactionInDateRange(t, startDate, endDate))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    /**
     * Check if a transaction falls within the specified date range
     */
    private boolean isTransactionInDateRange(Transaction transaction, String startDate, String endDate) {
        if (transaction.getCreateTime() == null || transaction.getCreateTime().isEmpty()) {
            return false;
        }
        if (startDate == null && endDate == null) {
            return true; // No date filtering
        }
        try {
            // Transaction createTime format: "yyyy-MM-dd HH:mm:ss"
            // Extract just the date part or parse as LocalDateTime
            LocalDate transactionDate;
            String createTime = transaction.getCreateTime();
            if (createTime.contains(" ")) {
                // Has time component - parse as datetime then get date
                transactionDate = java.time.LocalDateTime.parse(createTime, 
                    DateFormatUtil.ISO_DATETIME_FORMAT).toLocalDate();
            } else {
                // Just a date
                transactionDate = LocalDate.parse(createTime);
            }
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.MIN;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.MAX;
            return !transactionDate.isBefore(start) && !transactionDate.isAfter(end);
        } catch (Exception e) {
            return false; // Invalid date format
        }
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(12);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(40));
        
        Label emoji = new Label("💰");
        emoji.setStyle("-fx-font-size: 48px;");
        
        Label title = new Label("No budgets yet");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Create a budget to start tracking your spending");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        Button addBtn = new Button("+ Create Budget");
        addBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                       "-fx-background-radius: 8; -fx-padding: 10 20; -fx-font-size: 14px; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddBudgetDialog());
        VBox.setMargin(addBtn, new Insets(8, 0, 0, 0));
        
        emptyState.getChildren().addAll(emoji, title, subtitle, addBtn);
        return emptyState;
    }

    private void showAddBudgetDialog() {
        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle("Create Budget");
        dialog.setHeaderText("Set up a new budget");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Budget name");

        TextField limitField = new TextField();
        limitField.setPromptText("0.00");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("MONTHLY", "WEEKLY", "YEARLY", "CUSTOM");
        typeCombo.setValue("MONTHLY");

        // Category selection with button-based UI (multiple selection)
        final List<String> selectedCategoryIds = new ArrayList<>();
        final Map<String, String> categoryIdToName = new HashMap<>();
        
        VBox categoryContainer = new VBox(8);
        categoryContainer.setPadding(new Insets(8));
        categoryContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Header with selected category label
        Label selectedCategoryLabel = new Label("All Categories (General Budget)");
        selectedCategoryLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        // Create scrollable category buttons
        ScrollPane categoryScroll = new ScrollPane();
        categoryScroll.setFitToWidth(true);
        categoryScroll.setPrefHeight(180);
        categoryScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane categoryButtons = new FlowPane();
        categoryButtons.setHgap(8);
        categoryButtons.setVgap(8);
        categoryButtons.setPadding(new Insets(8));
        
        // Add "All Categories" button
        Button allCategoriesBtn = new Button("All Categories");
        allCategoriesBtn.setUserData("selected");
        styleCategoryButton(allCategoriesBtn, true);
        allCategoriesBtn.setOnAction(e -> {
            selectedCategoryIds.clear();
            selectedCategoryLabel.setText("All Categories (General Budget)");
            // Deselect all other buttons
            for (javafx.scene.Node node : categoryButtons.getChildren()) {
                if (node instanceof Button btn) {
                    boolean isSelected = btn == allCategoriesBtn;
                    btn.setUserData(isSelected ? "selected" : "unselected");
                    styleCategoryButton(btn, isSelected);
                }
            }
        });
        categoryButtons.getChildren().add(allCategoriesBtn);
        
        // Add category buttons for expense categories
        for (Category cat : categoryService.getDefaultCategories()) {
            if (cat.getType() == Category.Type.EXPENSE) {
                categoryIdToName.put(cat.getId(), cat.getName());
                Button categoryBtn = new Button(cat.getName());
                categoryBtn.setUserData("unselected");
                styleCategoryButton(categoryBtn, false);
                categoryBtn.setOnAction(e -> {
                    // Deselect "All Categories"
                    allCategoriesBtn.setUserData("unselected");
                    styleCategoryButton(allCategoriesBtn, false);
                    
                    // Toggle this category
                    boolean wasSelected = "selected".equals(categoryBtn.getUserData());
                    if (wasSelected) {
                        selectedCategoryIds.remove(cat.getId());
                        categoryBtn.setUserData("unselected");
                        styleCategoryButton(categoryBtn, false);
                    } else {
                        selectedCategoryIds.add(cat.getId());
                        categoryBtn.setUserData("selected");
                        styleCategoryButton(categoryBtn, true);
                    }
                    
                    // Update label
                    if (selectedCategoryIds.isEmpty()) {
                        selectedCategoryLabel.setText("All Categories (General Budget)");
                        allCategoriesBtn.setUserData("selected");
                        styleCategoryButton(allCategoriesBtn, true);
                    } else {
                        String labelText = selectedCategoryIds.size() == 1 
                            ? categoryIdToName.get(selectedCategoryIds.get(0))
                            : selectedCategoryIds.size() + " categories selected";
                        selectedCategoryLabel.setText(labelText);
                    }
                });
                categoryButtons.getChildren().add(categoryBtn);
            }
        }
        
        categoryScroll.setContent(categoryButtons);
        categoryContainer.getChildren().addAll(
            new Label("Selected:"),
            selectedCategoryLabel,
            new Label("Choose Category:"),
            categoryScroll
        );

        DatePicker startDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker endDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        
        // Configure DatePickers for UK format
        DateFormatUtil.configureDatePickerUkFormat(startDatePicker);
        DateFormatUtil.configureDatePickerUkFormat(endDatePicker);

        grid.add(new Label("Budget Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Limit Amount:"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryContainer, 1, 2);
        grid.add(new Label("Period Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(600);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    double limit = Double.parseDouble(limitField.getText());
                    String name = nameField.getText().isEmpty() ? "Monthly Budget" : nameField.getText();
                    String startDate = DateFormatUtil.formatToIso(startDatePicker.getValue());
                    String endDate = DateFormatUtil.formatToIso(endDatePicker.getValue());
                    Budget.PeriodType periodType = Budget.PeriodType.valueOf(typeCombo.getValue());
                    
                    // Create budget without categoryId (will use junction table)
                    Budget budget = new Budget(name, limit, 0, startDate, endDate, periodType, null, null);
                    
                    return budget;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(budget -> {
            // Add budget with selected categories
            List<String> categoryIds = selectedCategoryIds.isEmpty() ? null : new ArrayList<>(selectedCategoryIds);
            dataStore.addBudgetWithCategories(budget, categoryIds);
            dataStore.notifyBudgetRefresh();
            refresh();
        });
    }

    private void showEditBudgetDialog(Budget budget) {
        Dialog<Budget> dialog = new Dialog<>();
        dialog.setTitle("Edit Budget");
        dialog.setHeaderText("Modify budget details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(budget.getName());
        TextField limitField = new TextField(String.valueOf(budget.getLimitAmount()));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("MONTHLY", "WEEKLY", "YEARLY", "CUSTOM");
        typeCombo.setValue(budget.getPeriodType().toString());

        // Load existing categories for this budget
        List<Category> existingCategories = budgetService.getCategoriesForBudget(budget.getId());
        final List<String> selectedCategoryIds = new ArrayList<>();
        for (Category cat : existingCategories) {
            selectedCategoryIds.add(cat.getId());
        }
        final Map<String, String> categoryIdToName = new HashMap<>();
        
        VBox categoryContainer = new VBox(8);
        categoryContainer.setPadding(new Insets(8));
        categoryContainer.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        
        // Header with selected category label
        String initialLabelText = selectedCategoryIds.isEmpty() ? "All Categories (General Budget)" :
            (selectedCategoryIds.size() == 1 ? categoryIdToNameMap.get(selectedCategoryIds.get(0)) :
            selectedCategoryIds.size() + " categories selected");
        Label selectedCategoryLabel = new Label(initialLabelText);
        selectedCategoryLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        // Create scrollable category buttons
        ScrollPane categoryScroll = new ScrollPane();
        categoryScroll.setFitToWidth(true);
        categoryScroll.setPrefHeight(180);
        categoryScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane categoryButtons = new FlowPane();
        categoryButtons.setHgap(8);
        categoryButtons.setVgap(8);
        categoryButtons.setPadding(new Insets(8));
        
        // Add "All Categories" button
        Button allCategoriesBtn = new Button("All Categories");
        boolean allCatSelected = selectedCategoryIds.isEmpty();
        allCategoriesBtn.setUserData(allCatSelected ? "selected" : "unselected");
        styleCategoryButton(allCategoriesBtn, allCatSelected);
        allCategoriesBtn.setOnAction(e -> {
            selectedCategoryIds.clear();
            selectedCategoryLabel.setText("All Categories (General Budget)");
            // Deselect all other buttons
            for (javafx.scene.Node node : categoryButtons.getChildren()) {
                if (node instanceof Button btn) {
                    boolean isSelected = btn == allCategoriesBtn;
                    btn.setUserData(isSelected ? "selected" : "unselected");
                    styleCategoryButton(btn, isSelected);
                }
            }
        });
        categoryButtons.getChildren().add(allCategoriesBtn);
        
        // Add category buttons for expense categories
        for (Category cat : categoryService.getDefaultCategories()) {
            if (cat.getType() == Category.Type.EXPENSE) {
                categoryIdToName.put(cat.getId(), cat.getName());
                Button categoryBtn = new Button(cat.getName());
                boolean isSelected = selectedCategoryIds.contains(cat.getId());
                categoryBtn.setUserData(isSelected ? "selected" : "unselected");
                styleCategoryButton(categoryBtn, isSelected);
                categoryBtn.setOnAction(e -> {
                    // Deselect "All Categories"
                    allCategoriesBtn.setUserData("unselected");
                    styleCategoryButton(allCategoriesBtn, false);
                    
                    // Toggle this category
                    boolean wasSelected = "selected".equals(categoryBtn.getUserData());
                    if (wasSelected) {
                        selectedCategoryIds.remove(cat.getId());
                        categoryBtn.setUserData("unselected");
                        styleCategoryButton(categoryBtn, false);
                    } else {
                        selectedCategoryIds.add(cat.getId());
                        categoryBtn.setUserData("selected");
                        styleCategoryButton(categoryBtn, true);
                    }
                    
                    // Update label
                    if (selectedCategoryIds.isEmpty()) {
                        selectedCategoryLabel.setText("All Categories (General Budget)");
                        allCategoriesBtn.setUserData("selected");
                        styleCategoryButton(allCategoriesBtn, true);
                    } else {
                        String labelText = selectedCategoryIds.size() == 1 
                            ? categoryIdToName.get(selectedCategoryIds.get(0))
                            : selectedCategoryIds.size() + " categories selected";
                        selectedCategoryLabel.setText(labelText);
                    }
                });
                categoryButtons.getChildren().add(categoryBtn);
            }
        }
        
        categoryScroll.setContent(categoryButtons);
        categoryContainer.getChildren().addAll(
            new Label("Selected:"),
            selectedCategoryLabel,
            new Label("Choose Categories:"),
            categoryScroll
        );

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        
        // Configure DatePickers for UK format
        DateFormatUtil.configureDatePickerUkFormat(startDatePicker);
        DateFormatUtil.configureDatePickerUkFormat(endDatePicker);
        
        try {
            if (budget.getStartDate() != null) {
                startDatePicker.setValue(DateFormatUtil.parseIsoDate(budget.getStartDate()));
            }
            if (budget.getEndDate() != null) {
                endDatePicker.setValue(DateFormatUtil.parseIsoDate(budget.getEndDate()));
            }
        } catch (Exception e) {
            // Use defaults if parsing fails
        }

        grid.add(new Label("Budget Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Limit Amount:"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryContainer, 1, 2);
        grid.add(new Label("Period Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(600);

        // Style the delete button
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double limit = Double.parseDouble(limitField.getText());
                    budget.setName(nameField.getText());
                    budget.setLimitAmount(limit);
                    budget.setPeriodType(Budget.PeriodType.valueOf(typeCombo.getValue()));
                    
                    if (startDatePicker.getValue() != null) {
                        budget.setStartDate(DateFormatUtil.formatToIso(startDatePicker.getValue()));
                    }
                    if (endDatePicker.getValue() != null) {
                        budget.setEndDate(DateFormatUtil.formatToIso(endDatePicker.getValue()));
                    }
                    return budget;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Budget");
                confirm.setHeaderText("Are you sure you want to delete this budget?");
                confirm.setContentText("This action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dataStore.deleteBudget(budget.getId());
                        dataStore.notifyBudgetRefresh();
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedBudget -> {
            // Update budget with selected categories
            List<String> categoryIds = selectedCategoryIds.isEmpty() ? null : new ArrayList<>(selectedCategoryIds);
            dataStore.updateBudgetWithCategories(updatedBudget, categoryIds);
            dataStore.notifyBudgetRefresh();
            refresh();
        });
    }

    /**
     * Calculate total expenses within a date range
     */
    private double calculateTotalExpensesInDateRange(String startDate, String endDate) {
        List<Transaction> transactions = dataStore.getTransactions();
        return transactions.stream()
            .filter(t -> t.getIncome() <= 0) // Only expenses
            .filter(t -> isTransactionInDateRange(t, startDate, endDate))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    /**
     * Get date range based on month selector value
     */
    private LocalDate[] getDateRangeFromSelector(String selector) {
        LocalDate now = LocalDate.now();
        LocalDate start, end;
        
        switch (selector) {
            case "Last Month":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
                break;
            case "Last 3 Months":
                start = now.minusMonths(3).withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "This Month":
            default:
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
        }
        
        return new LocalDate[]{start, end};
    }
    
    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            // updateSummary();  // Summary section removed from UI
            // updateMonthlyOverview();  // Monthly overview section removed from UI
            loadBudgets();
        });
    }
    
    private void styleCategoryButton(Button button, boolean isSelected) {
        if (isSelected) {
            button.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #1d4ed8;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;"
            );
            // Remove hover handlers for selected buttons
            button.setOnMouseEntered(null);
            button.setOnMouseExited(null);
        } else {
            button.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #475569;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 500;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;"
            );
            
            // Add hover effect only for unselected buttons
            button.setOnMouseEntered(e -> {
                // Check userData to ensure button is still unselected
                if ("unselected".equals(button.getUserData())) {
                    button.setStyle(
                        "-fx-background-color: #f1f5f9;" +
                        "-fx-text-fill: #1e293b;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #94a3b8;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;"
                    );
                }
            });
            
            button.setOnMouseExited(e -> {
                // Check userData to ensure button is still unselected
                if ("unselected".equals(button.getUserData())) {
                    button.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-text-fill: #475569;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;"
                    );
                }
            });
        }
    }

    private boolean isPrimaryDoubleClick(MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2;
    }
}
