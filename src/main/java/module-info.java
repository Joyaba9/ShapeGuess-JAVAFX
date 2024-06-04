module bryan.shapeguess {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.healthmarketscience.jackcess;


    opens bryan.shapeguess to javafx.fxml;
    exports bryan.shapeguess;
}