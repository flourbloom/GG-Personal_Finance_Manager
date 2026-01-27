package gitgud.pfm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    // Instatiating the scene variable
    private static Scene scene;

    // Abstart start, entry point of the jfx application
    @Override
    public void start(Stage stage) throws IOException {
        // pass in the fxml file to loadFXML method as the root node
        // 640 width, 480 height
        scene = new Scene(loadFXML("primary"), 640, 480);
        
        // setting the title to stage
        stage.setTitle("Personal Finance Manager");

        // setting the scene to stage
        stage.setScene(scene);

        // display the stage
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}