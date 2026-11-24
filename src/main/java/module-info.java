module com.mycompany.algoproject {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.algoproject to javafx.fxml;
    exports com.mycompany.algoproject;
}
