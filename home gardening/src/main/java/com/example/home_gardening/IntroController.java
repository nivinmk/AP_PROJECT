package com.example.home_gardening;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class IntroController {

    public void handleNext(ActionEvent event) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("features-view.fxml"));

        Scene scene = new Scene(loader.load());

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setScene(scene);
        stage.show();
    }
}