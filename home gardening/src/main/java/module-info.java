module com.example.home_gardening {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.example.home_gardening to javafx.fxml;
    exports com.example.home_gardening;
}
