module gitgud.pfm {
    requires javafx.controls;
    requires javafx.fxml;

    opens gitgud.pfm to javafx.fxml;
    exports gitgud.pfm;
}
