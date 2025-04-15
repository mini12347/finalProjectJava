module org.openjfx.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires javafx.web;
    requires com.sothawo.mapjfx;
    requires com.google.gson;
    requires org.json;
    requires jdk.jfr;
    requires itextpdf;
    requires com.github.librepdf.openpdf;
    requires java.desktop;
    requires jdk.jsobject;
    opens Entities to javafx.base;
    opens Controllers to javafx.fxml;
    opens org.openjfx.finalproject to javafx.fxml;
    // Ajoutez cette ligne
    exports org.openjfx.finalproject;
}