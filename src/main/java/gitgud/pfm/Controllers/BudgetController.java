package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Category;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.CategoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private Button addBudgetButton;
    @FXML private Label totalBudgetLabel;
    @FXML private Label totalSpentLabel;
    @FXML private Label remainingLabel;
    @FXML private Label activeBudgetsLabel;
    @FXML private ComboBox<String> monthSelector;
    @FXML private Label budgetSpentLabel;
    @FXML private Label budgetLimitLabel;
    @FXML private Label budgetPercentLabel;
    @FXML private ProgressBar budgetProgress;
    @FXML private Label budgetHintLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private VBox budgetsList;

    private DataStore dataStore;
    private CategoryService categoryService;
    private Map<String, String> categoryIdToNameMap;
    private Map<String, String> categoryNameToIdMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        categoryService = new CategoryService();
        categoryIdToNameMap = new HashMap<>();
        categoryNameToIdMap = new HashMap<>();
        
        // Build category maps
        for (Category cat : categoryService.getDefaultCategories()) {
            categoryIdToNameMap.put(cat.getId(), cat.getName());
            categoryNameToIdMap.put(cat.getName(), cat.getId());
        }
        
        // Register for budget refresh notifications
        dataStore.addBudgetRefreshListener(this::refresh);
        
        // Setup month selector
        if (monthSelector != null) {
            monthSelector.setItems(FXCollections.observableArrayList(
                "This Month", "Last Month", "Last 3 Months"
            ));
            monthSelector.setValue("This Month");
            monthSelector.setOnAction(e -> updateMonthlyOverview());
        }
        
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
        
        updateSummary();
        updateMonthlyOverview();
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
        List<Budget> budgets = dataStore.getBudgets();
        double totalExpenses = dataStore.getTotalExpenses();
        
        double totalBudget = budgets.stream().mapToDouble(Budget::getLimitAmount).sum();
        double remaining = Math.max(0, totalBudget - totalExpenses);
        
        totalBudgetLabel.setText(String.format("$%.2f", totalBudget));
        totalSpentLabel.setText(String.format("$%.2f", totalExpenses));
        remainingLabel.setText(String.format("$%.2f", remaining));
        activeBudgetsLabel.setText(String.valueOf(budgets.size()));
    }

    private void updateMonthlyOverview() {
        List<Budget> budgets = dataStore.getBudgets();
        double totalExpenses = dataStore.getTotalExpenses();
        
        // Find the monthly budget limit
        double monthlyLimit = 0;
        for (Budget budget : budgets) {
            if (budget.getPeriodType() == Budget.PeriodType.MONTHLY) {
                monthlyLimit = budget.getLimitAmount();
                break;
            }
        }
        
        // If no monthly budget, use total or default
        if (monthlyLimit == 0 && !budgets.isEmpty()) {
            monthlyLimit = budgets.get(0).getLimitAmount();
        }
        if (monthlyLimit == 0) {
            monthlyLimit = 3000.0; // Default
        }
        
        double percent = monthlyLimit > 0 ? Math.min(100, (totalExpenses / monthlyLimit) * 100) : 0;
        double remaining = Math.max(0, monthlyLimit - totalExpenses);
        
        budgetSpentLabel.setText(String.format("$%.2f", totalExpenses));
        budgetLimitLabel.setText(String.format("$%.2f", monthlyLimit));
        budgetPercentLabel.setText(String.format("%.0f%%", percent));
        budgetProgress.setProgress(percent / 100.0);
        
        // Update hint based on spending
        if (percent >= 100) {
            budgetHintLabel.setText("⚠️ Budget exceeded!");
            budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
        } else if (percent >= 80) {
            budgetHintLabel.setText(String.format("⚠️ $%.2f remaining - approaching limit", remaining));
            budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
        } else {
            budgetHintLabel.setText(String.format("$%.2f remaining this month", remaining));
            budgetHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        }
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
        
        // Calculate spending: category-specific or total
        double spent;
        if (budget.isCategoryBudget() && budget.getCategoryId() != null) {
            spent = calculateCategorySpending(budget.getCategoryId());
        } else {
            spent = totalExpenses;
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
        
        // Show category if it's a category budget
        if (budget.isCategoryBudget() && categoryIdToNameMap.containsKey(budget.getCategoryId())) {
            Label categoryLabel = new Label(categoryIdToNameMap.get(budget.getCategoryId()));
            categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6; -fx-background-color: #eff6ff; " +
                              "-fx-padding: 2 8; -fx-background-radius: 4;");
            meta.getChildren().add(categoryLabel);
        }

        Label dateLabel = new Label(budget.getStartDate() != null ? 
            "From: " + budget.getStartDate() : "No date set");
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
        return item;
    }

    /**
     * Calculate total spending for a specific category
     */
    private double calculateCategorySpending(String categoryId) {
        List<Transaction> transactions = dataStore.getTransactions();
        return transactions.stream()
            .filter(t -> t.getIncome() <= 0) // Only expenses (income = 0 means expense)
            .filter(t -> categoryId.equals(t.getCategoryId()))
            .mapToDouble(Transaction::getAmount)
            .sum();
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

        // Category selection for category-specific budgets
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPrefWidth(250);
        categoryCombo.getItems().add("All Categories (General Budget)");
        for (Category cat : categoryService.getDefaultCategories()) {
            if (cat.getType() == Category.Type.EXPENSE) { // Only show expense categories
                categoryCombo.getItems().add(cat.getName());
            }
        }
        categoryCombo.setValue("All Categories (General Budget)");

        DatePicker startDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker endDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));

        grid.add(new Label("Budget Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Limit Amount:"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Period Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    double limit = Double.parseDouble(limitField.getText());
                    String name = nameField.getText().isEmpty() ? "Monthly Budget" : nameField.getText();
                    String startDate = startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String endDate = endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Budget.PeriodType periodType = Budget.PeriodType.valueOf(typeCombo.getValue());
                    
                    // Get selected category ID
                    String categoryId = null;
                    String selectedCategory = categoryCombo.getValue();
                    if (selectedCategory != null && !selectedCategory.equals("All Categories (General Budget)")) {
                        categoryId = categoryNameToIdMap.get(selectedCategory);
                    }
                    
                    return new Budget(name, limit, 0, startDate, endDate, periodType, null, categoryId);
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
            dataStore.addBudget(budget);
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

        // Category selection for category-specific budgets
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPrefWidth(250);
        categoryCombo.getItems().add("All Categories (General Budget)");
        for (Category cat : categoryService.getDefaultCategories()) {
            if (cat.getType() == Category.Type.EXPENSE) { // Only show expense categories
                categoryCombo.getItems().add(cat.getName());
            }
        }
        // Set current category
        if (budget.getCategoryId() != null && categoryIdToNameMap.containsKey(budget.getCategoryId())) {
            categoryCombo.setValue(categoryIdToNameMap.get(budget.getCategoryId()));
        } else {
            categoryCombo.setValue("All Categories (General Budget)");
        }

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        
        try {
            if (budget.getStartDate() != null) {
                startDatePicker.setValue(LocalDate.parse(budget.getStartDate()));
            }
            if (budget.getEndDate() != null) {
                endDatePicker.setValue(LocalDate.parse(budget.getEndDate()));
            }
        } catch (Exception e) {
            // Use defaults if parsing fails
        }

        grid.add(new Label("Budget Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Limit Amount:"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Period Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);

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
                    
                    // Update category
                    String selectedCategory = categoryCombo.getValue();
                    if (selectedCategory != null && !selectedCategory.equals("All Categories (General Budget)")) {
                        budget.setCategoryId(categoryNameToIdMap.get(selectedCategory));
                    } else {
                        budget.setCategoryId(null);
                    }
                    
                    if (startDatePicker.getValue() != null) {
                        budget.setStartDate(startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    }
                    if (endDatePicker.getValue() != null) {
                        budget.setEndDate(endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
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
            dataStore.updateBudget(updatedBudget);
            dataStore.notifyBudgetRefresh();
            refresh();
        });
    }

    public void refresh() {
        javafx.application.Platform.runLater(() -> {
            updateSummary();
            updateMonthlyOverview();
            loadBudgets();
        });
    }
}
