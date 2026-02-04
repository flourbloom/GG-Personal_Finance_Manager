package gitgud.pfm.GUI;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Goal;
import gitgud.pfm.Models.Wallet;
import gitgud.pfm.Models.Category;
import gitgud.pfm.services.CategoryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoalsView extends ScrollPane {

    private DataStore dataStore;
    private CategoryService categoryService;
    private VBox mainContent;
    private VBox activeGoalsList;
    private VBox completedGoalsList;

    public GoalsView() {
        dataStore = DataStore.getInstance();
        categoryService = new CategoryService();

        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");

        // Header with Add button
        HBox header = createHeader();

        // Summary cards
        HBox summaryCards = createSummaryCards();

        // Active Goals Section
        VBox activeGoalsCard = createActiveGoalsCard();

        // Completed Goals Section
        VBox completedGoalsCard = createCompletedGoalsCard();

        mainContent.getChildren().addAll(header, summaryCards, activeGoalsCard, completedGoalsCard);

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

        Label subtitle = new Label("Track your savings and financial targets");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        VBox titleBox = new VBox(4);
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Add Goal");
        addButton.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; " +
                        "-fx-background-radius: 10; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddGoalDialog());

        header.getChildren().addAll(titleBox, spacer, addButton);
        return header;
    }

    private HBox createSummaryCards() {
        HBox cards = new HBox(16);

        VBox totalCard = createSummaryCard("Total Goals", "0", "#3b82f6", "📊", "totalGoals");
        VBox inProgressCard = createSummaryCard("In Progress", "0", "#f59e0b", "🎯", "inProgress");
        VBox completedCard = createSummaryCard("Completed", "0", "#10b981", "✅", "completed");
        VBox savedCard = createSummaryCard("Total Saved", "$0.00", "#8b5cf6", "💰", "totalSaved");

        cards.getChildren().addAll(totalCard, inProgressCard, completedCard, savedCard);
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(inProgressCard, Priority.ALWAYS);
        HBox.setHgrow(completedCard, Priority.ALWAYS);
        HBox.setHgrow(savedCard, Priority.ALWAYS);

        return cards;
    }

    private VBox createSummaryCard(String title, String value, String color, String icon, String id) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setId(id + "Card");

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        headerBox.getChildren().addAll(iconLabel, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        valueLabel.setId(id + "Value");

        card.getChildren().addAll(headerBox, valueLabel);
        return card;
    }

    private VBox createActiveGoalsCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Region iconView = new Region();
        iconView.setPrefSize(18, 18);
        iconView.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 4;");

        Label cardTitle = new Label("Active Goals");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        header.getChildren().addAll(iconView, cardTitle);

        activeGoalsList = new VBox(12);

        card.getChildren().addAll(header, activeGoalsList);
        return card;
    }

    private VBox createCompletedGoalsCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Region iconView = new Region();
        iconView.setPrefSize(18, 18);
        iconView.setStyle("-fx-background-color: #10b981; -fx-background-radius: 4;");

        Label cardTitle = new Label("Completed Goals");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        header.getChildren().addAll(iconView, cardTitle);

        completedGoalsList = new VBox(12);

        card.getChildren().addAll(header, completedGoalsList);
        return card;
    }

    private void loadGoals() {
        activeGoalsList.getChildren().clear();
        completedGoalsList.getChildren().clear();

        List<Goal> goals = dataStore.getGoals();

        int totalGoals = goals.size();
        int inProgress = 0;
        int completed = 0;
        double totalSaved = 0;

        for (Goal goal : goals) {
            double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
            totalSaved += goal.getBalance();

            if (progress >= 1.0) {
                completed++;
                VBox goalCard = createGoalCard(goal, true);
                completedGoalsList.getChildren().add(goalCard);
            } else {
                inProgress++;
                VBox goalCard = createGoalCard(goal, false);
                activeGoalsList.getChildren().add(goalCard);
            }
        }

        // Update summary labels
        Label totalLabel = (Label) mainContent.lookup("#totalGoalsValue");
        Label inProgressLabel = (Label) mainContent.lookup("#inProgressValue");
        Label completedLabel = (Label) mainContent.lookup("#completedValue");
        Label savedLabel = (Label) mainContent.lookup("#totalSavedValue");

        if (totalLabel != null)
            totalLabel.setText(String.valueOf(totalGoals));
        if (inProgressLabel != null)
            inProgressLabel.setText(String.valueOf(inProgress));
        if (completedLabel != null)
            completedLabel.setText(String.valueOf(completed));
        if (savedLabel != null)
            savedLabel.setText(String.format("$%.2f", totalSaved));

        if (activeGoalsList.getChildren().isEmpty()) {
            Label emptyLabel = new Label("No active goals. Click '+ Add Goal' to create one!");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            activeGoalsList.getChildren().add(emptyLabel);
        }

        if (completedGoalsList.getChildren().isEmpty()) {
            Label emptyLabel = new Label("No completed goals yet. Keep saving!");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            completedGoalsList.getChildren().add(emptyLabel);
        }
    }

    private VBox createGoalCard(Goal goal, boolean isCompleted) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));

        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        double percentage = Math.min(100, progress * 100);
        double remaining = Math.max(0, goal.getTarget() - goal.getBalance());

        String borderColor = isCompleted ? "#10b981" : (progress >= 0.75 ? "#f59e0b" : "#e2e8f0");
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 12; -fx-border-width: 0 0 0 4;");

        // Header row
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(2);
        Label name = new Label(goal.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label priorityLabel = new Label("Priority: " + (int) goal.getPriority() + "/10");
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        nameBox.getChildren().addAll(name, priorityLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String deadlineText = goal.getDeadline() != null && !goal.getDeadline().isEmpty()
                ? goal.getDeadline()
                : "No deadline";
        Label deadline = new Label("📅 " + deadlineText);
        deadline.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; " +
                "-fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6;");

        Label statusBadge = new Label(isCompleted ? "✅ Completed" : "🎯 In Progress");
        statusBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (isCompleted ? "#10b981" : "#f59e0b") + "; " +
                "-fx-background-color: " + (isCompleted ? "#ecfdf5" : "#fffbeb") + "; " +
                "-fx-padding: 4 8; -fx-background-radius: 6;");

        headerRow.getChildren().addAll(nameBox, spacer, deadline, statusBadge);

        // Progress section
        VBox progressSection = new VBox(8);

        HBox progressHeader = new HBox();
        progressHeader.setAlignment(Pos.CENTER_LEFT);

        Label amountLabel = new Label(String.format("$%.2f of $%.2f", goal.getBalance(), goal.getTarget()));
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #374151;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Label percentLabel = new Label(String.format("%.1f%%", percentage));
        String percentColor = isCompleted ? "#10b981" : (progress >= 0.75 ? "#f59e0b" : "#3b82f6");
        percentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + percentColor + ";");

        progressHeader.getChildren().addAll(amountLabel, spacer2, percentLabel);

        ProgressBar progressBar = new ProgressBar(Math.min(1.0, progress));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressBar.setStyle("-fx-accent: " + percentColor + ";");

        Label remainingLabel = new Label(
                isCompleted ? "Goal achieved! 🎉" : String.format("$%.2f remaining", remaining));
        remainingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        progressSection.getChildren().addAll(progressHeader, progressBar, remainingLabel);

        // Action buttons
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(8, 0, 0, 0));

        if (!isCompleted) {
            Button contributeBtn = new Button("+ Add Contribution");
            contributeBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px; -fx-font-weight: 600; " +
                    "-fx-background-radius: 8; -fx-cursor: hand;");
            contributeBtn.setOnAction(e -> showContributionDialog(goal));
            actionButtons.getChildren().add(contributeBtn);

            Button markCompleteBtn = new Button("Mark Complete");
            markCompleteBtn.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #10b981; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px; -fx-font-weight: 500; " +
                    "-fx-background-radius: 8; -fx-cursor: hand;");
            markCompleteBtn.setOnAction(e -> markGoalAsComplete(goal));
            actionButtons.getChildren().add(markCompleteBtn);
        }

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                "-fx-padding: 8 16; -fx-font-size: 12px; -fx-font-weight: 500; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditGoalDialog(goal));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; " +
                "-fx-padding: 8 16; -fx-font-size: 12px; -fx-font-weight: 500; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> showDeleteConfirmation(goal));

        actionButtons.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(headerRow, progressSection, actionButtons);
        return card;
    }

    private void showAddGoalDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Add New Goal");

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(450);

        Label header = new Label("Create New Goal");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Set up a new savings goal to track your progress");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox headerBox = new VBox(4);
        headerBox.getChildren().addAll(header, subtitle);

        VBox formFields = new VBox(16);

        // Goal name
        VBox nameBox = createFormField("Goal Name");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Emergency Fund, Vacation, New Car...");
        styleTextField(nameField);
        nameBox.getChildren().add(nameField);

        // Target amount
        VBox targetBox = createFormField("Target Amount ($)");
        TextField targetField = new TextField();
        targetField.setPromptText("Enter target amount...");
        styleTextField(targetField);
        targetField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*"))
                targetField.setText(oldVal);
        });
        targetBox.getChildren().add(targetField);

        // Deadline
        VBox deadlineBox = createFormField("Deadline");
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select deadline...");
        deadlinePicker.setMaxWidth(Double.MAX_VALUE);
        deadlineBox.getChildren().add(deadlinePicker);

        // Wallet selector
        VBox accountBox = createFormField("Link to Wallet (optional)");
        ComboBox<String> accountCombo = new ComboBox<>();
        accountCombo.setPromptText("Select wallet...");
        accountCombo.setMaxWidth(Double.MAX_VALUE);
        accountCombo.getItems().add("None");
        for (Wallet wallet : dataStore.getWallets()) {
            accountCombo.getItems().add(wallet.getName());
        }
        accountCombo.setValue("None");
        accountBox.getChildren().add(accountCombo);

        // Category selector
        VBox categoryBox = createFormField("Category (optional)");
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.getItems().add("General Savings");
        for (Category cat : categoryService.getAllCategories()) {
            categoryCombo.getItems().add(cat.getName());
        }
        categoryCombo.setValue("General Savings");
        categoryBox.getChildren().add(categoryCombo);

        // Priority
        VBox priorityBox = createFormField("Priority (1-10)");
        Slider prioritySlider = new Slider(1, 10, 5);
        prioritySlider.setShowTickLabels(true);
        prioritySlider.setShowTickMarks(true);
        prioritySlider.setMajorTickUnit(1);
        prioritySlider.setSnapToTicks(true);

        Label priorityValue = new Label("5");
        priorityValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #3b82f6;");
        prioritySlider.valueProperty().addListener((obs, oldVal, newVal) -> priorityValue.setText(String.valueOf(newVal.intValue())));

        HBox priorityRow = new HBox(12);
        priorityRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(prioritySlider, Priority.ALWAYS);
        priorityRow.getChildren().addAll(prioritySlider, priorityValue);
        priorityBox.getChildren().add(priorityRow);

        formFields.getChildren().addAll(nameBox, targetBox, deadlineBox, accountBox, categoryBox, priorityBox);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button createBtn = new Button("Create Goal");
        createBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        createBtn.setOnAction(e -> {
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a goal name");
                return;
            }
            if (targetField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a target amount");
                return;
            }

            try {
                double target = Double.parseDouble(targetField.getText());
                if (target <= 0) {
                    showAlert("Error", "Target must be greater than 0");
                    return;
                }

                String deadline = deadlinePicker.getValue() != null
                        ? deadlinePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "";
                String createTime = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Goal goal = new Goal(nameField.getText().trim(), target, 0.0, deadline, prioritySlider.getValue(),
                        createTime);
                dataStore.addGoal(goal);
                loadGoals();
                dialogStage.close();
                showSuccess("Goal created successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid target amount");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, createBtn);
        content.getChildren().addAll(headerBox, new Separator(), formFields, buttonBox);

        dialogStage.setScene(new Scene(content));
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void showEditGoalDialog(Goal goal) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Edit Goal");

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(450);

        Label header = new Label("Edit Goal");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        VBox formFields = new VBox(16);

        VBox nameBox = createFormField("Goal Name");
        TextField nameField = new TextField(goal.getName());
        styleTextField(nameField);
        nameBox.getChildren().add(nameField);

        VBox targetBox = createFormField("Target Amount ($)");
        TextField targetField = new TextField(String.valueOf(goal.getTarget()));
        styleTextField(targetField);
        targetField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*"))
                targetField.setText(oldVal);
        });
        targetBox.getChildren().add(targetField);

        VBox deadlineBox = createFormField("Deadline");
        DatePicker deadlinePicker = new DatePicker();
        if (goal.getDeadline() != null && !goal.getDeadline().isEmpty()) {
            try {
                deadlinePicker.setValue(LocalDate.parse(goal.getDeadline()));
            } catch (Exception ex) {
            }
        }
        deadlinePicker.setMaxWidth(Double.MAX_VALUE);
        deadlineBox.getChildren().add(deadlinePicker);

        VBox priorityBox = createFormField("Priority (1-10)");
        Slider prioritySlider = new Slider(1, 10, goal.getPriority());
        prioritySlider.setShowTickLabels(true);
        prioritySlider.setShowTickMarks(true);
        prioritySlider.setMajorTickUnit(1);
        prioritySlider.setSnapToTicks(true);

        Label priorityValue = new Label(String.valueOf((int) goal.getPriority()));
        priorityValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #3b82f6;");
        prioritySlider.valueProperty().addListener((obs, oldVal, newVal) -> priorityValue.setText(String.valueOf(newVal.intValue())));

        HBox priorityRow = new HBox(12);
        priorityRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(prioritySlider, Priority.ALWAYS);
        priorityRow.getChildren().addAll(prioritySlider, priorityValue);
        priorityBox.getChildren().add(priorityRow);

        formFields.getChildren().addAll(nameBox, targetBox, deadlineBox, priorityBox);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        saveBtn.setOnAction(e -> {
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a goal name");
                return;
            }
            try {
                double target = Double.parseDouble(targetField.getText());
                if (target <= 0) {
                    showAlert("Error", "Target must be greater than 0");
                    return;
                }

                goal.setName(nameField.getText().trim());
                goal.setTarget(target);
                goal.setDeadline(deadlinePicker.getValue() != null
                        ? deadlinePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "");
                goal.setPriority((int) prioritySlider.getValue());

                dataStore.updateGoal(goal);
                loadGoals();
                dialogStage.close();
                showSuccess("Goal updated successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid target amount");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        content.getChildren().addAll(header, new Separator(), formFields, buttonBox);

        dialogStage.setScene(new Scene(content));
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void showContributionDialog(Goal goal) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Add Contribution");

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(400);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("💰");
        iconLabel.setStyle("-fx-font-size: 32px;");

        VBox headerText = new VBox(4);
        Label titleLabel = new Label("Add Contribution");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        Label goalLabel = new Label("to " + goal.getName());
        goalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        headerText.getChildren().addAll(titleLabel, goalLabel);
        header.getChildren().addAll(iconLabel, headerText);

        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        double remaining = Math.max(0, goal.getTarget() - goal.getBalance());

        VBox progressInfo = new VBox(8);
        progressInfo.setPadding(new Insets(16));
        progressInfo.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 12;");

        Label currentLabel = new Label(String.format("Current: $%.2f / $%.2f (%.1f%%)",
                goal.getBalance(), goal.getTarget(), progress * 100));
        currentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");

        Label remainingLabel = new Label(String.format("Remaining: $%.2f", remaining));
        remainingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #10b981;");
        progressInfo.getChildren().addAll(currentLabel, remainingLabel);

        VBox amountBox = createFormField("Contribution Amount ($)");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount...");
        styleTextField(amountField);
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*"))
                amountField.setText(oldVal);
        });
        amountBox.getChildren().add(amountField);

        HBox quickAmounts = new HBox(8);
        quickAmounts.setAlignment(Pos.CENTER_LEFT);
        for (double amt : new double[] { 10, 50, 100, 250 }) {
            Button quickBtn = new Button("$" + (int) amt);
            quickBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;");
            quickBtn.setOnAction(e -> amountField.setText(String.valueOf(amt)));
            quickAmounts.getChildren().add(quickBtn);
        }

        Button fillBtn = new Button("Fill ($" + String.format("%.0f", remaining) + ")");
        fillBtn.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #10b981; " +
                "-fx-padding: 8 16; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;");
        fillBtn.setOnAction(e -> amountField.setText(String.format("%.2f", remaining)));
        quickAmounts.getChildren().add(fillBtn);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add Contribution");
        addBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-padding: 12 24; -fx-font-size: 14px; -fx-font-weight: 600; -fx-background-radius: 10;");
        addBtn.setOnAction(e -> {
            if (amountField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter an amount");
                return;
            }
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showAlert("Error", "Amount must be greater than 0");
                    return;
                }

                goal.setBalance(goal.getBalance() + amount);
                dataStore.updateGoal(goal);
                loadGoals();
                dialogStage.close();

                if (goal.getBalance() >= goal.getTarget()) {
                    showSuccess("Congratulations! 🎉 You've reached your goal!");
                } else {
                    showSuccess(String.format("Added $%.2f to %s!", amount, goal.getName()));
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);
        content.getChildren().addAll(header, progressInfo, amountBox, quickAmounts, buttonBox);

        dialogStage.setScene(new Scene(content));
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void showDeleteConfirmation(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Goal");
        alert.setHeaderText("Are you sure you want to delete this goal?");
        alert.setContentText("Goal: " + goal.getName() + "\nProgress: $" +
                String.format("%.2f", goal.getBalance()) + " / $" + String.format("%.2f", goal.getTarget()) +
                "\n\nThis action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataStore.deleteGoal(goal.getId());
                loadGoals();
                showSuccess("Goal deleted successfully!");
            }
        });
    }

    private void markGoalAsComplete(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mark Goal as Complete");
        alert.setHeaderText("Mark this goal as complete?");
        alert.setContentText("Goal: " + goal.getName() + "\n\nThis will set the balance to match the target.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                goal.setBalance(goal.getTarget());
                dataStore.updateGoal(goal);
                loadGoals();
                showSuccess("Congratulations! 🎉 Goal marked as complete!");
            }
        });
    }

    private VBox createFormField(String label) {
        VBox box = new VBox(8);
        Label fieldLabel = new Label(label);
        fieldLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        box.getChildren().add(fieldLabel);
        return box;
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 12 16; -fx-font-size: 14px;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
