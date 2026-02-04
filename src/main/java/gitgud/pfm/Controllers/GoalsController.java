package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Goal;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GoalsController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private VBox mainContent;
    @FXML private Button addGoalButton;
    @FXML private Label totalGoalsLabel;
    @FXML private Label activeGoalsLabel;
    @FXML private Label completedGoalsLabel;
    @FXML private Label totalSavedLabel;
    @FXML private ComboBox<String> sortGoalsCombo;
    @FXML private VBox goalsList;

    private DataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        
        addGoalButton.setOnAction(e -> showAddGoalDialog());
        
        if (sortGoalsCombo != null) {
            sortGoalsCombo.setOnAction(e -> loadGoals());
        }
        
        updateSummary();
        loadGoals();
    }

    private void updateSummary() {
        List<Goal> goals = dataStore.getGoals();
        
        int total = goals.size();
        int completed = (int) goals.stream().filter(g -> g.getBalance() >= g.getTarget()).count();
        int active = total - completed;
        double totalSaved = goals.stream().mapToDouble(Goal::getBalance).sum();
        
        totalGoalsLabel.setText(String.valueOf(total));
        activeGoalsLabel.setText(String.valueOf(active));
        completedGoalsLabel.setText(String.valueOf(completed));
        totalSavedLabel.setText(String.format("$%.2f", totalSaved));
    }

    private void loadGoals() {
        goalsList.getChildren().clear();
        List<Goal> goals = dataStore.getGoals();
        
        // Apply sorting
        String sortBy = sortGoalsCombo != null ? sortGoalsCombo.getValue() : null;
        if (sortBy != null) {
            switch (sortBy) {
                case "Priority":
                    goals = goals.stream()
                            .sorted(Comparator.comparingDouble(Goal::getPriority).reversed())
                            .collect(Collectors.toList());
                    break;
                case "Deadline":
                    goals = goals.stream()
                            .sorted(Comparator.comparing(g -> g.getDeadline() != null ? g.getDeadline() : ""))
                            .collect(Collectors.toList());
                    break;
                case "Progress":
                    goals = goals.stream()
                            .sorted(Comparator.comparingDouble((Goal g) -> g.getTarget() > 0 ? g.getBalance() / g.getTarget() : 0).reversed())
                            .collect(Collectors.toList());
                    break;
                case "Amount":
                    goals = goals.stream()
                            .sorted(Comparator.comparingDouble(Goal::getTarget).reversed())
                            .collect(Collectors.toList());
                    break;
            }
        }

        for (Goal goal : goals) {
            VBox goalCard = createGoalCard(goal);
            goalsList.getChildren().add(goalCard);
        }

        if (goals.isEmpty()) {
            Label emptyLabel = new Label("No goals yet. Click 'Add Goal' to create one.");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            goalsList.getChildren().add(emptyLabel);
        }
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(goal.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priority = new Label("Priority: " + (int) goal.getPriority());
        priority.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label deadline = new Label(goal.getDeadline() != null ? goal.getDeadline() : "No deadline");
        deadline.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        // Edit button with pencil icon
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;"));
        editBtn.setOnAction(e -> showEditGoalDialog(goal));

        titleBox.getChildren().addAll(name, spacer, priority, deadline, editBtn);

        // Progress bar
        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        ProgressBar progressBar = new ProgressBar(Math.min(1.0, progress));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-control-inner-background: #e2e8f0;");
        
        // Color based on progress
        if (progress >= 1.0) {
            progressBar.setStyle("-fx-accent: #22c55e;");
        } else if (progress >= 0.5) {
            progressBar.setStyle("-fx-accent: #3b82f6;");
        } else {
            progressBar.setStyle("-fx-accent: #f59e0b;");
        }

        HBox progressBox = new HBox(12);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBox.getChildren().add(progressBar);

        Label progressLabel = new Label(String.format("$%.2f / $%.2f (%.0f%%)", 
            goal.getBalance(), goal.getTarget(), progress * 100));
        progressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        progressBox.getChildren().add(progressLabel);

        card.getChildren().addAll(titleBox, progressBox);
        return card;
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
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedGoal -> {
            dataStore.updateGoal(updatedGoal);
            refresh();
        });
    }

    @FXML
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

        TextField nameField = new TextField();
        nameField.setPromptText("Goal name");

        TextField targetField = new TextField();
        targetField.setPromptText("Target amount");
        targetField.setText("1000");

        TextField deadlineField = new TextField();
        deadlineField.setPromptText("Deadline (YYYY-MM-DD)");

        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, 5);

        grid.add(new Label("Goal Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Target Amount:"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(deadlineField, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(prioritySpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double target = Double.parseDouble(targetField.getText());
                    return new Goal(nameField.getText(), target, 0,
                            deadlineField.getText(), prioritySpinner.getValue(),
                            java.time.LocalDateTime.now().toString());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid input!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(goal -> {
            dataStore.addGoal(goal);
            refresh();
        });
    }

    public void refresh() {
        updateSummary();
        loadGoals();
    }
}
