package com.example.home_gardening;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class SurvivalController {

    @FXML
    private TextField plantName;

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
    public void predictSurvival() {

        try {

            String plant = plantName.getText();
            String sun = sunlightBox.getValue();
            String water = waterBox.getValue();
            String space = spaceBox.getValue();

            if(plant.isEmpty() || sun == null || water == null || space == null){
                resultLabel.setText("Please fill all inputs!");
                return;
            }

            // Temporary prediction
            String score = "82%";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/home_gardening/result-view.fxml"));

            Scene scene = new Scene(loader.load());

            ResultController controller = loader.getController();
            controller.setResult("Survival Score", score);

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
                    new FXMLLoader(getClass().getResource("features-view.fxml"));

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}