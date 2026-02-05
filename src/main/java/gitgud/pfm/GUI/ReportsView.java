package gitgud.pfm.GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportsView extends StackPane {
    
    public ReportsView() {
        
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28));
        mainContent.setStyle("-fx-background-color: #f0f2f5;");
        
        HBox header = createHeader();
        Label content = new Label("Financial Reports - Coming Soon");
        content.setStyle("-fx-font-size: 18px; -fx-text-fill: #1e293b;");
        
        mainContent.getChildren().addAll(header, content);
        
        getChildren().add(mainContent);
        setStyle("-fx-background-color: #f0f2f5;");
    }
    
    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Financial Reports");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        header.getChildren().addAll(title);
        return header;
    }
}
