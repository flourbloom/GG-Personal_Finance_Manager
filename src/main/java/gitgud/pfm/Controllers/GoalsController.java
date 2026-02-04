package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Goal;
<<<<<<< HEAD
import javafx.animation.*;
=======
>>>>>>> 119913d (in the middle of translating fxml)
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
<<<<<<< HEAD
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
=======

import java.net.URL;
>>>>>>> 119913d (in the middle of translating fxml)
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
        
<<<<<<< HEAD
        // Style the add goal button with hover effects
        styleAddGoalButton();
        
=======
>>>>>>> 119913d (in the middle of translating fxml)
        addGoalButton.setOnAction(e -> showAddGoalDialog());
        
        if (sortGoalsCombo != null) {
            sortGoalsCombo.setOnAction(e -> loadGoals());
<<<<<<< HEAD
            styleSortComboBox();
=======
>>>>>>> 119913d (in the middle of translating fxml)
        }
        
        updateSummary();
        loadGoals();
    }

<<<<<<< HEAD
    private void styleAddGoalButton() {
        if (addGoalButton == null) return;
        
        // Base style
        addGoalButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 12 24;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 8, 0, 0, 2);"
        );
        
        // Hover animation
        addGoalButton.setOnMouseEntered(e -> {
            addGoalButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 12, 0, 0, 4);"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), addGoalButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        addGoalButton.setOnMouseExited(e -> {
            addGoalButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 8, 0, 0, 2);"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), addGoalButton);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        // Press animation
        addGoalButton.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), addGoalButton);
            scale.setToX(0.95);
            scale.setToY(0.95);
            scale.play();
        });
        
        addGoalButton.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), addGoalButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
    }

    private void styleSortComboBox() {
        if (sortGoalsCombo == null) return;
        
        sortGoalsCombo.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 8 12;" +
            "-fx-font-size: 14px;"
        );
    }

=======
>>>>>>> 119913d (in the middle of translating fxml)
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
<<<<<<< HEAD
                            .sorted(Comparator.comparing(g -> g.getDeadline() != null ? g.getDeadline() : "9999-12-31"))
=======
                            .sorted(Comparator.comparing(g -> g.getDeadline() != null ? g.getDeadline() : ""))
