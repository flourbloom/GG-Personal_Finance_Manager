package gitgud.pfm.GUI;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class Sidebar extends VBox {
    
    public Sidebar() {
        Button homeBtn = new Button("Home");
        Button accountsBtn = new Button("Accounts");
        Button reportsBtn = new Button("Reports");
        
        this.getChildren().addAll(homeBtn, accountsBtn, reportsBtn);
        this.setSpacing(10);
        this.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
    }
}