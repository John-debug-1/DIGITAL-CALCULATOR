package com.digitalcalculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CalculatorApp extends Application {

    //Εδώ αλλάζει το version
    private static final String VERSION = "1.7.2";

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/calculator.fxml"));
        Scene scene = new Scene(root, 380, 680);
        stage.setTitle("Calculator " + VERSION);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
