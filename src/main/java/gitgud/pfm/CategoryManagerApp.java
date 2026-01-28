package gitgud.pfm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Standalone Category Manager App
 */
public class CategoryManagerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("category"), 640, 480);
        stage.setTitle("Category Manager");
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CategoryManagerApp.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
