package com.example.home_gardening;

import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class SurvivalController {
    private final ApiService apiService = new ApiService();

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
        String plant = plantName.getText();
        String sun = sunlightBox.getValue();
        String water = waterBox.getValue();
        String space = spaceBox.getValue();

        if(plant == null || plant.isBlank() || sun == null || water == null || space == null){
            resultLabel.setText("Please fill all inputs.");
            return;
        }

        resultLabel.setText("Predicting...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                double score = apiService.predictSurvival(plant.trim(), water, space, sun);
                return String.format("Survival Score: %.2f%%", score);
            }
        };

        task.setOnSucceeded(event -> resultLabel.setText(task.getValue()));
        task.setOnFailed(event -> resultLabel.setText("Error: " + task.getException().getMessage()));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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
            resultLabel.setText("Navigation error: " + e.getMessage());
        }
    }
}
