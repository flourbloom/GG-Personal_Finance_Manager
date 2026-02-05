package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReportsController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox mainContent;
    @FXML private ComboBox<String> reportPeriodCombo;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netSavingsLabel;
    @FXML private Label savingsRateLabel;
    @FXML private Label incomeChangeLabel;
    @FXML private Label expenseChangeLabel;
    @FXML private Label savingsChangeLabel;
    @FXML private Label rateChangeLabel;
    @FXML private VBox expensePieChartContainer;
    @FXML private VBox categoryBreakdownList;
    @FXML private VBox incomeExpenseChartContainer;

    private DataStore dataStore;
    private boolean showPercentage = false;
    private PieChart expensePieChart;
    
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_COLORS = new LinkedHashMap<>();
    
    static {
        CATEGORY_NAMES.put("1", "Food & Drinks");
        CATEGORY_NAMES.put("2", "Transport");
        CATEGORY_NAMES.put("3", "Home Bills");
        CATEGORY_NAMES.put("4", "Self-care");
        CATEGORY_NAMES.put("5", "Shopping");
        CATEGORY_NAMES.put("6", "Health");
        CATEGORY_NAMES.put("7", "Subscription");
        CATEGORY_NAMES.put("8", "Entertainment & Sport");
        CATEGORY_NAMES.put("9", "Traveling");
        CATEGORY_NAMES.put("10", "Salary");
        CATEGORY_NAMES.put("11", "Investment");
        
        CATEGORY_ICONS.put("1", "🍔");
        CATEGORY_ICONS.put("2", "🚗");
        CATEGORY_ICONS.put("3", "🏠");
        CATEGORY_ICONS.put("4", "💆");
        CATEGORY_ICONS.put("5", "🛒");
        CATEGORY_ICONS.put("6", "💊");
        CATEGORY_ICONS.put("7", "📱");
        CATEGORY_ICONS.put("8", "🎮");
        CATEGORY_ICONS.put("9", "✈️");
        CATEGORY_ICONS.put("10", "💰");
        CATEGORY_ICONS.put("11", "📈");
        
        CATEGORY_COLORS.put("1", "#ef4444");
        CATEGORY_COLORS.put("2", "#f97316");
        CATEGORY_COLORS.put("3", "#eab308");
        CATEGORY_COLORS.put("4", "#84cc16");
        CATEGORY_COLORS.put("5", "#22c55e");
        CATEGORY_COLORS.put("6", "#14b8a6");
        CATEGORY_COLORS.put("7", "#06b6d4");
        CATEGORY_COLORS.put("8", "#3b82f6");
        CATEGORY_COLORS.put("9", "#8b5cf6");
        CATEGORY_COLORS.put("10", "#10b981");
        CATEGORY_COLORS.put("11", "#6366f1");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        dataStore.addWalletRefreshListener(this::refresh);
        
        if (reportPeriodCombo != null) {
            reportPeriodCombo.setOnAction(e -> loadReportData());
        }
        
        loadReportData();
    }

    private void loadReportData() {
        updateSummaryCards();
        
        loadExpensePieChart();
        loadCategoryBreakdown();
        loadIncomeExpenseChart();
    }

    private void updateSummaryCards() {
        List<Transaction> filteredTransactions = getFilteredTransactions();
        
        double totalIncome = filteredTransactions.stream()
                .filter(tx -> tx.getIncome() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalExpenses = filteredTransactions.stream()
                .filter(tx -> tx.getIncome() <= 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double netSavings = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;

        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        netSavingsLabel.setText(String.format("$%.2f", netSavings));
        savingsRateLabel.setText(String.format("%.1f%%", savingsRate));

        // Get selected period for label
        String selectedPeriod = reportPeriodCombo != null ? reportPeriodCombo.getValue() : "This Month";
        incomeChangeLabel.setText(selectedPeriod);
        expenseChangeLabel.setText(selectedPeriod);
        savingsChangeLabel.setText(selectedPeriod);
        rateChangeLabel.setText(selectedPeriod);
    }
    
    private List<Transaction> getFilteredTransactions() {
        List<Transaction> allTransactions = dataStore.getTransactions();
        String selectedPeriod = reportPeriodCombo != null ? reportPeriodCombo.getValue() : "This Month";
        
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        
        switch (selectedPeriod) {
            case "This Week":
                startDate = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                break;
            case "This Month":
                startDate = now.withDayOfMonth(1);
                break;
            case "Last 3 Months":
                startDate = now.minusMonths(3).withDayOfMonth(1);
                break;
            case "Last 6 Months":
                startDate = now.minusMonths(6).withDayOfMonth(1);
                break;
            case "This Year":
                startDate = now.withDayOfYear(1);
                break;
            default:
                startDate = now.withDayOfMonth(1);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return allTransactions.stream()
                .filter(tx -> {
                    try {
                        String createTime = tx.getCreateTime();
                        if (createTime == null) return false;
                        
                        LocalDate txDate;
                        try {
                            txDate = LocalDateTime.parse(createTime, formatter).toLocalDate();
                        } catch (Exception e) {
                            try {
                                txDate = LocalDate.parse(createTime, dateOnlyFormatter);
                            } catch (Exception e2) {
                                return false;
                            }
                        }
                        
                        return !txDate.isBefore(startDate) && !txDate.isAfter(now);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }


    
    private String getToggleStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-size: 13px; -fx-font-weight: 600;";
        }
        return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-size: 13px;";
    }
    


    private void loadExpensePieChart() {
        expensePieChartContainer.getChildren().clear();
        
        HBox toggleBox = new HBox(8);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        
        ToggleGroup displayToggle = new ToggleGroup();
        
        ToggleButton dollarBtn = new ToggleButton("$");
        dollarBtn.setToggleGroup(displayToggle);
        dollarBtn.setSelected(!showPercentage);
        dollarBtn.setStyle(getToggleStyle(!showPercentage));
        
        ToggleButton percentBtn = new ToggleButton("%");
        percentBtn.setToggleGroup(displayToggle);
        percentBtn.setSelected(showPercentage);
        percentBtn.setStyle(getToggleStyle(showPercentage));
        
        dollarBtn.setOnAction(e -> {
            showPercentage = false;
            dollarBtn.setStyle(getToggleStyle(true));
            percentBtn.setStyle(getToggleStyle(false));
            updatePieChart();
        });
        
        percentBtn.setOnAction(e -> {
            showPercentage = true;
            percentBtn.setStyle(getToggleStyle(true));
            dollarBtn.setStyle(getToggleStyle(false));
            updatePieChart();
        });
        
        toggleBox.getChildren().addAll(dollarBtn, percentBtn);
        
        expensePieChart = new PieChart();
        expensePieChart.setPrefHeight(300);
        expensePieChart.setPrefWidth(300);
        expensePieChart.setMinHeight(300);
        expensePieChart.setMinWidth(300);
        expensePieChart.setMaxHeight(300);
        expensePieChart.setMaxWidth(300);
        expensePieChart.setLegendVisible(false);
        expensePieChart.setLabelsVisible(true);
        expensePieChart.setLabelLineLength(10);
        expensePieChart.setStartAngle(90);
        
        updatePieChart();
        
        expensePieChartContainer.getChildren().addAll(toggleBox, expensePieChart);
        VBox.setMargin(toggleBox, new Insets(0, 0, 8, 0));
    }
    
    private void updatePieChart() {
        expensePieChart.getData().clear();
        
        // Income category IDs to exclude from spending breakdown
        java.util.Set<String> incomeCategories = java.util.Set.of("10", "11");
        
        List<Transaction> transactions = getFilteredTransactions();
        // Use LinkedHashMap to preserve insertion order
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        transactions.stream()
                .filter(tx -> tx.getIncome() <= 0)
                .filter(tx -> !incomeCategories.contains(tx.getCategoryId()))
                .forEach(tx -> {
                    String catId = tx.getCategoryId() != null ? tx.getCategoryId() : "Other";
                    categoryTotals.merge(catId, tx.getAmount(), Double::sum);
                });
        
        if (categoryTotals.isEmpty()) {
            return;
        }
        
        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Store category IDs in order for color assignment
        List<String> categoryIds = new ArrayList<>(categoryTotals.keySet());
        
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double amount = entry.getValue();
            double percentage = total > 0 ? (amount / total) * 100 : 0;
            
            String label;
            if (showPercentage) {
                label = String.format("%.1f%%", percentage);
            } else {
                label = "$" + String.format("%.0f", amount);
            }
            
            PieChart.Data slice = new PieChart.Data(label, amount);
            expensePieChart.getData().add(slice);
        }
        
        // Apply colors after data is added - use categoryIds list to match order
        javafx.application.Platform.runLater(() -> {
            for (int i = 0; i < expensePieChart.getData().size() && i < categoryIds.size(); i++) {
                String categoryId = categoryIds.get(i);
                PieChart.Data slice = expensePieChart.getData().get(i);
                String color = CATEGORY_COLORS.getOrDefault(categoryId, "#64748b");
                
                if (slice.getNode() != null) {
                    slice.getNode().setStyle("-fx-pie-color: " + color + ";");
                    
                    String name = CATEGORY_NAMES.getOrDefault(categoryId, categoryId);
                    double amount = categoryTotals.get(categoryId);
                    double percentage = total > 0 ? (amount / total) * 100 : 0;
                    Tooltip tooltip = new Tooltip(name + "\n$" + String.format("%.2f", amount) + " (" + String.format("%.1f%%", percentage) + ")");
                    Tooltip.install(slice.getNode(), tooltip);
                }
            }
        });
    }



    private void loadCategoryBreakdown() {
        categoryBreakdownList.getChildren().clear();

        // Income category IDs to exclude from spending breakdown
        java.util.Set<String> incomeCategories = java.util.Set.of("10", "11");

        List<Transaction> transactions = getFilteredTransactions();
        // Use LinkedHashMap to preserve insertion order
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        transactions.stream()
                .filter(tx -> tx.getIncome() <= 0)
                .filter(tx -> !incomeCategories.contains(tx.getCategoryId()))
                .forEach(tx -> {
                    String catId = tx.getCategoryId() != null ? tx.getCategoryId() : "Other";
                    categoryTotals.merge(catId, tx.getAmount(), Double::sum);
                });

        if (categoryTotals.isEmpty()) {
            Label emptyLabel = new Label("No expense data available");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            categoryBreakdownList.getChildren().add(emptyLabel);
            return;
        }

        // Add hint label
        Label hintLabel = new Label("Double-click a category to view transactions");
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        categoryBreakdownList.getChildren().add(hintLabel);

        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        // Sort categories by amount in descending order (highest spending first)
        categoryTotals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                String categoryId = entry.getKey();
                String categoryName = CATEGORY_NAMES.getOrDefault(categoryId, categoryId);
                String icon = CATEGORY_ICONS.getOrDefault(categoryId, "📦");
                String color = CATEGORY_COLORS.getOrDefault(categoryId, "#64748b");
                
                HBox categoryRow = createCategoryRow(categoryId, categoryName, icon, entry.getValue(), total, color);
                categoryBreakdownList.getChildren().add(categoryRow);
            });
    }

    private HBox createCategoryRow(String categoryId, String category, String icon, double amount, double total, String color) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 8, 12, 8));
        row.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-radius: 8;");

        row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showCategoryTransactionsPopup(categoryId, category);
            }
        });
        
        row.setOnMouseEntered(e -> row.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-color: #f8fafc; -fx-background-radius: 8;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-radius: 8;"));

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(36, 36);
        iconContainer.setMinSize(36, 36);
        iconContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        iconContainer.getChildren().add(iconLabel);

        Label nameLabel = new Label(category);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
        nameLabel.setPrefWidth(130);

        double percentage = total > 0 ? amount / total : 0;
        ProgressBar progressBar = new ProgressBar(percentage);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: " + color + ";");
        HBox.setHgrow(progressBar, Priority.ALWAYS);

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
    
    private void showCategoryTransactionsPopup(String categoryId, String categoryName) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(categoryName + " Transactions");
        
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: white;");
        
        Label header = new Label(CATEGORY_ICONS.getOrDefault(categoryId, "📦") + " " + categoryName);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        VBox transactionsList = new VBox(8);
        
        List<Transaction> categoryTransactions = dataStore.getTransactions().stream()
                .filter(tx -> categoryId.equals(tx.getCategoryId()))
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
        
        if (categoryTransactions.isEmpty()) {
            Label emptyLabel = new Label("No transactions in this category");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            transactionsList.getChildren().add(emptyLabel);
        } else {
            double totalAmount = 0;
            for (Transaction tx : categoryTransactions) {
                HBox txRow = new HBox(12);
                txRow.setAlignment(Pos.CENTER_LEFT);
                txRow.setPadding(new Insets(10));
                txRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");
                
                VBox details = new VBox(2);
                HBox.setHgrow(details, Priority.ALWAYS);
                
                Label nameLabel = new Label(tx.getName());
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
                
                Label dateLabel = new Label(tx.getCreateTime());
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
                
                details.getChildren().addAll(nameLabel, dateLabel);
                
                Label amountLabel = new Label(String.format("$%.2f", tx.getAmount()));
                String amountColor = tx.getIncome() > 0 ? "#22c55e" : "#ef4444";
                amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + amountColor + ";");
                
                txRow.getChildren().addAll(details, amountLabel);
                transactionsList.getChildren().add(txRow);
                
                totalAmount += tx.getAmount();
            }
            
            HBox totalRow = new HBox();
            totalRow.setAlignment(Pos.CENTER_RIGHT);
            totalRow.setPadding(new Insets(12, 0, 0, 0));
            totalRow.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");
            
            Label totalLabel = new Label("Total: $" + String.format("%.2f", totalAmount));
            totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
            totalRow.getChildren().add(totalLabel);
            
            transactionsList.getChildren().add(totalRow);
        }
        
        ScrollPane scrollPane = new ScrollPane(transactionsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10 24; -fx-font-size: 14px;");
        closeBtn.setOnAction(e -> popup.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeBtn);
        
        content.getChildren().addAll(header, scrollPane, buttonBox);
        
        Scene scene = new Scene(content, 500, 550);
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void loadIncomeExpenseChart() {
        if (incomeExpenseChartContainer == null) return;
        incomeExpenseChartContainer.getChildren().clear();
        
        // Create category axis for months
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#1e293b"));
        xAxis.setTickLabelFont(javafx.scene.text.Font.font(12));
        xAxis.setAnimated(false);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#1e293b"));
        yAxis.setTickLabelFont(javafx.scene.text.Font.font(12));
        yAxis.setAutoRanging(true);
        yAxis.setAnimated(false);
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Income and Expenses");
        barChart.setPrefHeight(400);
        barChart.setMinHeight(400);
        barChart.setLegendVisible(true);
        barChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        barChart.setCategoryGap(30);
        barChart.setBarGap(3);
        barChart.setAnimated(false);
        barChart.setStyle("-fx-font-size: 12px;");
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        
        // Get all transactions
        List<Transaction> allTransactions = dataStore.getTransactions();
        
        // Determine how many months to show based on selected period
        String selectedPeriod = reportPeriodCombo != null ? reportPeriodCombo.getValue() : "This Month";
        int monthsToShow;
        switch (selectedPeriod) {
            case "This Week":
            case "This Month":
                monthsToShow = 6; // Show last 6 months for context
                break;
            case "Last 3 Months":
                monthsToShow = 3;
                break;
            case "Last 6 Months":
                monthsToShow = 6;
                break;
            case "This Year":
                monthsToShow = 12;
                break;
            default:
                monthsToShow = 6;
        }
        
        // Create maps for monthly totals
        Map<YearMonth, Double> monthlyIncome = new LinkedHashMap<>();
        Map<YearMonth, Double> monthlyExpenses = new LinkedHashMap<>();
        
        // Initialize months
        YearMonth currentMonth = YearMonth.now();
        List<String> monthLabels = new ArrayList<>();
        for (int i = monthsToShow - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            monthlyIncome.put(month, 0.0);
            monthlyExpenses.put(month, 0.0);
            monthLabels.add(month.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        }
        
        // Set categories on x-axis
        xAxis.setCategories(javafx.collections.FXCollections.observableArrayList(monthLabels));
        
        // Parse transactions and aggregate by month
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Transaction tx : allTransactions) {
            try {
                String createTime = tx.getCreateTime();
                if (createTime == null) continue;
                
                LocalDate txDate;
                try {
                    txDate = LocalDateTime.parse(createTime, formatter).toLocalDate();
                } catch (Exception e) {
                    try {
                        txDate = LocalDate.parse(createTime, dateOnlyFormatter);
                    } catch (Exception e2) {
                        continue;
                    }
                }
                
                YearMonth txMonth = YearMonth.from(txDate);
                
                if (monthlyIncome.containsKey(txMonth)) {
                    if (tx.getIncome() > 0) {
                        monthlyIncome.merge(txMonth, tx.getAmount(), Double::sum);
                    } else {
                        monthlyExpenses.merge(txMonth, tx.getAmount(), Double::sum);
                    }
                }
            } catch (Exception e) {
                // Skip transactions with invalid dates
            }
        }
        
        // Add data to series using consistent month labels
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (YearMonth month : monthlyIncome.keySet()) {
            String monthLabel = month.format(monthFormatter);
            incomeSeries.getData().add(new XYChart.Data<>(monthLabel, monthlyIncome.get(month)));
            // Show expenses as negative for visual distinction
            expenseSeries.getData().add(new XYChart.Data<>(monthLabel, -monthlyExpenses.get(month)));
        }
        
        barChart.getData().addAll(incomeSeries, expenseSeries);
        
        // Style the bars after rendering
        javafx.application.Platform.runLater(() -> {
            // Style income bars (green)
            for (XYChart.Data<String, Number> data : incomeSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #166534;");
                }
            }
            // Style expense bars (red)
            for (XYChart.Data<String, Number> data : expenseSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ef4444;");
                }
            }
        });
        
        incomeExpenseChartContainer.getChildren().add(barChart);
    }

    public void refresh() {
        javafx.application.Platform.runLater(this::loadReportData);
    }
}
