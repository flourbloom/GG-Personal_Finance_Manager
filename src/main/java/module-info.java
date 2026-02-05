module gitgud.pfm {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires transitive javafx.graphics;
    requires org.xerial.sqlitejdbc;
    
    //Open is like a pointer telling the controllers where the fxml is
    opens gitgud.pfm.Controllers to javafx.fxml;
    opens gitgud.pfm.Models to javafx.base;
    
    exports gitgud.pfm;
    exports gitgud.pfm.Controllers;
    exports gitgud.pfm.Models;
    exports gitgud.pfm.services;
}
