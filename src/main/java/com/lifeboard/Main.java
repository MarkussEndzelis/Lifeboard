package com.lifeboard;

import com.lifeboard.db.Database;

import javafx.application.Application;
import javafx.scene.Scene;

import javafx.stage.Stage;
import com.lifeboard.ui.MainLayout;;

public class Main extends Application{
    
    @Override
    public void start(Stage primaryStage){
        Database.initialize();

        MainLayout layout = new MainLayout();

        Scene scene = new Scene(layout, 1000, 650);
        primaryStage.setTitle("LifeBoard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
