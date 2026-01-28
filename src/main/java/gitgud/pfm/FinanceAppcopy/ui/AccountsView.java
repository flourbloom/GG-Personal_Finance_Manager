
package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.data.DataStore;
import gitgud.pfm.FinanceAppcopy.model.Account;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class AccountsView extends ScrollPane {

    private final DataStore dataStore;
    private final VBox mainContent;
    private VBox accountsList;
    private HBox summarySection;

    public AccountsView() {
        dataStore = DataStore.getInstance();

        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");

        HBox header = createHeader();
        summarySection = createSummaryCards();
        VBox accountsCard = createAccountsCard();

        mainContent.getChildren().addAll(header, summarySection, accountsCard);

        setContent(mainContent);
        setFitToWidth(true);
        setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");

        loadAccounts();
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Accounts");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add Account");
        addButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddAccountDialog());

        header.getChildren().addAll(title, spacer, addButton);
        return header;
    }

    private HBox createSummaryCards() {
        HBox summary = new HBox(16);

        List<Account> accounts = dataStore.getAccounts();

        double totalAssets = accounts.stream().filter(a -> a.getBalance() > 0).mapToDouble(Account::getBalance).sum();
        double totalLiabilities = Math.abs(accounts.stream().filter(a -> a.getBalance() < 0).mapToDouble(Account::getBalance).sum());
        double netWorth = totalAssets - totalLiabilities;

        VBox assetsCard = createSummaryCard("Total Assets", totalAssets, "#22c55e");
        VBox liabilitiesCard = createSummaryCard("Total Liabilities", totalLiabilities, "#ef4444");
        VBox netWorthCard = createSummaryCard("Net Worth", netWorth, "#3b82f6");

        HBox.setHgrow(assetsCard, Priority.ALWAYS);
        HBox.setHgrow(liabilitiesCard, Priority.ALWAYS);
        HBox.setHgrow(netWorthCard, Priority.ALWAYS);

        summary.getChildren().addAll(assetsCard, liabilitiesCard, netWorthCard);
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

    private VBox createAccountsCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));

        Label cardTitle = new Label("All Accounts");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        accountsList = new VBox(12);

        card.getChildren().addAll(cardTitle, accountsList);
        return card;
    }

    private void loadAccounts() {
        accountsList.getChildren().clear();
        List<Account> accounts = dataStore.getAccounts();
        for (Account account : accounts) {
            HBox accountCard = createAccountCard(account);
            accountsList.getChildren().add(accountCard);
        }
    }

    private HBox createAccountCard(Account account) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        Region avatar = new Region();
        avatar.setPrefSize(56, 56);
        avatar.setStyle("-fx-background-color: " + account.getColor() + "; -fx-background-radius: 12;");

        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(account.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label type = new Label(account.getType().toUpperCase() + " ACCOUNT");
        type.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");

        details.getChildren().addAll(name, type);

        VBox balanceBox = new VBox(4);
        balanceBox.setAlignment(Pos.CENTER_RIGHT);

        String balanceColor = account.getBalance() >= 0 ? "#22c55e" : "#ef4444";
        Label balance = new Label(String.format("$%.2f", Math.abs(account.getBalance())));
        balance.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + balanceColor + ";");

        Label balanceLabel = new Label(account.getBalance() >= 0 ? "Available" : "Due");
        balanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        balanceBox.getChildren().addAll(balance, balanceLabel);

        card.getChildren().addAll(avatar, details, balanceBox);
        return card;
    }

    private void showAddAccountDialog() {
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("Add New Account");
        dialog.setHeaderText("Enter account details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Account name");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("bank", "cash", "credit");
        typeBox.setValue("bank");

        TextField balanceField = new TextField();
        balanceField.setPromptText("0.00");
        balanceField.setText("0");

        ComboBox<String> colorBox = new ComboBox<>();
        colorBox.getItems().addAll("#3b82f6", "#22c55e", "#ef4444", "#f59e0b", "#8b5cf6", "#ec4899");
        colorBox.setValue("#3b82f6");

        grid.add(new Label("Account Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Account Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Initial Balance:"), 0, 2);
        grid.add(balanceField, 1, 2);
        grid.add(new Label("Color:"), 0, 3);
        grid.add(colorBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double balance = Double.parseDouble(balanceField.getText());
                    int newId = dataStore.getNextAccountId();
                    return new Account(newId, nameField.getText(), typeBox.getValue(), balance, colorBox.getValue());
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid balance!");
                    alert.show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(account -> {
            dataStore.addAccount(account);
            refreshView();
        });
    }

    private void refreshView() {
        loadAccounts();
        mainContent.getChildren().set(1, createSummaryCards());
    }
}
