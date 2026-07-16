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

        contentArea.setPadding(new Insets(28));
        contentArea.setSpacing(20);
        contentArea.getStyleClass().add("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        setCenter(contentArea);

        showView(new DashboardView(), findButtonByText(sidebar, "Dashboard"));
    }

    private VBox buildSidebar(){
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(24, 12, 20, 12));
        sidebar.setPrefWidth(190);
        sidebar.getStyleClass().add("sidebar");

        Label title = new Label("LifeBoard");
        title.getStyleClass().add("sidebar-title");
        title.setPadding(new Insets(0, 0, 24, 8));

        Button dashboardBtn = navButton("Dashboard");
        Button tasksBtn = navButton("Tasks");
        Button habitsBtn = navButton("Habits");
        Button budgetBtn = navButton("Budget");
        Button journalBtn = navButton("Journal");

        dashboardBtn.setOnAction(e -> showView(new DashboardView(), dashboardBtn));
        tasksBtn.setOnAction(e -> showView(new TasksView(), tasksBtn));
        habitsBtn.setOnAction(e -> showView(new HabitsView(), habitsBtn));
        budgetBtn.setOnAction(e -> showView(new BudgetView(), budgetBtn));
        journalBtn.setOnAction(e -> showView(new JournalView(), journalBtn));

        sidebar.getChildren().addAll(title, dashboardBtn, tasksBtn, habitsBtn, budgetBtn, journalBtn);
        return sidebar;
    }

    private Button navButton(String text){
        Button btn = new Button(text);
        btn.setPrefWidth(166);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 12, 10, 12));
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    private void showView(javafx.scene.Node view, Button clicked){
        if (activeButton != null){
            activeButton.getStyleClass().remove("nav-button-active");
            activeButton.getStyleClass().add("nav-button");
        }
        if (clicked != null){
            clicked.getStyleClass().remove("nav-button");
            clicked.getStyleClass().add("nav-button-active");
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
