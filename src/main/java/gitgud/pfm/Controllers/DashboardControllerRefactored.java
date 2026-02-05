package gitgud.pfm.Controllers;

import gitgud.pfm.infrastructure.ServiceLocator;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.navigation.NavigationService;
import gitgud.pfm.viewmodels.DashboardViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * REFACTORED DashboardController - Now follows MVC/MVVM principles
 * 
 * CHANGES:
 * - Uses DashboardViewModel for all business logic
 * - Uses NavigationService instead of static callbacks
 * - Uses ServiceLocator for dependency injection
 * - Thin controller - only UI coordination
 * - No direct business logic
 * 
 * SOLID Compliance:
 * - Single Responsibility: Only handles UI coordination
 * - Dependency Inversion: Depends on abstractions (services)
 * - Interface Segregation: Uses only needed services
 */
public class DashboardControllerRefactored implements Initializable {

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

    private DashboardViewModel viewModel;
    private NavigationService navigationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get dependencies from ServiceLocator
        viewModel = ServiceLocator.get(DashboardViewModel.class);
        navigationService = ServiceLocator.get(NavigationService.class);
        
        // Bind UI to ViewModel
        bindViewModel();
        
        // Setup navigation
        setupNavigation();
        
        // Load data
        viewModel.loadData();
        
        // Render UI from ViewModel data
        renderPriorityGoals();
        renderRecentTransactions();
    }
    
    /**
     * Bind UI elements to ViewModel properties
     * This is the essence of MVVM - UI automatically updates when data changes
     */
    private void bindViewModel() {
        // Budget section bindings
        totalSpentLabel.textProperty().bind(viewModel.totalSpentTextProperty());
        budgetLimitLabel.textProperty().bind(viewModel.budgetLimitTextProperty());
        goalPercentLabel.textProperty().bind(viewModel.budgetPercentTextProperty());
        goalProgress.progressProperty().bind(viewModel.budgetProgressProperty());
        goalHintLabel.textProperty().bind(viewModel.budgetHintTextProperty());
    }
    
    /**
     * Setup navigation links
     */
    private void setupNavigation() {
        if (viewAllGoals != null) {
            viewAllGoals.setOnAction(e -> navigationService.navigateTo("goals"));
        }
        
        if (viewAllTransactions != null) {
            viewAllTransactions.setOnAction(e -> navigationService.navigateTo("transactions"));
        }
    }
    
    /**
     * Render priority goals from ViewModel data
     */
    private void renderPriorityGoals() {
        priorityGoalsList.getChildren().clear();
        
        if (viewModel.getPriorityGoals().isEmpty()) {
            Label emptyLabel = new Label("No priority goals");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            priorityGoalsList.getChildren().add(emptyLabel);
            return;
        }
        
        for (Goal goal : viewModel.getPriorityGoals()) {
            HBox goalItem = createPriorityGoalItem(goal);
            priorityGoalsList.getChildren().add(goalItem);
        }
    }
    
    /**
     * Create UI for a single priority goal
     * NOTE: In a more advanced refactor, this would be a custom component
     */
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

        double progressPercent = viewModel.getGoalCompletionPercentage(goal);
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
    
    /**
     * Render recent transactions from ViewModel data
     */
    private void renderRecentTransactions() {
        transactionsList.getChildren().clear();
        
        if (viewModel.getRecentTransactions().isEmpty()) {
            Label emptyLabel = new Label("No recent transactions");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            transactionsList.getChildren().add(emptyLabel);
            return;
        }
        
        for (Transaction transaction : viewModel.getRecentTransactions()) {
            HBox txItem = createTransactionItem(transaction);
            transactionsList.getChildren().add(txItem);
        }
    }
    
    /**
     * Create UI for a single transaction
     */
    private HBox createTransactionItem(Transaction transaction) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label nameLabel = new Label(transaction.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        boolean isIncome = transaction.getIncome() == 1.0;
        String amountText = String.format("$%.2f", transaction.getAmount());
        String amountColor = isIncome ? "#10b981" : "#ef4444";
        
        Label amountLabel = new Label(amountText);
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + amountColor + ";");

        item.getChildren().addAll(nameLabel, amountLabel);
        return item;
    }
    
    /**
     * Refresh all data (called when returning to dashboard)
     */
    public void refresh() {
        viewModel.loadData();
        renderPriorityGoals();
        renderRecentTransactions();
    }
}
