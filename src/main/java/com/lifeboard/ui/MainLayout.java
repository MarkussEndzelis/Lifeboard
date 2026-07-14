package com.lifeboard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MainLayout extends BorderPane{
    
    private final VBox contentArea = new VBox();
    private Button activeButton;

    public MainLayout(){
        VBox sidebar = buildSidebar();
        setLeft(sidebar);

        contentArea.setPadding(new Insets(24));
        contentArea.setStyle("-fx-background-color: #f4f5f7;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        setCenter(contentArea);

        showView(new TasksView(), findButtonByText(sidebar, "Tasks"));
    }

    private VBox buildSidebar(){
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: #1e2130");

        Label title = new Label("LifeBoard");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 20, 8));

        Button dashboardBtn = navButton("Dashboard");
        Button tasksBtn = navButton("Tasks");
        Button habitsBtn = navButton("Habits");
        Button budgetBtn = navButton("Budget");

        dashboardBtn.setOnAction(e -> showView(new Label("Dashboard coming soon"), dashboardBtn));
        tasksBtn.setOnAction(e -> showView(new TasksView(), tasksBtn));
        habitsBtn.setOnAction(e -> showView(new Label("Habits coming soon"), habitsBtn));
        budgetBtn.setOnAction(e -> showView(new Label("Budget coming soon"), budgetBtn));

        sidebar.getChildren().addAll(title, dashboardBtn, tasksBtn, habitsBtn, budgetBtn);
        return sidebar;
    }

    private Button navButton(String text){
        Button btn = new Button(text);
        btn.setPrefWidth(156);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 12, 10, 12));
        return btn;
    }

    private String inactiveStyle(){
        return "-fx-background-color: transparent; -fx-text-fill: #c5c8d3; -fx-font-size: 14px; -fx-background-radius: 8;";
    }
    private String activeStyle(){
        return "-fx-background-color: #2ed573; -fx-text-fill: #0f0f1a; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;";
    }

    private void showView(javafx.scene.Node view, Button clicked){
        if (activeButton != null){
            activeButton.setStyle(inactiveStyle());
        }
        if (clicked != null){
            clicked.setStyle(activeStyle());
            activeButton = clicked;
        }
        contentArea.getChildren().setAll(view);
    }

    private Button findButtonByText(VBox sidebar, String text){
        for (var node : sidebar.getChildren()){
            if (node instanceof Button b && b.getText().equals(text)){
                return b;
            }
        }
        return null;
    }
}
