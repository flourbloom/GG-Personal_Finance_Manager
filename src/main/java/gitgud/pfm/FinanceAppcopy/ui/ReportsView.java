package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.data.DataStore;
import gitgud.pfm.FinanceAppcopy.model.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
// FontAwesome removed: no icon usage

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsView extends StackPane {
    
    private DataStore dataStore;
    
    public ReportsView() {
        dataStore = DataStore.getInstance();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
        
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        
        // Header
        HBox header = createHeader();
        
        // Summary Cards
        HBox summary = createSummaryCards();
        
        // Charts Row 1
        HBox chartsRow1 = new HBox(16);
        VBox categoryChart = createCategoryChart();
        VBox monthlyChart = createMonthlyChart();
        HBox.setHgrow(categoryChart, Priority.ALWAYS);
        HBox.setHgrow(monthlyChart, Priority.ALWAYS);
        chartsRow1.getChildren().addAll(categoryChart, monthlyChart);
        
        mainContent.getChildren().addAll(header, summary, chartsRow1);
        
        scrollPane.setContent(mainContent);
        getChildren().add(scrollPane);
        setStyle("-fx-background-color: #f0f2f5;");
    }
    
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Financial Reports");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        header.getChildren().addAll(title);
        return header;
    }
    
    private HBox createSummaryCards() {
        HBox summary = new HBox(16);
        
        List<Transaction> transactions = dataStore.getTransactions();
        LocalDateTime now = LocalDateTime.now();
        
        double totalIncome = transactions.stream()
                .filter(t -> t.getType().equals("income"))
                .filter(t -> t.getTime().getMonth() == now.getMonth())
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalExpenses = transactions.stream()
                .filter(t -> t.getType().equals("expense"))
                .filter(t -> t.getTime().getMonth() == now.getMonth())
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double savings = totalIncome - totalExpenses;
        
        VBox incomeCard = createSummaryCard("Total Income", totalIncome, "#22c55e");
        VBox expensesCard = createSummaryCard("Total Expenses", totalExpenses, "#ef4444");
        VBox savingsCard = createSummaryCard("Net Savings", savings, "#3b82f6");
        
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expensesCard, Priority.ALWAYS);
        HBox.setHgrow(savingsCard, Priority.ALWAYS);
        
        summary.getChildren().addAll(incomeCard, expensesCard, savingsCard);
        return summary;
    }
    
    private VBox createSummaryCard(String label, double amount, String color) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: 600;");

        header.getChildren().addAll(titleLabel);
        
        Label amountLabel = new Label(String.format("$%.2f", amount));
        amountLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(header, amountLabel);
        return card;
    }
    
    private VBox createCategoryChart() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));
        
        Label title = new Label("Expenses by Category");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        // Calculate category totals
        Map<String, Double> categoryTotals = new HashMap<>();
        List<Transaction> expenses = dataStore.getTransactions().stream()
                .filter(t -> t.getType().equals("expense"))
                .collect(Collectors.toList());
        
        for (Transaction t : expenses) {
            categoryTotals.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        
        PieChart chart = new PieChart();
        chart.setTitle("");
        chart.setLegendVisible(true);
        chart.setPrefHeight(300);
        
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                entry.getKey().toUpperCase() + " ($" + String.format("%.2f", entry.getValue()) + ")",
                entry.getValue()
            );
            chart.getData().add(slice);
        }
        
        VBox.setVgrow(chart, Priority.ALWAYS);
        card.getChildren().addAll(title, chart);
        return card;
    }
    
    private VBox createMonthlyChart() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));
        
        Label title = new Label("Monthly Trend");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(true);
        chart.setPrefHeight(300);
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        
        // Sample data for last 3 months
        LocalDateTime now = LocalDateTime.now();
        for (int i = 2; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthName = month.getMonth().toString().substring(0, 3);
            
            double income = dataStore.getTransactions().stream()
                    .filter(t -> t.getType().equals("income"))
                    .filter(t -> t.getTime().getMonth() == month.getMonth())
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            
            double expenses = dataStore.getTransactions().stream()
                    .filter(t -> t.getType().equals("expense"))
                    .filter(t -> t.getTime().getMonth() == month.getMonth())
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            
            incomeSeries.getData().add(new XYChart.Data<>(monthName, income > 0 ? income : 500));
            expenseSeries.getData().add(new XYChart.Data<>(monthName, expenses > 0 ? expenses : 300));
        }
        
        chart.getData().addAll(incomeSeries, expenseSeries);
        
        VBox.setVgrow(chart, Priority.ALWAYS);
        card.getChildren().addAll(title, chart);
        return card;
    }
}
