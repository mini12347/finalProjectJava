module org.openjfx.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    opens Entities to javafx.base;
    opens Controllers to javafx.fxml;
    opens org.openjfx.finalproject to javafx.fxml;
    exports org.openjfx.finalproject;
}