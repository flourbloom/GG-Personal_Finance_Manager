package gitgud.pfm.GUI;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class DashboardView extends ScrollPane {
    
    private DataStore dataStore;
    private VBox mainContent;
    private Label totalSpentLabel;
    private Label goalPercentLabel;
    private ProgressBar goalProgress;
    private Label goalHintLabel;
    
    public DashboardView() {
        dataStore = DataStore.getInstance();
        
        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        
        // Budget Goal Section
        VBox budgetGoal = createBudgetGoalCard();
        
        // Priority Goals Section
        VBox priorityGoals = createPriorityGoalsCard();
        
        // Spending Chart Section
        VBox spendingChart = createSpendingChartCard();
        
        // Recent Transactions Section
        VBox transactions = createTransactionsCard();
        
        mainContent.getChildren().addAll(budgetGoal, priorityGoals, spendingChart, transactions);
        
        setContent(mainContent);
        setFitToWidth(true);
        setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
        
        updateBudgetGoal();
    }
    
    private VBox createCard() {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));
        return card;
    }
    
    private HBox createCardHeader(String title) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Region iconView = new Region();
        iconView.setPrefSize(18, 18);
        iconView.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 4;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(iconView, titleLabel);
        
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().add(titleBox);
        
        return header;
    }
    
    private VBox createBudgetGoalCard() {
        VBox card = createCard();
        
        HBox header = createCardHeader("Monthly Budget Goal");
        
        // Goal details
        HBox goalDetails = new HBox();
        goalDetails.setAlignment(Pos.CENTER);
        goalDetails.setPadding(new Insets(0, 0, 12, 0));
        
        HBox goalText = new HBox(8);
        goalText.setAlignment(Pos.BASELINE_LEFT);
        
        totalSpentLabel = new Label("$0");
        totalSpentLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        Label separator = new Label("of");
        separator.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        Label budget = new Label("$3,000");
        budget.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b;");
        
        goalText.getChildren().addAll(totalSpentLabel, separator, budget);
        
        goalPercentLabel = new Label("0%");
        goalPercentLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #f59e0b;");
        
        HBox.setHgrow(goalText, Priority.ALWAYS);
        goalDetails.getChildren().addAll(goalText, goalPercentLabel);
        
        // Progress bar
        goalProgress = new ProgressBar(0);
        goalProgress.setPrefHeight(12);
        goalProgress.setMaxWidth(Double.MAX_VALUE);
        goalProgress.setStyle("-fx-accent: #3b82f6;");
        
        // Goal hint
        goalHintLabel = new Label("$3,000 remaining this month");
        goalHintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        VBox.setMargin(goalHintLabel, new Insets(12, 0, 0, 0));
        
        card.getChildren().addAll(header, goalDetails, goalProgress, goalHintLabel);
        return card;
    }
    
    private VBox createPriorityGoalsCard() {
        VBox card = createCard();
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = createCardTitle("Priority Goals");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Hyperlink viewAll = new Hyperlink("View All");
        viewAll.setStyle("-fx-font-size: 14px; -fx-text-fill: #3b82f6; -fx-font-weight: 500;");
        
        header.getChildren().addAll(title, viewAll);
        
        VBox goalsList = new VBox(12);
        
        List<Goal> priorityGoals = dataStore.getGoals().stream()
                .filter(g -> g.getPriority() > 5 && g.getBalance() < g.getTarget())
                .collect(Collectors.toList());
        
        for (Goal goal : priorityGoals) {
            HBox goalItem = createPriorityGoalItem(goal);
            goalsList.getChildren().add(goalItem);
        }
        
        if (priorityGoals.isEmpty()) {
            Label emptyLabel = new Label("No priority goals");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            goalsList.getChildren().add(emptyLabel);
        }
        
        card.getChildren().addAll(header, goalsList);
        return card;
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
        
        item.getChildren().addAll(info, progressBox);
        return item;
    }
    
    private Label createCardTitle(String text) {
        Region iconView = new Region();
        iconView.setPrefSize(18, 18);
        iconView.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 4;");
        
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        box.getChildren().addAll(iconView, label);
        
        Label result = new Label();
        result.setGraphic(box);
        return result;
    }
    
    private VBox createSpendingChartCard() {
        VBox card = createCard();
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = createCardTitle("Spending Comparison");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        ComboBox<String> periodSelect = new ComboBox<>();
        periodSelect.getItems().addAll("This Month vs Last Month", "Last 3 Months", "Last 6 Months");
        periodSelect.setValue("This Month vs Last Month");
        periodSelect.setStyle("-fx-font-size: 13px;");
        
        header.getChildren().addAll(title, periodSelect);
        
        // Create chart
        NumberAxis xAxis = new NumberAxis(1, 4, 1);
        xAxis.setLabel("Week");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setPrefHeight(280);
        lineChart.setLegendVisible(true);
        
        XYChart.Series<Number, Number> thisMonth = new XYChart.Series<>();
        thisMonth.setName("This Month");
        thisMonth.getData().add(new XYChart.Data<>(1, 350));
        thisMonth.getData().add(new XYChart.Data<>(2, 420));
        thisMonth.getData().add(new XYChart.Data<>(3, 380));
        thisMonth.getData().add(new XYChart.Data<>(4, 450));
        
        XYChart.Series<Number, Number> lastMonth = new XYChart.Series<>();
        lastMonth.setName("Last Month");
        lastMonth.getData().add(new XYChart.Data<>(1, 300));
        lastMonth.getData().add(new XYChart.Data<>(2, 380));
        lastMonth.getData().add(new XYChart.Data<>(3, 340));
        lastMonth.getData().add(new XYChart.Data<>(4, 400));
        
        @SuppressWarnings("unchecked")
        XYChart.Series<Number,Number>[] series = new XYChart.Series[]{thisMonth, lastMonth};
        lineChart.getData().addAll(series);
        
        card.getChildren().addAll(header, lineChart);
        return card;
    }
    
    private VBox createTransactionsCard() {
        VBox card = createCard();
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = createCardTitle("Recent Transactions");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Hyperlink viewAll = new Hyperlink("View All");
        viewAll.setStyle("-fx-font-size: 14px; -fx-text-fill: #3b82f6; -fx-font-weight: 500;");
        
        header.getChildren().addAll(title, viewAll);
        
        VBox transactionsList = new VBox(4);
        
        List<Transaction> transactions = dataStore.getTransactions().stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(10)
                .collect(Collectors.toList());
        
        for (Transaction tx : transactions) {
            HBox txItem = createTransactionItem(tx);
            transactionsList.getChildren().add(txItem);
        }
        
        card.getChildren().addAll(header, transactionsList);
        return card;
    }
    
    private HBox createTransactionItem(Transaction tx) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14, 0, 14, 0));
        item.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        
        // Icon
        StackPane icon = createTransactionIcon(tx.getCategoryId());
        
        // Details
        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox.setMargin(details, new Insets(0, 14, 0, 14));
        
        Label titleLabel = new Label(tx.getName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
        
        Label timeLabel = new Label(tx.getCreateTime());
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        details.getChildren().addAll(titleLabel, timeLabel);
        
        // Amount
        String sign = tx.getIncome() > 0 ? "+" : "-";
        String color = tx.getIncome() > 0 ? "#22c55e" : "#ef4444";
        
        Label amount = new Label(sign + String.format("$%.2f", tx.getAmount()));
        amount.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
        
        item.getChildren().addAll(icon, details, amount);
        return item;
    }
    
    private StackPane createTransactionIcon(String category) {
        StackPane pane = new StackPane();
        pane.setPrefSize(44, 44);
        pane.setStyle("-fx-background-radius: 12;");
        
        String bgColor, iconColor;
        String emoji;
        
        switch (category) {
            case "food":
                emoji = "🍴";
                bgColor = "#fef3c7";
                iconColor = "#d97706";
                break;
            case "transport":
                emoji = "🚗";
                bgColor = "#dbeafe";
                iconColor = "#2563eb";
                break;
            case "shopping":
                emoji = "🛍";
                bgColor = "#fce7f3";
                iconColor = "#db2777";
                break;
            case "bills":
                emoji = "📄";
                bgColor = "#fee2e2";
                iconColor = "#dc2626";
                break;
            case "income":
                emoji = "↓";
                bgColor = "#dcfce7";
                iconColor = "#16a34a";
                break;
            case "entertainment":
                emoji = "🎬";
                bgColor = "#ede9fe";
                iconColor = "#7c3aed";
                break;
            default:
                emoji = "●";
                bgColor = "#f1f5f9";
                iconColor = "#64748b";
        }
        
        pane.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");
        
        Label iconView = new Label(emoji);
        iconView.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: 20px;");
        
        pane.getChildren().add(iconView);
        return pane;
    }
    
    private void updateBudgetGoal() {
        double budgetLimit = 3000.0;
        
        double totalSpent = dataStore.getTotalExpenses();
        
        double percent = Math.min(100, (totalSpent / budgetLimit) * 100);
        double remaining = Math.max(0, budgetLimit - totalSpent);
        
        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
        goalPercentLabel.setText(String.format("%.0f%%", percent));
        goalProgress.setProgress(percent / 100.0);
        goalHintLabel.setText(String.format("$%.2f remaining this month", remaining));
    }
}
