package gitgud.pfm.GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AccountsView extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Create a UI control (a Button)
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(event -> {
            System.out.println("Hello World!");
        });

        // 2. Create a layout pane and add the control
        StackPane root = new StackPane();
        root.getChildren().add(btn);

        // 3. Create a scene, add the layout to it, and set dimensions
        Scene scene = new Scene(root, 300, 250);

        // 4. Configure the stage (window) and set the scene
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show(); // Display the window
    }

    public static void main(String[] args) {
        launch(args);
    }
}