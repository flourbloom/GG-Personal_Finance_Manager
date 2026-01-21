package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class DemoController implements Initializable {

    @FXML
    private Button myButton;

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialization if needed
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("Button clicked");
    }
}
