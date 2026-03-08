package com.example.home_gardening;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class ResultController {

    @FXML
    private Label resultTitle;

    @FXML
    private Label resultValue;

    public void setResult(String title, String value) {

        resultTitle.setText(title);
        resultValue.setText(value);
    }

    @FXML
    public void goToFeatures(ActionEvent event) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/example/home_gardening/features-view.fxml"));

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
