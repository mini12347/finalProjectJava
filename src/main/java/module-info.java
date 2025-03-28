module org.openjfx.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    opens Entities to javafx.base;
    opens Controllers to javafx.fxml;
    opens org.openjfx.finalproject to javafx.fxml;
    opens Entities to javafx.base;  // Ajoutez cette ligne
    exports org.openjfx.finalproject;
}