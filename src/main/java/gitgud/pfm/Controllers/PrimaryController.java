package gitgud.pfm.Controllers;

import java.io.IOException;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        // This method is no longer used with the new GUI architecture
        System.out.println("Navigation switched to secondary view");
    }

    @FXML
    private void exitProgram() throws IOException {
        System.exit(0);
    }
}
