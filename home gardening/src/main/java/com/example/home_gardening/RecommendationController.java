package com.example.home_gardening;

import javafx.fxml.FXML;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

public class RecommendationController {
    private final ApiService apiService = new ApiService();

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
        String sun = sunlightBox.getValue();
        String water = waterBox.getValue();
        String space = spaceBox.getValue();

        if(sun == null || water == null || space == null){
            resultLabel.setText("Please select all inputs.");
            return;
        }

        resultLabel.setText("Loading recommendations...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                var recommendations = apiService.recommendPlants(water, space, sun);
                if (recommendations == null || recommendations.isEmpty()) {
                    return "No plants found for selected inputs.";
                }
                return String.join(", ", recommendations);
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
