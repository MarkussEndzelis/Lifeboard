package com.lifeboard;

import com.lifeboard.db.Database;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application{
    
    @Override
    public void start(Stage primaryStage){
        Database.initialize();

        StackPane root = new StackPane();
        root.getChildren().add(new Label("LifeBoard is running!"));

        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setTitle("LifeBoard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
