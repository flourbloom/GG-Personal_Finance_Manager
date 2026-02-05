package gitgud.pfm.GUI;

import gitgud.pfm.GUI.data.DataStore;
import gitgud.pfm.Models.Wallet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class WalletsView extends ScrollPane {

    private final DataStore dataStore;
    private final VBox mainContent;
    private VBox walletsList;
    private HBox summarySection;

    public WalletsView() {
        dataStore = DataStore.getInstance();

        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");

        HBox header = createHeader();
        summarySection = createSummaryCards();
        VBox walletsCard = createWalletsCard();

        mainContent.getChildren().addAll(header, summarySection, walletsCard);

        setContent(mainContent);
        setFitToWidth(true);
        setStyle("-fx-background-color: #f0f2f5; -fx-background: #f0f2f5;");

        loadWallets();
    }

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Wallets");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("Add Wallet");
        addButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        addButton.setOnAction(e -> showAddWalletDialog());

        header.getChildren().addAll(title, spacer, addButton);
        return header;
    }

    private HBox createSummaryCards() {
        HBox summary = new HBox(16);

        List<Wallet> wallets = dataStore.getWallets();

        double totalBalance = wallets.stream().mapToDouble(Wallet::getBalance).sum();
        double totalAssets = Math.max(0, totalBalance);
        double totalLiabilities = Math.abs(Math.min(0, totalBalance));
        double netWorth = totalBalance;

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

    private VBox createWalletsCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 3, 0, 0, 1);");
        card.setPadding(new Insets(24));

        Label cardTitle = new Label("All Wallets");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        walletsList = new VBox(12);

        card.getChildren().addAll(cardTitle, walletsList);
        return card;
    }

    private void loadWallets() {
        walletsList.getChildren().clear();
        List<Wallet> wallets = dataStore.getWallets();
        for (Wallet wallet : wallets) {
            HBox walletCard = createWalletCard(wallet);
            walletsList.getChildren().add(walletCard);
        }
    }

    private HBox createWalletCard(Wallet wallet) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12;");

        Region avatar = new Region();
        avatar.setPrefSize(56, 56);
        avatar.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 12;");

        VBox details = new VBox(4);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(wallet.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        details.getChildren().add(name);

        VBox balanceBox = new VBox(4);
        balanceBox.setAlignment(Pos.CENTER_RIGHT);

        String balanceColor = wallet.getBalance() >= 0 ? "#22c55e" : "#ef4444";
        Label balance = new Label(String.format("$%.2f", Math.abs(wallet.getBalance())));
        balance.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + balanceColor + ";");

        Label balanceLabel = new Label(wallet.getBalance() >= 0 ? "Available" : "Due");
        balanceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        balanceBox.getChildren().addAll(balance, balanceLabel);

        card.getChildren().addAll(avatar, details, balanceBox);
        return card;
    }

    private void showAddWalletDialog() {
        Dialog<Wallet> dialog = new Dialog<>();
        dialog.setTitle("Add New Wallet");
        dialog.setHeaderText("Enter wallet details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Wallet name");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("bank", "cash", "credit");
        typeBox.setValue("bank");

        TextField balanceField = new TextField();
        balanceField.setPromptText("0.00");
        balanceField.setText("0");

        grid.add(new Label("Wallet Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Wallet Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Initial Balance:"), 0, 2);
        grid.add(balanceField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    double balance = Double.parseDouble(balanceField.getText());
                    return new Wallet(nameField.getText(), balance, typeBox.getValue());
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
            refreshView();
        });
    }

    private void refreshView() {
        loadWallets();
        mainContent.getChildren().set(1, createSummaryCards());
    }
}