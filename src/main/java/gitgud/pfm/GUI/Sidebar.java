package gitgud.pfm.GUI;

import gitgud.pfm.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class Sidebar extends VBox {
    
    private App app;
    private VBox navMenu;
    private NavItem activeItem;
    
    public Sidebar(App app) {
        this.app = app;
        
        setPrefWidth(240);
        setStyle("-fx-background-color: #1e293b;");
        setPadding(new Insets(20, 0, 20, 0));
        
        // Logo section
        HBox logo = createLogo();
        
        // Navigation menu
        navMenu = new VBox(6);
        navMenu.setPadding(new Insets(20, 12, 20, 12));
        VBox.setVgrow(navMenu, Priority.ALWAYS);
        
        // TODO Where in the world is budget???
        NavItem dashboard = new NavItem("Dashboard", "📋", "#3b82f6", () -> app.showDashboard());
        NavItem transactions = new NavItem("Transactions", "⇄", "#a855f7", () -> app.showTransactions());
        NavItem reports = new NavItem("Reports", "📈", "#10b981", () -> app.showReports());
        NavItem goals = new NavItem("Goals", "🚩", "#ef4444", () -> app.showGoals());
        NavItem wallets = new NavItem("Wallets", "👛", "#f59e0b", () -> app.showWallets());
        
        navMenu.getChildren().addAll(dashboard, transactions, reports, goals, wallets);
        activeItem = dashboard;
        dashboard.setActive(true);
        
        // Profile section
        HBox profile = createProfile();
        
        getChildren().addAll(logo, navMenu, profile);
    }
    
    private HBox createLogo() {
        HBox logo = new HBox(12);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(0, 24, 24, 24));
        logo.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");
        
        Region icon = new Region();
        icon.setPrefSize(24, 24);
        icon.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 6;");
        
        Label label = new Label("FinanceApp");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        logo.getChildren().addAll(icon, label);
        return logo;
    }
    
    private HBox createProfile() {
        HBox profile = new HBox(12);
        profile.setAlignment(Pos.CENTER_LEFT);
        profile.setPadding(new Insets(16, 20, 16, 20));
        profile.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12;");
        VBox.setMargin(profile, new Insets(12));
        
        // Avatar placeholder
        Region avatar = new Region();
        avatar.setPrefSize(40, 40);
        avatar.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 20;");
        
        VBox profileInfo = new VBox(2);
        profileInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(profileInfo, Priority.ALWAYS);
        
        Label name = new Label("John Doe");
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600;");
        
        Label email = new Label("john@email.com");
        email.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        
        profileInfo.getChildren().addAll(name, email);
        
        Label settingsIcon = new Label("⚙");
        settingsIcon.setStyle("-fx-text-fill: #94a3b8; -fx-opacity: 0.6; -fx-font-size: 14px;");
        settingsIcon.setOnMouseEntered(e -> settingsIcon.setStyle("-fx-text-fill: #94a3b8; -fx-opacity: 1; -fx-font-size: 14px;"));
        settingsIcon.setOnMouseExited(e -> settingsIcon.setStyle("-fx-text-fill: #94a3b8; -fx-opacity: 0.6; -fx-font-size: 14px;"));
        
        profile.getChildren().addAll(avatar, profileInfo, settingsIcon);
        return profile;
    }
    
    public void setActiveItem(String itemName) {
        for (var node : navMenu.getChildren()) {
            if (node instanceof NavItem) {
                NavItem item = (NavItem) node;
                item.setActive(item.getText().equals(itemName));
                if (item.getText().equals(itemName)) {
                    activeItem = item;
                }
            }
        }
    }
    
    private class NavItem extends HBox {
        private String text;
        private String icon;
        private String iconColor;
        private boolean active = false;
        
        public NavItem(String text, String icon, String iconColor, Runnable action) {
            this.text = text;
            this.icon = icon;
            this.iconColor = iconColor;
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(14);
            setPadding(new Insets(14, 16, 14, 16));
            setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
            
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + iconColor + ";");
            
            Label label = new Label(text);
            label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
            
            getChildren().addAll(iconLabel, label);
            
            setOnMouseEntered(e -> {
                if (!active) {
                    setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-cursor: hand;");
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
                }
            });

            setOnMouseExited(e -> {
                if (!active) {
                    setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
                    label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
                }
            });
            
            setOnMouseClicked(e -> action.run());
        }
        
        public void setActive(boolean active) {
            this.active = active;
            Label iconLabel = (Label)getChildren().get(0);
            Label textLabel = (Label)getChildren().get(1);
            if (active) {
                setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 10; -fx-cursor: hand;");
                textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 500;");
            } else {
                setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
                textLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
            }
        }
        
        public String getText() {
            return text;
        }
    }
}