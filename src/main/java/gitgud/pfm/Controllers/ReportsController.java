package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReportsController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox mainContent;
    @FXML private ComboBox<String> reportPeriodCombo;
    @FXML private Button exportButton;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netSavingsLabel;
    @FXML private Label savingsRateLabel;
    @FXML private Label incomeChangeLabel;
    @FXML private Label expenseChangeLabel;
    @FXML private Label savingsChangeLabel;
    @FXML private Label rateChangeLabel;
    @FXML private LineChart<String, Number> incomeExpenseChart;
    @FXML private PieChart expensePieChart;
    @FXML private BarChart<String, Number> monthlyTrendsChart;
    @FXML private VBox categoryBreakdownList;

    private DataStore dataStore;
    
    // Category ID to Name mapping
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_COLORS = new LinkedHashMap<>();
    
    static {
        // Expense categories
        CATEGORY_NAMES.put("1", "Food & Drinks");
        CATEGORY_NAMES.put("2", "Transport");
        CATEGORY_NAMES.put("3", "Home Bills");
        CATEGORY_NAMES.put("4", "Self-care");
        CATEGORY_NAMES.put("5", "Shopping");
        CATEGORY_NAMES.put("6", "Health");
        CATEGORY_NAMES.put("9", "Subscription");
        CATEGORY_NAMES.put("10", "Entertainment");
        CATEGORY_NAMES.put("11", "Traveling");
        // Income categories
        CATEGORY_NAMES.put("7", "Salary");
        CATEGORY_NAMES.put("8", "Investment");
        
        CATEGORY_ICONS.put("1", "🍔");
        CATEGORY_ICONS.put("2", "🚗");
        CATEGORY_ICONS.put("3", "🏠");
        CATEGORY_ICONS.put("4", "💆");
        CATEGORY_ICONS.put("5", "🛒");
        CATEGORY_ICONS.put("6", "💊");
        CATEGORY_ICONS.put("9", "📱");
        CATEGORY_ICONS.put("10", "🎮");
        CATEGORY_ICONS.put("11", "✈️");
        CATEGORY_ICONS.put("7", "💰");
        CATEGORY_ICONS.put("8", "📈");
        
        CATEGORY_COLORS.put("1", "#ef4444");
        CATEGORY_COLORS.put("2", "#f97316");
        CATEGORY_COLORS.put("3", "#eab308");
        CATEGORY_COLORS.put("4", "#84cc16");
        CATEGORY_COLORS.put("5", "#22c55e");
        CATEGORY_COLORS.put("6", "#14b8a6");
        CATEGORY_COLORS.put("9", "#06b6d4");
        CATEGORY_COLORS.put("10", "#3b82f6");
        CATEGORY_COLORS.put("11", "#8b5cf6");
        CATEGORY_COLORS.put("7", "#10b981");
        CATEGORY_COLORS.put("8", "#6366f1");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        
        if (reportPeriodCombo != null) {
            reportPeriodCombo.setOnAction(e -> loadReportData());
        }
        
        if (exportButton != null) {
            exportButton.setOnAction(e -> exportReport());
        }
        
        loadReportData();
    }

    private void loadReportData() {
        updateSummaryCards();
        loadIncomeExpenseChart();
        loadExpensePieChart();
        loadMonthlyTrendsChart();
        loadCategoryBreakdown();
    }

    private void updateSummaryCards() {
        double totalIncome = dataStore.getTotalIncome();
        double totalExpenses = dataStore.getTotalExpenses();
        double netSavings = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        netSavingsLabel.setText(String.format("$%.2f", netSavings));
        savingsRateLabel.setText(String.format("%.1f%%", savingsRate));

        // Placeholder change labels
        incomeChangeLabel.setText("+0% from last period");
        expenseChangeLabel.setText("+0% from last period");
        savingsChangeLabel.setText("+0% from last period");
        rateChangeLabel.setText("+0% from last period");
    }

    private void loadIncomeExpenseChart() {
        incomeExpenseChart.getData().clear();
        
        List<Transaction> transactions = dataStore.getTransactions();
        
        // Group transactions by week
        Map<String, Double> weeklyIncome = new LinkedHashMap<>();
        Map<String, Double> weeklyExpenses = new LinkedHashMap<>();
        
        // Initialize last 4 weeks
        LocalDate now = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            String weekLabel = "Week " + (4 - i);
            weeklyIncome.put(weekLabel, 0.0);
            weeklyExpenses.put(weekLabel, 0.0);
        }
        
        // Calculate actual data from transactions
        for (Transaction tx : transactions) {
            try {
                String createTime = tx.getCreateTime();
                if (createTime != null && !createTime.isEmpty()) {
                    LocalDate txDate = LocalDate.parse(createTime.substring(0, 10));
                    long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(txDate, now);
                    
                    String weekLabel;
                    if (daysAgo < 7) {
                        weekLabel = "Week 4";
                    } else if (daysAgo < 14) {
                        weekLabel = "Week 3";
                    } else if (daysAgo < 21) {
                        weekLabel = "Week 2";
                    } else if (daysAgo < 28) {
                        weekLabel = "Week 1";
                    } else {
                        continue; // Skip older transactions
                    }
                    
                    if (tx.getIncome() > 0) {
                        weeklyIncome.merge(weekLabel, tx.getAmount(), Double::sum);
                    } else {
                        weeklyExpenses.merge(weekLabel, tx.getAmount(), Double::sum);
                    }
                }
            } catch (Exception e) {
                // Skip transactions with invalid dates
            }
        }

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        for (Map.Entry<String, Double> entry : weeklyIncome.entrySet()) {
            incomeSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        for (Map.Entry<String, Double> entry : weeklyExpenses.entrySet()) {
            expenseSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void loadExpensePieChart() {
        expensePieChart.getData().clear();

        // Get transactions and group by category
        List<Transaction> transactions = dataStore.getTransactions();
        Map<String, Double> categoryTotals = transactions.stream()
                .filter(tx -> tx.getIncome() <= 0)
                .collect(Collectors.groupingBy(
                        tx -> tx.getCategoryId() != null ? tx.getCategoryId() : "Other",
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        if (categoryTotals.isEmpty()) {
            // Sample data if no transactions
            categoryTotals.put("1", 450.0);
            categoryTotals.put("2", 200.0);
            categoryTotals.put("5", 350.0);
            categoryTotals.put("3", 500.0);
            categoryTotals.put("10", 150.0);
        }

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String categoryId = entry.getKey();
            String icon = CATEGORY_ICONS.getOrDefault(categoryId, "📦");
            String name = CATEGORY_NAMES.getOrDefault(categoryId, categoryId);
            String displayName = icon + " " + name;
            
            PieChart.Data slice = new PieChart.Data(displayName, entry.getValue());
            expensePieChart.getData().add(slice);
        }
        
        // Apply colors to pie slices
        int index = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String color = CATEGORY_COLORS.getOrDefault(entry.getKey(), "#64748b");
            if (index < expensePieChart.getData().size()) {
                PieChart.Data slice = expensePieChart.getData().get(index);
                slice.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
            index++;
        }
    }

    private void loadMonthlyTrendsChart() {
        monthlyTrendsChart.getData().clear();

        List<Transaction> transactions = dataStore.getTransactions();
        
        // Get the last 6 months
        LocalDate now = LocalDate.now();
        Map<String, Double> monthlySpending = new LinkedHashMap<>();
        List<String> monthLabels = new ArrayList<>();
        
        // Initialize last 6 months with 0
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthLabel = monthDate.format(monthFormatter);
            monthlySpending.put(monthLabel, 0.0);
            monthLabels.add(monthLabel);
        }
        
        // Group expenses by month
        for (Transaction tx : transactions) {
            // Only count expenses (income <= 0 means expense)
            if (tx.getIncome() > 0) {
                continue;
            }
            
            try {
                String createTime = tx.getCreateTime();
                if (createTime != null && !createTime.isEmpty()) {
                    LocalDate txDate = LocalDate.parse(createTime.substring(0, 10));
                    
                    // Check if transaction is within the last 6 months
                    LocalDate sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1);
                    if (!txDate.isBefore(sixMonthsAgo)) {
                        String monthLabel = txDate.format(monthFormatter);
                        // Only add if this month is in our map
                        if (monthlySpending.containsKey(monthLabel)) {
                            monthlySpending.merge(monthLabel, tx.getAmount(), Double::sum);
                        }
                    }
                }
            } catch (Exception e) {
                // Skip transactions with invalid dates
            }
        }

        // Configure Y axis (amount with markings)
        NumberAxis yAxis = (NumberAxis) monthlyTrendsChart.getYAxis();
        double maxValue = monthlySpending.values().stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double upperBound = Math.max(100, Math.ceil(maxValue * 1.2 / 1000) * 1000);
        double tickUnit = Math.max(100, Math.ceil(upperBound / 5 / 100) * 100);
        
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(tickUnit);
        yAxis.setMinorTickVisible(true);
        yAxis.setMinorTickCount(5);
        yAxis.setTickLabelsVisible(true);
        yAxis.setTickMarkVisible(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Spending");
        
        // Add data with month labels
        for (String month : monthLabels) {
            double amount = monthlySpending.get(month);
            series.getData().add(new XYChart.Data<>(month, amount));
        }

        monthlyTrendsChart.getData().add(series);
        
        // Add labels on top of bars after rendering
        monthlyTrendsChart.applyCss();
        monthlyTrendsChart.layout();
        
        for (XYChart.Data<String, Number> data : series.getData()) {
            double amount = data.getYValue().doubleValue();
            String month = data.getXValue();
            
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: #3b82f6;");
                
                // Create a VBox to hold both month label and amount
                VBox labelBox = new VBox(2);
                labelBox.setAlignment(Pos.CENTER);
                
                // Amount label on top
                Label amountLabel = new Label("$" + String.format("%.0f", amount));
                amountLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
                
                labelBox.getChildren().add(amountLabel);
                
                // Position the labels above the bar
                StackPane barNode = (StackPane) data.getNode();
                barNode.getChildren().add(labelBox);
                StackPane.setAlignment(labelBox, Pos.TOP_CENTER);
                StackPane.setMargin(labelBox, new Insets(-20, 0, 0, 0));
            }
        }
    }

    private void loadCategoryBreakdown() {
        categoryBreakdownList.getChildren().clear();

        // Get transactions and group by category
        List<Transaction> transactions = dataStore.getTransactions();
        Map<String, Double> categoryTotals = transactions.stream()
                .filter(tx -> tx.getIncome() <= 0)
                .collect(Collectors.groupingBy(
                        tx -> tx.getCategoryId() != null ? tx.getCategoryId() : "Other",
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        if (categoryTotals.isEmpty()) {
            // Sample data if no transactions
            categoryTotals.put("1", 450.0);
            categoryTotals.put("2", 200.0);
            categoryTotals.put("5", 350.0);
            categoryTotals.put("3", 500.0);
            categoryTotals.put("10", 150.0);
        }

        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String categoryId = entry.getKey();
            String categoryName = CATEGORY_NAMES.getOrDefault(categoryId, categoryId);
            String icon = CATEGORY_ICONS.getOrDefault(categoryId, "📦");
            String color = CATEGORY_COLORS.getOrDefault(categoryId, "#64748b");
            
            HBox categoryRow = createCategoryRow(
                    categoryName,
                    icon,
                    entry.getValue(),
                    total,
                    color
            );
            categoryBreakdownList.getChildren().add(categoryRow);
        }
    }

    private HBox createCategoryRow(String category, String icon, double amount, double total, String color) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Icon with colored background
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(36, 36);
        iconContainer.setMinSize(36, 36);
        iconContainer.setStyle(
            "-fx-background-color: " + color + "20; " +
            "-fx-background-radius: 10;"
        );
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        iconContainer.getChildren().add(iconLabel);

        // Category name
        Label nameLabel = new Label(category);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
        nameLabel.setPrefWidth(130);

        // Progress bar
        double percentage = total > 0 ? amount / total : 0;
        ProgressBar progressBar = new ProgressBar(percentage);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: " + color + ";");
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Amount and percentage
        VBox amountBox = new VBox(2);
        amountBox.setAlignment(Pos.CENTER_RIGHT);
        amountBox.setPrefWidth(100);

        Label amountLabel = new Label(String.format("$%.2f", amount));
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label percentLabel = new Label(String.format("%.1f%%", percentage * 100));
        percentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        amountBox.getChildren().addAll(amountLabel, percentLabel);

        row.getChildren().addAll(iconContainer, nameLabel, progressBar, amountBox);
        return row;
    }

    private void exportReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Report");
        alert.setHeaderText("Export Feature");
        alert.setContentText("Report export functionality coming soon!");
        alert.show();
    }

    public void refresh() {
        loadReportData();
    }
}
