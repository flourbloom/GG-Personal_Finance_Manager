package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.data.DataStore;
import gitgud.pfm.FinanceAppcopy.model.Account;
import gitgud.pfm.FinanceAppcopy.model.Goal;
import gitgud.pfm.FinanceAppcopy.model.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    
    private HBox createCardHeader(String title, FontAwesomeIcon icon) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18");
        iconView.setStyle("-fx-fill: #3b82f6;");
        
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
        
        HBox header = createCardHeader("Monthly Budget Goal", FontAwesomeIcon.BULLSEYE);
        
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
        
        Label title = createCardTitle("Priority Goals", FontAwesomeIcon.STAR);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Hyperlink viewAll = new Hyperlink("View All");
        viewAll.setStyle("-fx-font-size: 14px; -fx-text-fill: #3b82f6; -fx-font-weight: 500;");
        
        header.getChildren().addAll(title, viewAll);
        
        VBox goalsList = new VBox(12);
        
        List<Goal> priorityGoals = dataStore.getGoals().stream()
                .filter(g -> g.isPriority() && g.getCurrent() < g.getTarget())
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
        
        Label titleLabel = new Label(goal.getTitle());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        
        Label amounts = new Label(String.format("$%.2f / $%.2f", goal.getCurrent(), goal.getTarget()));
        amounts.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #64748b;");
        
        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), goal.getDeadline());
        Label deadline = new Label(daysLeft > 0 ? daysLeft + " days left" : "Overdue");
        deadline.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        meta.getChildren().addAll(amounts, dot, deadline);
        info.getChildren().addAll(titleLabel, meta);
        
        HBox progressBox = new HBox(12);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPrefWidth(140);
        
        ProgressBar miniProgress = new ProgressBar(goal.getProgress() / 100.0);
        miniProgress.setPrefHeight(8);
        miniProgress.setPrefWidth(80);
        miniProgress.setStyle("-fx-accent: #f59e0b;");
        
        Label percent = new Label(String.format("%.0f%%", goal.getProgress()));
        percent.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        progressBox.getChildren().addAll(miniProgress, percent);
        
        item.getChildren().addAll(info, progressBox);
        return item;
    }
    
    private Label createCardTitle(String text, FontAwesomeIcon icon) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18");
        iconView.setStyle("-fx-fill: #3b82f6;");
        
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
        
        Label title = createCardTitle("Spending Comparison", FontAwesomeIcon.LINE_CHART);
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
        
        lineChart.getData().addAll(thisMonth, lastMonth);
        
        card.getChildren().addAll(header, lineChart);
        return card;
    }
    
    private VBox createTransactionsCard() {
        VBox card = createCard();
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = createCardTitle("Recent Transactions", FontAwesomeIcon.HISTORY);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Hyperlink viewAll = new Hyperlink("View All");
        viewAll.setStyle("-fx-font-size: 14px; -fx-text-fill: #3b82f6; -fx-font-weight: 500;");
        
        header.getChildren().addAll(title, viewAll);
        
        VBox transactionsList = new VBox(4);
        
        List<Transaction> transactions = dataStore.getTransactions().stream()
                .sorted((a, b) -> b.getTime().compareTo(a.getTime()))
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
        StackPane icon = createTransactionIcon(tx.getCategory());
        
        // Details
        VBox details = new VBox(3);
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox.setMargin(details, new Insets(0, 14, 0, 14));
        
        Label titleLabel = new Label(tx.getTitle());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #1e293b;");
        
        Label timeLabel = new Label(formatTime(tx.getTime()));
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        details.getChildren().addAll(titleLabel, timeLabel);
        
        // Amount
        String sign = tx.getType().equals("income") ? "+" : "-";
        String color = tx.getType().equals("income") ? "#22c55e" : "#ef4444";
        
        Label amount = new Label(sign + String.format("$%.2f", tx.getAmount()));
        amount.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: " + color + ";");
        
        item.getChildren().addAll(icon, details, amount);
        return item;
    }
    
    private StackPane createTransactionIcon(String category) {
        StackPane pane = new StackPane();
        pane.setPrefSize(44, 44);
        pane.setStyle("-fx-background-radius: 12;");
        
        FontAwesomeIcon icon;
        String bgColor, iconColor;
        
        switch (category) {
            case "food":
                icon = FontAwesomeIcon.CUTLERY;
                bgColor = "#fef3c7";
                iconColor = "#d97706";
                break;
            case "transport":
                icon = FontAwesomeIcon.CAR;
                bgColor = "#dbeafe";
                iconColor = "#2563eb";
                break;
            case "shopping":
                icon = FontAwesomeIcon.SHOPPING_BAG;
                bgColor = "#fce7f3";
                iconColor = "#db2777";
                break;
            case "bills":
                icon = FontAwesomeIcon.FILE_TEXT;
                bgColor = "#fee2e2";
                iconColor = "#dc2626";
                break;
            case "income":
                icon = FontAwesomeIcon.ARROW_DOWN;
                bgColor = "#dcfce7";
                iconColor = "#16a34a";
                break;
            case "entertainment":
                icon = FontAwesomeIcon.FILM;
                bgColor = "#ede9fe";
                iconColor = "#7c3aed";
                break;
            default:
                icon = FontAwesomeIcon.CIRCLE;
                bgColor = "#f1f5f9";
                iconColor = "#64748b";
        }
        
        pane.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12;");
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18");
        iconView.setStyle("-fx-fill: " + iconColor + ";");
        
        pane.getChildren().add(iconView);
        return pane;
    }
    
    private String formatTime(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long hoursDiff = ChronoUnit.HOURS.between(time, now);
        
        if (hoursDiff < 24 && time.toLocalDate().equals(now.toLocalDate())) {
            return "Today, " + time.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (hoursDiff < 48 && time.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday, " + time.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else {
            return time.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }
    
    private void updateBudgetGoal() {
        double budgetLimit = 3000.0;
        LocalDateTime now = LocalDateTime.now();
        
        double totalSpent = dataStore.getTransactions().stream()
                .filter(t -> t.getType().equals("expense"))
                .filter(t -> t.getTime().getMonth() == now.getMonth() && 
                           t.getTime().getYear() == now.getYear())
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double percent = Math.min(100, (totalSpent / budgetLimit) * 100);
        double remaining = Math.max(0, budgetLimit - totalSpent);
        
        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
        goalPercentLabel.setText(String.format("%.0f%%", percent));
        goalProgress.setProgress(percent / 100.0);
        goalHintLabel.setText(String.format("$%.2f remaining this month", remaining));
    }
}
