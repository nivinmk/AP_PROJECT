package com.example.home_gardening;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class RecommendationController {

    @FXML
    private ComboBox<String> sunlightBox;

    @FXML
    private ComboBox<String> waterBox;

    @FXML
    private ComboBox<String> spaceBox;

    @FXML
    private Label resultLabel;

    @FXML
    public void initialize() {

        sunlightBox.getItems().addAll("Low","Medium","High");
        waterBox.getItems().addAll("Low","Medium","High");
        spaceBox.getItems().addAll("Small","Medium","Large");
    }

    @FXML
    public void recommendPlant() {

        try {

            String sun = sunlightBox.getValue();
            String water = waterBox.getValue();
            String space = spaceBox.getValue();

            if(sun == null || water == null || space == null){
                resultLabel.setText("Please select all inputs!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/home_gardening/result-view.fxml"));

            Scene scene = new Scene(loader.load());

            ResultController controller = loader.getController();
            controller.setResult("Recommended Plant", "Tomato");

            Stage stage = (Stage) resultLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent event) {

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