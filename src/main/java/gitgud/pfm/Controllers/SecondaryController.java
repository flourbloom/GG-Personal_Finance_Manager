package gitgud.pfm.Controllers;

import java.io.IOException;

import gitgud.pfm.App;
import javafx.fxml.FXML;
public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}