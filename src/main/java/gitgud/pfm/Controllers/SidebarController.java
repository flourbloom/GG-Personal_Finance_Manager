package gitgud.pfm.Controllers;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Wallet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private VBox sidebarRoot;
    @FXML private HBox logoSection;
    @FXML private VBox navMenu;
    @FXML private HBox dashboardNavItem;
    @FXML private HBox transactionsNavItem;
    @FXML private HBox reportsNavItem;
    @FXML private HBox goalsNavItem;
    @FXML private HBox accountsNavItem;
    @FXML private ComboBox<Wallet> walletSelector;
    @FXML private Label walletBalanceLabel;

    private HBox activeItem;
    private Runnable onDashboardClick;
    private Runnable onTransactionsClick;
    private Runnable onReportsClick;
    private Runnable onGoalsClick;
    private Runnable onAccountsClick;
    private DataStore dataStore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeItem = dashboardNavItem;
        dataStore = DataStore.getInstance();
        
        // Register for wallet refresh notifications
        dataStore.addWalletRefreshListener(this::refreshWallets);
        
        // Setup click handlers
        setupNavItem(dashboardNavItem, "Dashboard");
        setupNavItem(transactionsNavItem, "Transactions");
        setupNavItem(reportsNavItem, "Reports");
        setupNavItem(goalsNavItem, "Goals");
        setupNavItem(accountsNavItem, "Accounts");
        
        setupWalletSelector();
    }
    
    // ⚠️ WARNING: DO NOT MODIFY WALLET SELECTOR STYLING WITHOUT TESTING ⚠️
    // The cell factory styling uses dark background (#1e293b) to match sidebar theme.
    // The dropdown popup background is styled via setStyle on cells.
    private void setupWalletSelector() {
        if (walletSelector != null) {
            // Custom cell factory for dropdown items - show name and balance with DARK THEME
            walletSelector.setCellFactory(lv -> {
                javafx.scene.control.ListCell<Wallet> cell = new javafx.scene.control.ListCell<Wallet>() {
                    @Override
                    protected void updateItem(Wallet wallet, boolean empty) {
                        super.updateItem(wallet, empty);
                        if (empty || wallet == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10);
                            container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            container.setPrefWidth(180);
                            
                            // Wallet name on the LEFT
                            javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(wallet.getName());
                            nameLabel.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 13px; -fx-font-weight: 600;");
                            nameLabel.setMinWidth(80);
                            
                            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                            
                            // Balance on the RIGHT in green
                            javafx.scene.control.Label balanceLabel = new javafx.scene.control.Label(String.format("$%.2f", wallet.getBalance()));
                            String balanceColor = wallet.getBalance() >= 0 ? "#10b981" : "#ef4444";
                            balanceLabel.setStyle("-fx-text-fill: " + balanceColor + "; -fx-font-size: 12px; -fx-font-weight: 700;");
                            
                            container.getChildren().addAll(nameLabel, spacer, balanceLabel);
                            setGraphic(container);
                            setText(null);
                        }
                        // Dark background for dropdown items
                        setStyle("-fx-background-color: #1e293b; -fx-padding: 10 14;");
                    }
                };
                // Hover effect
                cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #334155; -fx-padding: 10 14;"));
                cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: #1e293b; -fx-padding: 10 14;"));
                return cell;
            });
            
            // Custom button cell for selected item display (what shows when dropdown is closed)
            walletSelector.setButtonCell(new javafx.scene.control.ListCell<Wallet>() {
                @Override
                protected void updateItem(Wallet wallet, boolean empty) {
                    super.updateItem(wallet, empty);
                    if (empty || wallet == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(wallet.getName());
                        setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 600; -fx-background-color: transparent;");
                    }
                }
            });
            
            refreshWallets();
            walletSelector.setOnAction(e -> {
                Wallet selected = walletSelector.getValue();
                if (selected != null && walletBalanceLabel != null) {
                    walletBalanceLabel.setText(String.format("$%.2f", selected.getBalance()));
                }
            });
        }
    }
    
    public void refreshWallets() {
        if (walletSelector != null) {
            walletSelector.getItems().clear();
            List<Wallet> wallets = dataStore.getWallets();
            walletSelector.getItems().addAll(wallets);
            if (!wallets.isEmpty()) {
                walletSelector.setValue(wallets.get(0));
                if (walletBalanceLabel != null) {
                    walletBalanceLabel.setText(String.format("$%.2f", wallets.get(0).getBalance()));
                }
            }
        }
    }
    
    public Wallet getSelectedWallet() {
        return walletSelector != null ? walletSelector.getValue() : null;
    }
    
    public void updateSelectedWalletBalance(double newBalance) {
        Wallet selected = getSelectedWallet();
        if (selected != null) {
            selected.setBalance(newBalance);
            if (walletBalanceLabel != null) {
                walletBalanceLabel.setText(String.format("$%.2f", newBalance));
            }
        }
    }

    private void setupNavItem(HBox item, String name) {
        item.setOnMouseEntered(e -> {
            if (item != activeItem) {
                item.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-cursor: hand;");
                Label textLabel = (Label) item.getChildren().get(1);
                textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
            }
        });

        item.setOnMouseExited(e -> {
            if (item != activeItem) {
                item.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
                Label textLabel = (Label) item.getChildren().get(1);
                textLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
            }
        });

        item.setOnMouseClicked(e -> {
            setActiveItem(name);
            switch (name) {
                case "Dashboard":
                    if (onDashboardClick != null) onDashboardClick.run();
                    break;
                case "Transactions":
                    if (onTransactionsClick != null) onTransactionsClick.run();
                    break;
                case "Reports":
                    if (onReportsClick != null) onReportsClick.run();
                    break;
                case "Goals":
                    if (onGoalsClick != null) onGoalsClick.run();
                    break;
                case "Accounts":
                    if (onAccountsClick != null) onAccountsClick.run();
                    break;
            }
        });
    }

    public void setActiveItem(String itemName) {
        // Reset all items
        resetNavItem(dashboardNavItem);
        resetNavItem(transactionsNavItem);
        resetNavItem(reportsNavItem);
        resetNavItem(goalsNavItem);
        resetNavItem(accountsNavItem);

        // Set active item
        HBox item = null;
        switch (itemName) {
            case "Dashboard":
                item = dashboardNavItem;
                break;
            case "Transactions":
                item = transactionsNavItem;
                break;
            case "Reports":
                item = reportsNavItem;
                break;
            case "Goals":
                item = goalsNavItem;
                break;
            case "Accounts":
                item = accountsNavItem;
                break;
        }

        if (item != null) {
            activeItem = item;
            item.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 10; -fx-cursor: hand;");
            Label textLabel = (Label) item.getChildren().get(1);
            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 500;");
        }
    }

    private void resetNavItem(HBox item) {
        item.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
        Label textLabel = (Label) item.getChildren().get(1);
        textLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
    }

    // Navigation action setters
    public void setOnDashboardClick(Runnable action) {
        this.onDashboardClick = action;
    }

    public void setOnTransactionsClick(Runnable action) {
        this.onTransactionsClick = action;
    }

    public void setOnReportsClick(Runnable action) {
        this.onReportsClick = action;
    }

    public void setOnGoalsClick(Runnable action) {
        this.onGoalsClick = action;
    }

    public void setOnAccountsClick(Runnable action) {
        this.onAccountsClick = action;
    }
}
