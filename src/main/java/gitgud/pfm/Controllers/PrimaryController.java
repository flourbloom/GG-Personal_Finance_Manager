package gitgud.pfm.Controllers;

import java.io.IOException;
import gitgud.pfm.App;
import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void exitProgram() throws IOException {
        System.exit(0);
    }
}
