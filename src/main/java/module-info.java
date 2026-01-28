module gitgud.pfm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    requires org.xerial.sqlitejdbc;
    requires de.jensd.fx.glyphs.fontawesome;
    //Open is like a pointer telling the controllers where the fxml is
    // opens gitgud.pfm to javafx.fxml;
    opens gitgud.pfm.Controllers to javafx.fxml;
    opens gitgud.pfm.FinanceAppcopy.model to javafx.base;
    opens gitgud.pfm.FinanceAppcopy.ui to javafx.fxml;
    // TODO if you want to include Models into compilation
    // opens gitgud.pfm.Models to javafx.fxml;
    exports gitgud.pfm;
    exports gitgud.pfm.FinanceAppcopy;
}
