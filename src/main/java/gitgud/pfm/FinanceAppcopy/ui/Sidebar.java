package gitgud.pfm.FinanceAppcopy.ui;

import gitgud.pfm.FinanceAppcopy.FinanceApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

public class Sidebar extends VBox {
    
    private FinanceApp app;
    private VBox navMenu;
    private NavItem activeItem;
    
    public Sidebar(FinanceApp app) {
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
        
        NavItem dashboard = new NavItem("Dashboard", FontAwesomeIcon.PIE_CHART, () -> app.showDashboard());
        NavItem transactions = new NavItem("Transactions", FontAwesomeIcon.EXCHANGE, () -> app.showTransactions());
        NavItem reports = new NavItem("Reports", FontAwesomeIcon.FILE_ALT, () -> app.showReports());
        NavItem goals = new NavItem("Goals", FontAwesomeIcon.BULLSEYE, () -> app.showGoals());
        NavItem accounts = new NavItem("Accounts", FontAwesomeIcon.UNIVERSITY, () -> app.showAccounts());
        
        navMenu.getChildren().addAll(dashboard, transactions, reports, goals, accounts);
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
        
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.MONEY);
        icon.setSize("24");
        icon.setStyle("-fx-fill: #3b82f6;");
        
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
        
        FontAwesomeIconView settingsIcon = new FontAwesomeIconView(FontAwesomeIcon.COG);
        settingsIcon.setSize("16");
        settingsIcon.setStyle("-fx-fill: #94a3b8; -fx-opacity: 0.6;");
        settingsIcon.setOnMouseEntered(e -> settingsIcon.setStyle("-fx-fill: #94a3b8; -fx-opacity: 1;"));
        settingsIcon.setOnMouseExited(e -> settingsIcon.setStyle("-fx-fill: #94a3b8; -fx-opacity: 0.6;"));
        
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
        private boolean active = false;
        
        public NavItem(String text, FontAwesomeIcon icon, Runnable action) {
            this.text = text;
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(14);
            setPadding(new Insets(14, 16, 14, 16));
            setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
            
            FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
            iconView.setSize("16");
            iconView.setStyle("-fx-fill: #94a3b8;");
            
            Label label = new Label(text);
            label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
            
            getChildren().addAll(iconView, label);
            
            setOnMouseEntered(e -> {
                if (!active) {
                    setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10; -fx-cursor: hand;");
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
                    iconView.setStyle("-fx-fill: white;");
                }
            });

            setOnMouseExited(e -> {
                if (!active) {
                    setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
                    label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
                    iconView.setStyle("-fx-fill: #94a3b8;");
                }
            });
            
            setOnMouseClicked(e -> action.run());
        }
        
        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 10; -fx-cursor: hand;");
                ((Label)getChildren().get(1)).setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 500;");
                ((FontAwesomeIconView)getChildren().get(0)).setStyle("-fx-fill: white;");
            } else {
                setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
                ((Label)getChildren().get(1)).setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 15px;");
                ((FontAwesomeIconView)getChildren().get(0)).setStyle("-fx-fill: #94a3b8;");
            }
        }
        
        public String getText() {
            return text;
        }
    }
}
