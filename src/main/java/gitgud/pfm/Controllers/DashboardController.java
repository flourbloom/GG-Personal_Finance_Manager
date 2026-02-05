package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Budget;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private Label totalSpentLabel;
    @FXML private Label budgetLimitLabel;
    @FXML private Label goalPercentLabel;
    @FXML private ProgressBar goalProgress;
    @FXML private Label goalHintLabel;
    @FXML private VBox priorityGoalsList;
    @FXML private ComboBox<String> periodSelect;
    @FXML private LineChart<Number, Number> spendingChart;
    @FXML private VBox transactionsList;
    @FXML private Hyperlink viewAllTransactions;
    @FXML private Hyperlink viewAllGoals;
    @FXML private NumberAxis xAxis;

    private DataStore dataStore;
    private static Runnable onNavigateToGoals;
    private static Runnable onNavigateToTransactions;

    public static void setOnNavigateToGoals(Runnable callback) {
        onNavigateToGoals = callback;
    }

    public static void setOnNavigateToTransactions(Runnable callback) {
        onNavigateToTransactions = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        
        // Register for goal and budget refresh notifications
        dataStore.addGoalRefreshListener(this::refreshPriorityGoals);
        dataStore.addBudgetRefreshListener(this::refreshBudgetGoal);
        // Also listen for wallet/transaction changes to update budget display
        dataStore.addWalletRefreshListener(this::refreshBudgetGoal);
        
        updateBudgetGoal();
        loadPriorityGoals();
        loadSpendingChart();
        loadRecentTransactions();
        
        // Period selector listener
        if (periodSelect != null) {
            periodSelect.setOnAction(e -> loadSpendingChart());
        }
        
        // Setup navigation for View All links
        if (viewAllGoals != null) {
            viewAllGoals.setOnAction(e -> {
                if (onNavigateToGoals != null) {
                    onNavigateToGoals.run();
                }
            });
        }
        
        if (viewAllTransactions != null) {
            viewAllTransactions.setOnAction(e -> {
                if (onNavigateToTransactions != null) {
                    onNavigateToTransactions.run();
                }
            });
        }
    }
    
    private void refreshPriorityGoals() {
        javafx.application.Platform.runLater(this::loadPriorityGoals);
    }
    
    private void refreshBudgetGoal() {
        javafx.application.Platform.runLater(this::updateBudgetGoal);
    }

    private void updateBudgetGoal() {
        // Get budget limit from monthly budget in database, default to 3000.0
        double budgetLimit = getMonthlyBudgetLimit();
        double totalSpent = dataStore.getTotalExpenses();
        
        double percent = Math.min(100, (totalSpent / budgetLimit) * 100);
        double remaining = Math.max(0, budgetLimit - totalSpent);
        
        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
        // Update budget limit label dynamically
        if (budgetLimitLabel != null) {
            budgetLimitLabel.setText(String.format("$%.0f", budgetLimit));
        }
        goalPercentLabel.setText(String.format("%.0f%%", percent));
        goalProgress.setProgress(percent / 100.0);
        goalHintLabel.setText(String.format("$%.2f remaining this month", remaining));
    }
    
    private double getMonthlyBudgetLimit() {
        List<Budget> budgets = dataStore.getBudgets();
        // Look for a monthly budget
        for (Budget budget : budgets) {
            if (budget.getPeriodType() == Budget.PeriodType.MONTHLY) {
                return budget.getLimitAmount();
            }
        }
        // Return first budget limit if exists, otherwise default
        if (!budgets.isEmpty()) {
            return budgets.get(0).getLimitAmount();
        }
        return 3000.0; // Default budget limit
    }

    private void loadPriorityGoals() {
        priorityGoalsList.getChildren().clear();
        
        // Priority 1 is highest, so filter goals with priority <= 5 (top priorities)
        List<Goal> priorityGoals = dataStore.getGoals().stream()
                .filter(g -> g.getPriority() <= 5 && g.getBalance() < g.getTarget())
                .sorted((a, b) -> Double.compare(a.getPriority(), b.getPriority())) // Sort by priority (1 first)
                .collect(Collectors.toList());

        for (Goal goal : priorityGoals) {
            HBox goalItem = createPriorityGoalItem(goal);
            priorityGoalsList.getChildren().add(goalItem);
        }

        if (priorityGoals.isEmpty()) {
            Label emptyLabel = new Label("No priority goals");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            priorityGoalsList.getChildren().add(emptyLabel);
        }
    }

    private HBox createPriorityGoalItem(Goal goal) {
        HBox item = new HBox(16);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(16));
        item.setStyle("-fx-background-color: linear-gradient(to right, #fffbeb, #fef3c7); " +
                     "-fx-background-radius: 12; -fx-border-color: #f59e0b; " +
                     "-fx-border-width: 0 0 0 4; -fx-border-radius: 12;");

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(goal.getName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label amounts = new Label(String.format("$%.2f / $%.2f", goal.getBalance(), goal.getTarget()));
        amounts.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #64748b;");

        String deadlineStr = goal.getDeadline() != null ? goal.getDeadline() : "No deadline";
        Label deadline = new Label(deadlineStr);
        deadline.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        meta.getChildren().addAll(amounts, dot, deadline);
        info.getChildren().addAll(titleLabel, meta);

        HBox progressBox = new HBox(12);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPrefWidth(140);

        double progressPercent = goal.getTarget() > 0 ? (goal.getBalance() / goal.getTarget()) * 100 : 0;
        ProgressBar miniProgress = new ProgressBar(Math.min(1.0, progressPercent / 100.0));
        miniProgress.setPrefHeight(8);
        miniProgress.setPrefWidth(80);
        miniProgress.setStyle("-fx-accent: #f59e0b;");

        Label percent = new Label(String.format("%.0f%%", progressPercent));
        percent.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        progressBox.getChildren().addAll(miniProgress, percent);

        // Edit button with pencil icon
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;"));
        editBtn.setOnAction(e -> showEditGoalDialog(goal));

        item.getChildren().addAll(info, progressBox, editBtn);
        return item;
    }

    private void showEditGoalDialog(Goal goal) {
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("Edit Goal");
        dialog.setHeaderText("Modify goal details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(goal.getName());
        nameField.setPromptText("Goal name");

        TextField targetField = new TextField(String.valueOf(goal.getTarget()));
        targetField.setPromptText("Target amount");

        TextField currentField = new TextField(String.valueOf(goal.getBalance()));
        currentField.setPromptText("Current saved");

        TextField deadlineField = new TextField(goal.getDeadline() != null ? goal.getDeadline() : "");
        deadlineField.setPromptText("Deadline (YYYY-MM-DD)");

        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, (int) goal.getPriority());

        grid.add(new Label("Goal Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Target Amount:"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(new Label("Current Saved:"), 0, 2);
        grid.add(currentField, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(deadlineField, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(prioritySpinner, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Style the delete button
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double target = Double.parseDouble(targetField.getText());
                    double current = Double.parseDouble(currentField.getText());
                    goal.setName(nameField.getText());
                    goal.setTarget(target);
                    goal.setBalance(current);
                    goal.setDeadline(deadlineField.getText().isEmpty() ? null : deadlineField.getText());
                    goal.setPriority(prioritySpinner.getValue());
                    return goal;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid input!");
                    alert.show();
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Goal");
                confirm.setHeaderText("Are you sure you want to delete this goal?");
                confirm.setContentText("This action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dataStore.deleteGoal(goal.getId());
                        dataStore.notifyGoalRefresh();
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedGoal -> {
            dataStore.updateGoal(updatedGoal);
            dataStore.notifyGoalRefresh();
            refresh();
        });
    }

    private void loadSpendingChart() {
        spendingChart.getData().clear();
        
        // Get all transactions (expenses only) from all wallets by the accountholder
        List<Transaction> allTransactions = dataStore.getTransactions();
        
        // Get current month and last month
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        int daysInCurrentMonth = currentMonth.lengthOfMonth();
        int daysInLastMonth = lastMonth.lengthOfMonth();
        
        // Update x-axis bounds based on current month
        if (xAxis != null) {
            xAxis.setLowerBound(1);
            xAxis.setUpperBound(daysInCurrentMonth);
            xAxis.setTickUnit(1);
            xAxis.setMinorTickVisible(false);
            // Format tick labels as integers
            xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
                @Override
                public Number fromString(String string) {
                    return Integer.parseInt(string);
                }
            });
        }
        
        // Create maps to store daily expenses
        Map<Integer, Double> thisMonthExpenses = new HashMap<>();
        Map<Integer, Double> lastMonthExpenses = new HashMap<>();
        
        // Initialize all days with 0
        for (int day = 1; day <= daysInCurrentMonth; day++) {
            thisMonthExpenses.put(day, 0.0);
        }
        for (int day = 1; day <= daysInLastMonth; day++) {
            lastMonthExpenses.put(day, 0.0);
        }
        
        // Parse transactions and aggregate by day
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Transaction tx : allTransactions) {
            if (tx.getIncome() > 0) continue; // Skip income transactions
            
            try {
                String createTime = tx.getCreateTime();
                LocalDate txDate;
                
                // Try parsing with datetime format first, then date only
                try {
                    txDate = LocalDate.parse(createTime, formatter);
                } catch (Exception e) {
                    try {
                        txDate = LocalDate.parse(createTime, dateOnlyFormatter);
                    } catch (Exception e2) {
                        continue; // Skip if date cannot be parsed
                    }
                }
                
                int dayOfMonth = txDate.getDayOfMonth();
                YearMonth txMonth = YearMonth.from(txDate);
                
                if (txMonth.equals(currentMonth)) {
                    thisMonthExpenses.merge(dayOfMonth, tx.getAmount(), Double::sum);
                } else if (txMonth.equals(lastMonth)) {
                    lastMonthExpenses.merge(dayOfMonth, tx.getAmount(), Double::sum);
                }
            } catch (Exception e) {
                // Skip transactions with invalid dates
            }
        }
        
        // Create series for this month with cumulative spending
        XYChart.Series<Number, Number> thisMonthSeries = new XYChart.Series<>();
        thisMonthSeries.setName("This Month");
        double cumulativeThisMonth = 0.0;
        for (int day = 1; day <= daysInCurrentMonth; day++) {
            cumulativeThisMonth += thisMonthExpenses.get(day);
            thisMonthSeries.getData().add(new XYChart.Data<>(day, cumulativeThisMonth));
        }
        
        // Create series for last month with cumulative spending
        XYChart.Series<Number, Number> lastMonthSeries = new XYChart.Series<>();
        lastMonthSeries.setName("Last Month");
        double cumulativeLastMonth = 0.0;
        int maxDays = Math.min(daysInLastMonth, daysInCurrentMonth);
        for (int day = 1; day <= maxDays; day++) {
            cumulativeLastMonth += lastMonthExpenses.get(day);
            lastMonthSeries.getData().add(new XYChart.Data<>(day, cumulativeLastMonth));
        }

        spendingChart.getData().add(thisMonthSeries);
        spendingChart.getData().add(lastMonthSeries);
    }

    private void loadRecentTransactions() {
        transactionsList.getChildren().clear();

        List<Transaction> transactions = dataStore.getTransactions().stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(10)
                .collect(Collectors.toList());

        for (Transaction tx : transactions) {
            HBox txItem = createTransactionItem(tx);
            transactionsList.getChildren().add(txItem);
        }
    }

    private HBox createTransactionItem(Transaction tx) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 0, 14, 0));
        item.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        StackPane icon = createTransactionIcon(tx.getCategoryId());

        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox.setMargin(details, new Insets(0, 14, 0, 14));

        Label titleLabel = new Label(tx.getName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");

        Label timeLabel = new Label(tx.getCreateTime());
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        details.getChildren().addAll(titleLabel, timeLabel);

        String sign = tx.getIncome() > 0 ? "+" : "-";
        String color = tx.getIncome() > 0 ? "#22c55e" : "#ef4444";

        Label amount = new Label(sign + String.format("$%.2f", tx.getAmount()));
        amount.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");

        // Edit button with pencil icon
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;"));
        editBtn.setOnAction(e -> showEditTransactionDialog(tx));

        HBox.setMargin(editBtn, new Insets(0, 0, 0, 12));

        item.getChildren().addAll(icon, details, amount, editBtn);
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

    private StackPane createTransactionIcon(String categoryId) {
        StackPane pane = new StackPane();
        pane.setPrefSize(44, 44);

        String bgColor, emoji;

        switch (categoryId != null ? categoryId : "") {
            case "1": // Food & Drinks
                emoji = "🍔";
                bgColor = "#fee2e2";
                break;
            case "2": // Transport
                emoji = "🚗";
                bgColor = "#ffedd5";
                break;
            case "3": // Home Bills
                emoji = "🏠";
                bgColor = "#fef3c7";
                break;
            case "4": // Self-care
                emoji = "💆";
                bgColor = "#ecfccb";
                break;
            case "5": // Shopping
                emoji = "🛒";
                bgColor = "#dcfce7";
                break;
            case "6": // Health
                emoji = "💊";
                bgColor = "#ccfbf1";
                break;
            case "7": // Salary (Income)
                emoji = "💰";
                bgColor = "#d1fae5";
                break;
            case "8": // Investment (Income)
                emoji = "📈";
                bgColor = "#e0e7ff";
                break;
            case "9": // Subscription
                emoji = "📱";
                bgColor = "#cffafe";
                break;
            case "10": // Entertainment & Sport
                emoji = "🎮";
                bgColor = "#dbeafe";
                break;
            case "11": // Traveling
                emoji = "✈️";
                bgColor = "#ede9fe";
                break;
            default:
                emoji = "📋";
                bgColor = "#f1f5f9";
        }

        pane.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");

        Label iconView = new Label(emoji);
        iconView.setStyle("-fx-font-size: 20px;");

        pane.getChildren().add(iconView);
        return pane;
    }

    public void refresh() {
        updateBudgetGoal();
        loadPriorityGoals();
        loadSpendingChart();
        loadRecentTransactions();
    }
}