>>>>>>> 119913d (in the middle of translating fxml)
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
<<<<<<< HEAD
            VBox goalCard = createEnhancedGoalCard(goal);
            goalsList.getChildren().add(goalCard);
            
            // Entrance animation
            animateCardEntrance(goalCard);
        }

        if (goals.isEmpty()) {
            VBox emptyState = createEmptyState();
            goalsList.getChildren().add(emptyState);
        }
    }

    private VBox createEnhancedGoalCard(Goal goal) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        
        // Calculate progress
        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        boolean isCompleted = progress >= 1.0;
        
        // Dynamic gradient based on progress
        String gradientColor = getProgressGradientColor(progress);
        card.setStyle(
            "-fx-background-color: linear-gradient(to right, " + gradientColor + "15, white);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + gradientColor + "40;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
        );

        // Add hover effect
        addCardHoverEffect(card, gradientColor);

        // === HEADER SECTION ===
        HBox header = createCardHeader(goal, gradientColor, isCompleted);
        
        // === PROGRESS SECTION ===
        VBox progressSection = createProgressSection(goal, progress, gradientColor);
        
        // === INFO GRID ===
        GridPane infoGrid = createInfoGrid(goal, progress);
        
        // === FOOTER WITH ACTIONS (NO DETAILS BUTTON) ===
        HBox footer = createCardFooter(goal, gradientColor);

        card.getChildren().addAll(header, progressSection, infoGrid, footer);
        
        return card;
    }

    private HBox createCardHeader(Goal goal, String color, boolean isCompleted) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Goal Icon Circle with animation
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(56, 56);
        iconCircle.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 28;"
        );
        
        // Add pulsing animation for active goals
        if (!isCompleted) {
            addPulseAnimation(iconCircle);
        }
        
        Label iconLabel = new Label(isCompleted ? "✓" : "🎯");
        iconLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");
        iconCircle.getChildren().add(iconLabel);

        // Goal name and category
        VBox nameBox = new VBox(4);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        
        Label name = new Label(goal.getName());
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        name.setWrapText(true);
        
        Label category = new Label("Financial Goal • Priority: " + (int)goal.getPriority());
        category.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        
        nameBox.getChildren().addAll(name, category);

        // Status Badge
        Label statusBadge = createStatusBadge(goal);
        
        // Edit button
        Button editBtn = createStyledButton("✎", color);
        editBtn.setOnAction(e -> showEditGoalDialog(goal));

        header.getChildren().addAll(iconCircle, nameBox, statusBadge, editBtn);
        return header;
    }

    private VBox createProgressSection(Goal goal, double progress, String color) {
        VBox section = new VBox(8);
        
        // Progress labels
        HBox labelsRow = new HBox();
        labelsRow.setAlignment(Pos.CENTER_LEFT);
        
        Label currentLabel = new Label(String.format("$%.2f", goal.getBalance()));
        currentLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        Label separator = new Label(" / ");
        separator.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8;");
        
        Label targetLabel = new Label(String.format("$%.2f", goal.getTarget()));
        targetLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label percentLabel = new Label(String.format("%.1f%%", Math.min(progress * 100, 100)));
        percentLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        labelsRow.getChildren().addAll(currentLabel, separator, targetLabel, spacer, percentLabel);
        
        // Animated Progress Bar
        StackPane progressContainer = new StackPane();
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        progressContainer.setPrefHeight(16);
        
        // Background bar
        Region bgBar = new Region();
        bgBar.setPrefHeight(16);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        bgBar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 8;");
        
        // Progress bar with gradient
        Region progressBar = new Region();
        progressBar.setPrefHeight(16);
        progressBar.setMaxHeight(16);
        
        String progressGradient = String.format(
            "-fx-background-color: linear-gradient(to right, %s, %s90);" +
            "-fx-background-radius: 8;",
            color, color
        );
        progressBar.setStyle(progressGradient);
        
        // Animate progress bar
        animateProgressBar(progressBar, progress);
        
        progressContainer.getChildren().addAll(bgBar, progressBar);
        StackPane.setAlignment(progressBar, Pos.CENTER_LEFT);
        
        section.getChildren().addAll(labelsRow, progressContainer);
        return section;
    }

    private GridPane createInfoGrid(Goal goal, double progress) {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 0, 8, 0));
        
        // Column constraints for even spacing
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col1, col2, col3);
        
        // Remaining amount
        VBox remainingBox = createInfoBox(
            "💰",
            "Remaining",
            String.format("$%.2f", Math.max(0, goal.getTarget() - goal.getBalance())),
            "#f59e0b"
        );
        
        // Deadline info
        VBox deadlineBox = createDeadlineBox(goal);
        
        // Created date
        VBox createdBox = createInfoBox(
            "📅",
            "Created",
            formatDate(goal.getCreateTime()),
            "#3b82f6"
        );
        
        grid.add(remainingBox, 0, 0);
        grid.add(deadlineBox, 1, 0);
        grid.add(createdBox, 2, 0);
        
        return grid;
    }

    private VBox createInfoBox(String icon, String label, String value, String accentColor) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.setStyle(
            "-fx-background-color: " + accentColor + "10;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + accentColor + "30;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );
        
        HBox iconLabelRow = new HBox(6);
        iconLabelRow.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        
        iconLabelRow.getChildren().addAll(iconLabel, textLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        valueLabel.setWrapText(true);
        
        box.getChildren().addAll(iconLabelRow, valueLabel);
        return box;
    }

    private VBox createDeadlineBox(Goal goal) {
        String deadline = goal.getDeadline();
        String displayText;
        String color;
        
        if (deadline == null || deadline.isEmpty()) {
            displayText = "No deadline";
            color = "#94a3b8";
        } else {
            try {
                LocalDate deadlineDate = LocalDate.parse(deadline);
                LocalDate now = LocalDate.now();
                long daysRemaining = ChronoUnit.DAYS.between(now, deadlineDate);
                
                if (daysRemaining < 0) {
                    displayText = "Overdue by " + Math.abs(daysRemaining) + " days";
                    color = "#ef4444";
                } else if (daysRemaining == 0) {
                    displayText = "Due today!";
                    color = "#f59e0b";
                } else if (daysRemaining <= 7) {
                    displayText = daysRemaining + " days left";
                    color = "#f59e0b";
                } else if (daysRemaining <= 30) {
                    displayText = daysRemaining + " days left";
                    color = "#3b82f6";
                } else {
                    displayText = daysRemaining + " days left";
                    color = "#22c55e";
                }
            } catch (Exception e) {
                displayText = deadline;
                color = "#3b82f6";
            }
        }
        
        return createInfoBox("⏰", "Deadline", displayText, color);
    }

    private HBox createCardFooter(Goal goal, String color) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        // Only Contribute button (removed Details button)
        Button contributeBtn = createActionButton("+ Contribute", color, true);
        contributeBtn.setOnAction(e -> showContributeDialog(goal));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Goal ID for reference
        Label idLabel = new Label("ID: " + goal.getId().substring(0, Math.min(8, goal.getId().length())));
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        
        footer.getChildren().addAll(contributeBtn, spacer, idLabel);
        return footer;
    }

    private Button createActionButton(String text, String color, boolean filled) {
        Button btn = new Button(text);
        
        if (filled) {
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 10 20;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            );
            
            btn.setOnMouseEntered(e -> {
                btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 20;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-cursor: hand;" +
                    "-fx-opacity: 0.9;"
                );
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();
            });
            
            btn.setOnMouseExited(e -> {
                btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 20;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-cursor: hand;"
                );
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            });
        }
        
        return btn;
    }

    private Label createStatusBadge(Goal goal) {
        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        
        String text;
        String bgColor;
        String textColor;
        
        if (progress >= 1.0) {
            text = "✓ Completed";
            bgColor = "#22c55e";
            textColor = "white";
        } else if (progress >= 0.75) {
            text = "Almost there!";
            bgColor = "#3b82f6";
            textColor = "white";
        } else if (progress >= 0.5) {
            text = "In Progress";
            bgColor = "#f59e0b";
            textColor = "white";
        } else if (progress >= 0.25) {
            text = "Getting Started";
            bgColor = "#8b5cf6";
            textColor = "white";
        } else {
            text = "Just Started";
            bgColor = "#64748b";
            textColor = "white";
        }
        
        Label badge = new Label(text);
        badge.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 6 12;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;"
        );
        
        return badge;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 8 12;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "15;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 18px;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-padding: 8 12;" +
            "-fx-background-radius: 8;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 8 12;"
        ));
        
        return btn;
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(16);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        
        Label emoji = new Label("🎯");
        emoji.setStyle("-fx-font-size: 64px;");
        
        Label title = new Label("No Goals Yet");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Start your savings journey by creating your first goal!");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        emptyBox.getChildren().addAll(emoji, title, subtitle);
        return emptyBox;
    }

    // === ANIMATION METHODS ===

    private void animateCardEntrance(VBox card) {
        card.setOpacity(0);
        card.setTranslateY(20);
        
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(400), card);
        translate.setFromY(20);
        translate.setToY(0);
        
        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.setDelay(Duration.millis(50));
        parallel.play();
    }

    private void animateProgressBar(Region progressBar, double targetProgress) {
        double finalWidth = targetProgress * 100;
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressBar.prefWidthProperty(), 0)),
            new KeyFrame(Duration.millis(1000), new KeyValue(progressBar.prefWidthProperty(), finalWidth, Interpolator.EASE_OUT))
        );
        timeline.play();
    }

    private void addPulseAnimation(StackPane iconCircle) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(2000), iconCircle);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void addCardHoverEffect(VBox card, String color) {
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: linear-gradient(to right, " + color + "20, white);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, " + color + "40, 16, 0, 0, 6);" +
                "-fx-cursor: hand;"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: linear-gradient(to right, " + color + "15, white);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + color + "40;" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    // === HELPER METHODS ===

    private String getProgressGradientColor(double progress) {
        if (progress >= 1.0) return "#22c55e"; // Green - Completed
        if (progress >= 0.75) return "#3b82f6"; // Blue - Almost there
        if (progress >= 0.5) return "#f59e0b"; // Orange - Halfway
        if (progress >= 0.25) return "#8b5cf6"; // Purple - Getting started
        return "#64748b"; // Gray - Just started
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "N/A";
        try {
            // Try to parse and format the date
            String datePart = dateTime.split(" ")[0];
            LocalDate date = LocalDate.parse(datePart);
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return dateTime;
        }
    }

    // === STYLED DIALOG METHODS ===
    // ⚠️ WARNING: DO NOT MODIFY THIS DIALOG CODE WITHOUT TESTING ⚠️
    // The dialog sizing (setPrefWidth/setPrefHeight) is CRITICAL for proper rendering.
    // Removing or changing these values will cause the dialog to appear broken/glitchy.

    private void showContributeDialog(Goal goal) {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("💰 Contribute to Goal");
        
        // CRITICAL: Set dialog size to prevent glitchy/broken appearance
        dialog.getDialogPane().setPrefWidth(420);
        dialog.getDialogPane().setPrefHeight(380);
        dialog.getDialogPane().setMinWidth(420);
        dialog.getDialogPane().setMinHeight(380);
        
        // Custom header
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(16, 20, 12, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-background-radius: 8 8 0 0;");
        
        Label headerTitle = new Label("Add Money to Your Goal");
        headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;");
        
        Label headerSubtitle = new Label(goal.getName());
        headerSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);
        dialog.getDialogPane().setHeader(headerBox);

        ButtonType addButtonType = new ButtonType("Add Funds", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Progress info card
        VBox progressCard = new VBox(8);
        progressCard.setPadding(new Insets(16));
        progressCard.setStyle(
            "-fx-background-color: #f8fafc;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 10;"
        );
        
        Label currentProgress = new Label(String.format("Current Progress: $%.2f / $%.2f", 
            goal.getBalance(), goal.getTarget()));
        currentProgress.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        double progress = goal.getTarget() > 0 ? goal.getBalance() / goal.getTarget() : 0;
        ProgressBar progressBar = new ProgressBar(Math.min(1.0, progress));
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #3b82f6;");
        
        Label remaining = new Label(String.format("Remaining: $%.2f", 
            Math.max(0, goal.getTarget() - goal.getBalance())));
        remaining.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        
        progressCard.getChildren().addAll(currentProgress, progressBar, remaining);

        // Amount input
        VBox amountBox = new VBox(8);
        Label amountLabel = new Label("Amount to Add");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-padding: 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;"
        );
        
        // Quick amount buttons
        HBox quickAmounts = new HBox(8);
        String[] amounts = {"$25", "$50", "$100", "$250"};
        for (String amt : amounts) {
            Button quickBtn = new Button(amt);
            quickBtn.setStyle(
                "-fx-background-color: #f1f5f9;" +
                "-fx-text-fill: #3b82f6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            );
            quickBtn.setOnMouseEntered(e -> quickBtn.setStyle(
                "-fx-background-color: #3b82f6;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            ));
            quickBtn.setOnMouseExited(e -> quickBtn.setStyle(
                "-fx-background-color: #f1f5f9;" +
                "-fx-text-fill: #3b82f6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 16;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            ));
            quickBtn.setOnAction(e -> amountField.setText(amt.substring(1)));
            quickAmounts.getChildren().add(quickBtn);
        }
        
        amountBox.getChildren().addAll(amountLabel, amountField, quickAmounts);

        content.getChildren().addAll(progressCard, amountBox);
        dialog.getDialogPane().setContent(content);

        // Style buttons
        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addBtn.setStyle(
            "-fx-background-color: #3b82f6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;"
        );
        addBtn.setMinWidth(100);
        addBtn.setMaxWidth(150);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Amount");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter a valid number!");
                    styleAlert(alert);
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(amount -> {
            goal.setBalance(goal.getBalance() + amount);
            dataStore.updateGoal(goal);
            refresh();
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success!");
            success.setHeaderText(null);
            success.setContentText(String.format("Added $%.2f to %s!\n\nNew balance: $%.2f / $%.2f", 
                amount, goal.getName(), goal.getBalance(), goal.getTarget()));
            styleAlert(success);
            success.show();
        });
    }

    private void showEditGoalDialog(Goal goal) {
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("✏️ Edit Goal");
        
        // Custom header
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(20, 20, 10, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #7c3aed); -fx-background-radius: 10 10 0 0;");
        
        Label headerTitle = new Label("Modify Goal Details");
        headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;");
        
        Label headerSubtitle = new Label("Update your goal information");
        headerSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);
        dialog.getDialogPane().setHeader(headerBox);

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete Goal", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        // Styled text fields
        TextField nameField = createStyledTextField(goal.getName(), "Goal name");
        TextField targetField = createStyledTextField(String.valueOf(goal.getTarget()), "Target amount");
        TextField currentField = createStyledTextField(String.valueOf(goal.getBalance()), "Current saved");
        
        // Calendar date picker for deadline
        DatePicker deadlinePicker = new DatePicker();
        if (goal.getDeadline() != null && !goal.getDeadline().isEmpty()) {
            try {
                deadlinePicker.setValue(LocalDate.parse(goal.getDeadline()));
            } catch (Exception e) {
                // If parsing fails, leave it empty
            }
        }
        deadlinePicker.setPromptText("Select deadline");
        deadlinePicker.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8;"
        );

        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, (int) goal.getPriority());
        prioritySpinner.setStyle("-fx-font-size: 14px;");
        prioritySpinner.setPrefWidth(200);

        // Add styled labels
        grid.add(createFieldLabel("Goal Name", "🎯"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createFieldLabel("Target Amount", "💰"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(createFieldLabel("Current Saved", "💵"), 0, 2);
        grid.add(currentField, 1, 2);
        grid.add(createFieldLabel("Deadline", "📅"), 0, 3);
        grid.add(deadlinePicker, 1, 3);
        grid.add(createFieldLabel("Priority (1-10)", "⭐"), 0, 4);
        grid.add(prioritySpinner, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Style buttons
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle(
            "-fx-background-color: #8b5cf6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;"
        );

        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle(
            "-fx-background-color: #fee2e2;" +
            "-fx-text-fill: #dc2626;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;"
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double target = Double.parseDouble(targetField.getText());
                    double current = Double.parseDouble(currentField.getText());
                    goal.setName(nameField.getText());
                    goal.setTarget(target);
                    goal.setBalance(current);
                    goal.setDeadline(deadlinePicker.getValue() != null ? 
                        deadlinePicker.getValue().toString() : null);
                    goal.setPriority(prioritySpinner.getValue());
                    return goal;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter valid numbers for amounts!");
                    styleAlert(alert);
                    alert.show();
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Goal");
                confirm.setHeaderText("Are you sure you want to delete this goal?");
                confirm.setContentText("This action cannot be undone and all progress will be lost.");
                styleAlert(confirm);
                
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
=======
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

        titleBox.getChildren().addAll(name, spacer, priority, deadline);

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
>>>>>>> 119913d (in the middle of translating fxml)
    }

    @FXML
    private void showAddGoalDialog() {
        Dialog<Goal> dialog = new Dialog<>();
<<<<<<< HEAD
        dialog.setTitle("🎯 Create New Goal");
        
        // Custom header with gradient
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(20, 20, 10, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); -fx-background-radius: 10 10 0 0;");
        
        Label headerTitle = new Label("Create a New Savings Goal");
        headerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: white;");
        
        Label headerSubtitle = new Label("Set your financial targets and track your progress");
        headerSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");
        
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);
        dialog.getDialogPane().setHeader(headerBox);

        ButtonType addButtonType = new ButtonType("Create Goal", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        // Styled input fields
        TextField nameField = createStyledTextField("", "e.g., New Car, Vacation, Emergency Fund");
        TextField targetField = createStyledTextField("1000", "e.g., 5000");
        
        // Calendar date picker
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select a target date (optional)");
        deadlinePicker.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 8;"
        );

        Spinner<Integer> prioritySpinner = new Spinner<>(1, 10, 5);
        prioritySpinner.setStyle("-fx-font-size: 14px;");
        prioritySpinner.setPrefWidth(200);

        // Info text
        Label infoLabel = new Label("💡 Tip: Set a realistic deadline and priority to stay motivated!");
        infoLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 8;" +
            "-fx-background-color: #f1f5f9;" +
            "-fx-background-radius: 6;"
        );
        infoLabel.setWrapText(true);

        grid.add(createFieldLabel("Goal Name", "🎯"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createFieldLabel("Target Amount", "💰"), 0, 1);
        grid.add(targetField, 1, 1);
        grid.add(createFieldLabel("Deadline", "📅"), 0, 2);
        grid.add(deadlinePicker, 1, 2);
        grid.add(createFieldLabel("Priority (1-10)", "⭐"), 0, 3);
        grid.add(prioritySpinner, 1, 3);
        grid.add(infoLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Style the create button
        Button createBtn = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        createBtn.setStyle(
            "-fx-background-color: #22c55e;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-font-weight: 600;" +
            "-fx-cursor: hand;"
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    if (nameField.getText().trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Missing Information");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a goal name!");
                        styleAlert(alert);
                        alert.show();
                        return null;
                    }
                    
                    double target = Double.parseDouble(targetField.getText());
                    if (target <= 0) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Amount");
                        alert.setHeaderText(null);
                        alert.setContentText("Target amount must be greater than 0!");
                        styleAlert(alert);
                        alert.show();
                        return null;
                    }
                    
                    String deadline = deadlinePicker.getValue() != null ? 
                        deadlinePicker.getValue().toString() : null;
                    
                    return new Goal(
                        nameField.getText().trim(), 
                        target, 
                        0,
                        deadline,
                        prioritySpinner.getValue(),
                        java.time.LocalDateTime.now().toString()
                    );
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter a valid number for the target amount!");
                    styleAlert(alert);
=======
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
>>>>>>> 119913d (in the middle of translating fxml)
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(goal -> {
            dataStore.addGoal(goal);
            refresh();
<<<<<<< HEAD
            
            // Success notification
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Goal Created!");
            success.setHeaderText(null);
            success.setContentText(String.format("Your goal '%s' has been created!\nTarget: $%.2f", 
                goal.getName(), goal.getTarget()));
            styleAlert(success);
            success.show();
        });
    }

    // Helper methods for styled dialogs
    
    private TextField createStyledTextField(String text, String prompt) {
        TextField field = new TextField(text);
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-padding: 12;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8;"
        );
        field.setPrefWidth(300);
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 12;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: #3b82f6;" +
                    "-fx-border-radius: 8;" +
                    "-fx-border-width: 2;"
                );
            } else {
                field.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 12;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-border-radius: 8;"
                );
            }
        });
        
        return field;
    }
    
    private Label createFieldLabel(String text, String emoji) {
        Label label = new Label(emoji + " " + text);
        label.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1e293b;"
        );
        return label;
    }
    
    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;"
        );
        
        // Style header
        if (alert.getHeaderText() != null && !alert.getHeaderText().isEmpty()) {
            dialogPane.lookup(".header-panel").setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-padding: 16;"
            );
        }
        
        // Style buttons
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            if (button != null) {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    button.setStyle(
                        "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: 600;"
                    );
                } else {
                    button.setStyle(
                        "-fx-background-color: #f1f5f9;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: 600;"
                    );
                }
            }
=======
>>>>>>> 119913d (in the middle of translating fxml)
        });
    }

    public void refresh() {
        updateSummary();
        loadGoals();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 119913d (in the middle of translating fxml)
