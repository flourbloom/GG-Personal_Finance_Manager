package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Wallet;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AccountsController implements Initializable {

    @FXML private ScrollPane rootPane;
    @FXML private VBox mainContent;
    @FXML private Button addAccountButton;
    @FXML private HBox summarySection;
    @FXML private Label totalAssetsLabel;
    @FXML private Label totalLiabilitiesLabel;
    @FXML private Label netWorthLabel;
    @FXML private VBox accountsList;

    private DataStore dataStore;
    
    // Predefined color options
    private static final String[] COLOR_OPTIONS = {
        "#3b82f6", "#ef4444", "#22c55e", "#f59e0b", "#8b5cf6", 
        "#ec4899", "#06b6d4", "#f97316", "#84cc16", "#6366f1"
    };
    
    private String hexToRgba(String hex, double alpha) {
        // Default to blue if invalid hex
        if (hex == null || !hex.startsWith("#") || hex.length() != 7) {
            return String.format("rgba(59, 130, 246, %.2f)", alpha); // Default blue
        }
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, alpha);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return String.format("rgba(59, 130, 246, %.2f)", alpha); // Default blue
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataStore = DataStore.getInstance();
        
        styleAddAccountButton();
        addAccountButton.setOnAction(e -> showAddAccountDialog());
        
        loadAccounts();
        updateSummary();
        
        // Animate summary cards on load
        animateSummaryCards();
    }
    
    private void styleAddAccountButton() {
        if (addAccountButton == null) return;
        
        // Base style
        addAccountButton.setStyle(
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
        addAccountButton.setOnMouseEntered(e -> {
            addAccountButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 12, 0, 0, 4);"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), addAccountButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        addAccountButton.setOnMouseExited(e -> {
            addAccountButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 12 24;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 8, 0, 0, 2);"
            );
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), addAccountButton);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        // Press animation
        addAccountButton.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), addAccountButton);
            scale.setToX(0.95);
            scale.setToY(0.95);
            scale.play();
        });
        
        addAccountButton.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), addAccountButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
    }
    
    private void animateSummaryCards() {
        if (summarySection == null) return;
        
        int delay = 0;
        for (var node : summarySection.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                card.setOpacity(0);
                card.setTranslateY(20);
                
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, 
                        new KeyValue(card.opacityProperty(), 0),
                        new KeyValue(card.translateYProperty(), 20)
                    ),
                    new KeyFrame(Duration.millis(400), 
                        new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_OUT)
                    )
                );
                timeline.setDelay(Duration.millis(delay));
                timeline.play();
                delay += 100;
            }
        }
    }
    
    private void animateCardEntrance(HBox card, int index) {
        card.setOpacity(0);
        card.setTranslateX(-30);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(card.opacityProperty(), 0),
                new KeyValue(card.translateXProperty(), -30)
            ),
            new KeyFrame(Duration.millis(350),
                new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(card.translateXProperty(), 0, Interpolator.EASE_OUT)
            )
        );
        timeline.setDelay(Duration.millis(index * 80));
        timeline.play();
    }
    
    private void addCardHoverEffect(HBox card, String walletColor) {
        String rgbaLight = hexToRgba(walletColor, 0.08);
        String rgbaMedium = hexToRgba(walletColor, 0.15);
        String rgbaHoverLight = hexToRgba(walletColor, 0.12);
        String rgbaHoverMedium = hexToRgba(walletColor, 0.22);
        
        String baseStyle = 
            "-fx-background-color: linear-gradient(to right, " + rgbaLight + ", " + rgbaMedium + "); " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + walletColor + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-border-radius: 12;";
        
        String hoverStyle = 
            "-fx-background-color: linear-gradient(to right, " + rgbaHoverLight + ", " + rgbaHoverMedium + "); " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + walletColor + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, " + hexToRgba(walletColor, 0.3) + ", 12, 0, 0, 4);";
        
        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
    }

    private void updateSummary() {
        List<Wallet> wallets = dataStore.getWallets();

        double totalBalance = wallets.stream().mapToDouble(Wallet::getBalance).sum();
        double totalAssets = Math.max(0, totalBalance);
        double totalLiabilities = Math.abs(Math.min(0, totalBalance));

        totalAssetsLabel.setText(String.format("$%.2f", totalAssets));
        totalLiabilitiesLabel.setText(String.format("$%.2f", totalLiabilities));
        netWorthLabel.setText(String.format("$%.2f", totalBalance));
    }

    private void loadAccounts() {
        accountsList.getChildren().clear();
        List<Wallet> wallets = dataStore.getWallets();
        
        int index = 0;
        for (Wallet wallet : wallets) {
            HBox accountCard = createAccountCard(wallet);
            accountsList.getChildren().add(accountCard);
            animateCardEntrance(accountCard, index);
            index++;
        }

        if (wallets.isEmpty()) {
            Label emptyLabel = new Label("No accounts yet. Click 'Add Account' to create one.");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
            accountsList.getChildren().add(emptyLabel);
        }
    }

    private HBox createAccountCard(Wallet wallet) {
        String walletColor = wallet.getColor();
        // Validate color is a proper hex color, otherwise default to blue
        if (walletColor == null || !walletColor.startsWith("#") || walletColor.length() != 7) {
            walletColor = "#3b82f6";
        }
        
        // Convert hex to rgba for gradient background
        String rgbaLight = hexToRgba(walletColor, 0.08);
        String rgbaMedium = hexToRgba(walletColor, 0.15);
        
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
            "-fx-background-color: linear-gradient(to right, " + rgbaLight + ", " + rgbaMedium + "); " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: " + walletColor + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-border-radius: 12;"
        );
        
        // Add hover effects
        addCardHoverEffect(card, walletColor);
        
        // Double-click to open edit dialog
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showEditWalletDialog(wallet);
            }
        });
        card.setCursor(javafx.scene.Cursor.HAND);

        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(wallet.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label idLabel = new Label("Account");
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        details.getChildren().addAll(name, idLabel);

        VBox balanceBox = new VBox(4);
        balanceBox.setAlignment(Pos.CENTER_RIGHT);

        String balanceColor = wallet.getBalance() >= 0 ? "#22c55e" : "#ef4444";
        Label balance = new Label(String.format("$%.2f", Math.abs(wallet.getBalance())));
        balance.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + balanceColor + ";");

        Label balanceLabel = new Label(wallet.getBalance() >= 0 ? "Available" : "Due");
        balanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        balanceBox.getChildren().addAll(balance, balanceLabel);

        // Edit button with pencil icon and animation
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; " +
                "-fx-text-fill: #64748b; -fx-padding: 4 8;");
        editBtn.setOnMouseEntered(e -> {
            editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-padding: 4 8; -fx-background-radius: 6;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), editBtn);
            scale.setToX(1.15);
            scale.setToY(1.15);
            scale.play();
        });
        editBtn.setOnMouseExited(e -> {
            editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 4 8;");
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), editBtn);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        editBtn.setOnAction(e -> showEditWalletDialog(wallet));

        card.getChildren().addAll(details, balanceBox, editBtn);
        return card;
    }
    
    private HBox createColorPicker(String selectedColor) {
        HBox colorBox = new HBox(8);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        
        ToggleGroup colorGroup = new ToggleGroup();
        
        for (String color : COLOR_OPTIONS) {
            ToggleButton colorBtn = new ToggleButton();
            colorBtn.setToggleGroup(colorGroup);
            colorBtn.setPrefSize(28, 28);
            colorBtn.setMinSize(28, 28);
            colorBtn.setMaxSize(28, 28);
            colorBtn.setUserData(color);
            
            String baseStyle = "-fx-background-color: " + color + "; -fx-background-radius: 14; " +
                    "-fx-border-radius: 14; -fx-cursor: hand;";
            String selectedStyle = baseStyle + " -fx-border-color: #1e293b; -fx-border-width: 3;";
            
            colorBtn.setStyle(color.equals(selectedColor) ? selectedStyle : baseStyle);
            
            if (color.equals(selectedColor)) {
                colorBtn.setSelected(true);
            }
            
            colorBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    colorBtn.setStyle(selectedStyle);
                    // Bounce animation on selection
                    ScaleTransition bounce = new ScaleTransition(Duration.millis(150), colorBtn);
                    bounce.setFromX(1.0);
                    bounce.setFromY(1.0);
                    bounce.setToX(1.2);
                    bounce.setToY(1.2);
                    bounce.setAutoReverse(true);
                    bounce.setCycleCount(2);
                    bounce.play();
                } else {
                    colorBtn.setStyle(baseStyle);
                }
            });
            
            // Hover animation
            colorBtn.setOnMouseEntered(e -> {
                if (!colorBtn.isSelected()) {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(100), colorBtn);
                    scale.setToX(1.1);
                    scale.setToY(1.1);
                    scale.play();
                }
            });
            colorBtn.setOnMouseExited(e -> {
                if (!colorBtn.isSelected()) {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(100), colorBtn);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.play();
                }
            });
            
            colorBox.getChildren().add(colorBtn);
        }
        
        return colorBox;
    }
    
    private String getSelectedColor(HBox colorPicker, String defaultColor) {
        for (var node : colorPicker.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                if (btn.isSelected()) {
                    return (String) btn.getUserData();
                }
            }
        }
        return defaultColor;
    }

    private void showEditWalletDialog(Wallet wallet) {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Edit Account");
        dialog.setHeaderText("Modify account details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(wallet.getName());
        nameField.setPromptText("Account name");

        String currentColor = wallet.getColor() != null ? wallet.getColor() : "#3b82f6";
        HBox colorPicker = createColorPicker(currentColor);

        TextField balanceField = new TextField(String.valueOf(wallet.getBalance()));
        balanceField.setPromptText("0.00");

        grid.add(new Label("Account Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(new Label("Balance:"), 0, 2);
        grid.add(balanceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setMinWidth(480);

        // Style the delete button
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double balance = Double.parseDouble(balanceField.getText());
                    wallet.setName(nameField.getText());
                    wallet.setColor(getSelectedColor(colorPicker, currentColor));
                    wallet.setBalance(balance);
                    return wallet;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid balance!");
                    alert.show();
                    return null;
                }
            } else if (dialogButton == deleteButtonType) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Account");
                confirm.setHeaderText("Are you sure you want to delete this account?");
                confirm.setContentText("This action cannot be undone.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dataStore.deleteWallet(wallet.getId());
                        dataStore.notifyWalletRefresh();
                        refresh();
                    }
                });
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedWallet -> {
            dataStore.updateWallet(updatedWallet);
            dataStore.notifyWalletRefresh();
            refresh();
        });
    }

    @FXML
    private void showAddAccountDialog() {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Add New Account");
        dialog.setHeaderText("Enter account details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Account name");

        String defaultColor = "#3b82f6";
        HBox colorPicker = createColorPicker(defaultColor);

        TextField balanceField = new TextField();
        balanceField.setPromptText("0.00");
        balanceField.setText("0");

        grid.add(new Label("Account Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);
        grid.add(new Label("Initial Balance:"), 0, 2);
        grid.add(balanceField, 1, 2);

        // Validation error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        grid.add(errorLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(450);

        // Get the Add button and disable it initially
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Validation listener
        Runnable validateFields = () -> {
            String name = nameField.getText().trim();
            String balanceText = balanceField.getText().trim();
            boolean hasColor = getSelectedColor(colorPicker, null) != null;
            
            boolean isValid = !name.isEmpty() && !balanceText.isEmpty() && hasColor;
            
            // Validate balance is a number
            if (!balanceText.isEmpty()) {
                try {
                    Double.parseDouble(balanceText);
                } catch (NumberFormatException e) {
                    isValid = false;
                    errorLabel.setText("Balance must be a valid number");
                    errorLabel.setVisible(true);
                    addButton.setDisable(true);
                    return;
                }
            }
            
            if (name.isEmpty()) {
                errorLabel.setText("Account name is required");
                errorLabel.setVisible(true);
            } else if (balanceText.isEmpty()) {
                errorLabel.setText("Initial balance is required");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
            
            addButton.setDisable(!isValid);
        };

        // Add listeners to fields
        nameField.textProperty().addListener((obs, old, newVal) -> validateFields.run());
        balanceField.textProperty().addListener((obs, old, newVal) -> validateFields.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double balance = Double.parseDouble(balanceField.getText().trim());
                    String selectedColor = getSelectedColor(colorPicker, defaultColor);
                    return new Wallet(selectedColor, balance, nameField.getText().trim());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid balance!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(wallet -> {
            dataStore.addWallet(wallet);
            dataStore.notifyWalletRefresh();
            refresh();
        });
    }

    public void refresh() {
        loadAccounts();
        updateSummary();
    }
}
