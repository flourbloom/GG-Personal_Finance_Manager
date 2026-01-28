package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.data.DataStore;
import gitgud.pfm.FinanceAppcopy.model.Goal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
// FontAwesome removed: no icon usage

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GoalsView extends ScrollPane {
    
    private DataStore dataStore;
    private VBox mainContent;
    private VBox goalsList;
    
    public GoalsView() {
        dataStore = DataStore.getInstance();
        
        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        
        // Header
        HBox header = createHeader();
        
        // Goals List
        VBox goalsCard = createGoalsCard();
        
        mainContent.getChildren().addAll(header, goalsCard);
        
        setContent(mainContent);
        setFitToWidth(true);
        setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");
        
        loadGoals();
    }
    
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Financial Goals");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addButton = new Button("Add Goal");
        addButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 24; " +
                          "-fx-background-radius: 8; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddGoalDialog());
        
        header.getChildren().addAll(title, spacer, addButton);
        return header;
    }
    
    private VBox createGoalsCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));
        
        Label cardTitle = new Label("All Goals");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        goalsList = new VBox(16);
        
        card.getChildren().addAll(cardTitle, goalsList);
        return card;
    }
    
    private void loadGoals() {
        goalsList.getChildren().clear();
        
        List<Goal> goals = dataStore.getGoals();
        
        for (Goal goal : goals) {
            VBox goalCard = createGoalCard(goal);
            goalsList.getChildren().add(goalCard);
        }
    }
    
    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        
        String borderColor = goal.isPriority() ? "#f59e0b" : "#e2e8f0";
        String bgColor = goal.isPriority() ? "linear-gradient(to right, #fffbeb, #fef3c7)" : "#f8fafc";
        
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; " +
                     "-fx-border-color: " + borderColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 12;");
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(goal.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // No icon for priority; visual priority uses border/background color
        
        if (goal.getCurrent() >= goal.getTarget()) {
            Label completed = new Label("COMPLETED");
            completed.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e; -fx-font-weight: 700; " +
                             "-fx-background-color: #dcfce7; -fx-padding: 4 12; -fx-background-radius: 12;");
            header.getChildren().add(completed);
        }
        
        header.getChildren().add(0, titleLabel);
        header.getChildren().add(1, spacer);
        
        // Details
        HBox details = new HBox(24);
        details.setAlignment(Pos.CENTER_LEFT);
        
        VBox amountBox = new VBox(4);
        Label amountLabel = new Label("Progress");
        amountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label amount = new Label(String.format("$%.2f / $%.2f", goal.getCurrent(), goal.getTarget()));
        amount.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        amountBox.getChildren().addAll(amountLabel, amount);
        
        VBox categoryBox = new VBox(4);
        Label catLabel = new Label("Category");
        catLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label category = new Label(goal.getCategory().toUpperCase());
        category.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #3b82f6;");
        categoryBox.getChildren().addAll(catLabel, category);
        
        VBox deadlineBox = new VBox(4);
        Label deadLabel = new Label("Deadline");
        deadLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        String deadlineText = daysLeft > 0 ? daysLeft + " days left" : "Overdue";
        String deadlineColor = daysLeft > 0 ? "#1e293b" : "#ef4444";
        Label deadline = new Label(deadlineText);
        deadline.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + deadlineColor + ";");
        deadlineBox.getChildren().addAll(deadLabel, deadline);
        
        details.getChildren().addAll(amountBox, categoryBox, deadlineBox);
        
        // Progress bar
        HBox progressBox = new HBox(12);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        
        ProgressBar progress = new ProgressBar(goal.getProgress() / 100.0);
        progress.setPrefHeight(12);
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setStyle("-fx-accent: " + (goal.isPriority() ? "#f59e0b" : "#3b82f6") + ";");
        
        Label percent = new Label(String.format("%.0f%%", goal.getProgress()));
        percent.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        progressBox.getChildren().addAll(progress, percent);
        
        // Actions
        HBox actions = new HBox(8);
        
        Button addFundsBtn = new Button("Add Funds");
        addFundsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                           "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        addFundsBtn.setOnAction(e -> showAddFundsDialog(goal));
        
        Button togglePriorityBtn = new Button(goal.isPriority() ? "Remove Priority" : "Set Priority");
        togglePriorityBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; " +
                                  "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        togglePriorityBtn.setOnAction(e -> {
            goal.setPriority(!goal.isPriority());
            dataStore.updateGoal(goal);
            loadGoals();
        });
        
        actions.getChildren().addAll(addFundsBtn, togglePriorityBtn);
        
        card.getChildren().addAll(header, details, progressBox, actions);
        return card;
    }
    
    private void showAddGoalDialog() {
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("Add New Goal");
        dialog.setHeaderText("Enter goal details");
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Emergency Fund");
        
        TextField targetField = new TextField();
        targetField.setPromptText("0.00");
        
        TextField currentField = new TextField();
        currentField.setPromptText("0.00");
        currentField.setText("0");
        
        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusMonths(6));
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("emergency", "vacation", "car", "house", "education", "other");
        categoryBox.setValue("other");
        
        CheckBox priorityCheck = new CheckBox("Set as Priority");
        
        grid.add(new Label("Goal Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Target Amount:"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(new Label("Current Amount:"), 0, 2);
        grid.add(currentField, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(deadlinePicker, 1, 3);
        grid.add(new Label("Category:"), 0, 4);
        grid.add(categoryBox, 1, 4);
        grid.add(priorityCheck, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double target = Double.parseDouble(targetField.getText());
                    double current = Double.parseDouble(currentField.getText());
                    int newId = dataStore.getNextGoalId();
                    
                    return new Goal(
                        newId,
                        titleField.getText(),
                        target,
                        current,
                        deadlinePicker.getValue(),
                        categoryBox.getValue(),
                        priorityCheck.isSelected(),
                        LocalDate.now()
                    );
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(goal -> {
            dataStore.addGoal(goal);
            loadGoals();
        });
    }
    
    private void showAddFundsDialog(Goal goal) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Funds");
        dialog.setHeaderText("Add funds to: " + goal.getTitle());
        dialog.setContentText("Amount:");
        
        dialog.showAndWait().ifPresent(response -> {
            try {
                double amount = Double.parseDouble(response);
                goal.setCurrent(goal.getCurrent() + amount);
                dataStore.updateGoal(goal);
                loadGoals();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid amount!");
                alert.show();
            }
        });
    }
}
