package com.lifeboard.ui;

import com.lifeboard.dao.HabitDAO;
import com.lifeboard.model.Habit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HabitsView extends VBox{
    
    private final HabitDAO habitDAO = new HabitDAO();
    private final VBox habitListBox = new VBox(10);
    private final TextField nameField = new TextField();

    public HabitsView(){
        setSpacing(16);

        Label header = new Label("Habits");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox form = buildForm();

        ScrollPane scrollPane = new ScrollPane(habitListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, form, scrollPane);
        refresh();
    }

    private HBox buildForm(){
        nameField.setPromptText("New habit name (e.g. Drink water");
        nameField.setPrefWidth(260);

        Button addBtn = new Button("Add Habit");
        addBtn.setStyle("-fx-background-color: #00cfff; -fx-text-fill: #0f0f1a; -fx-font-weight: bold; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> addHabit());

        HBox row = new HBox(8, nameField, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return row;
    }

    private void addHabit(){
        String name = nameField.getText().trim();
        if(name.isEmpty()){
            return;
        }
        habitDAO.insert(name);
        nameField.clear();
        refresh();
    }

    private void refresh(){
        habitListBox.getChildren().clear();
        List<Habit> habits = habitDAO.getAll();

        if(habits.isEmpty()){
            Label empty = new Label("No habits yet - add one above!");
            empty.setStyle("-fx-text-fill: #999;");
            habitListBox.getChildren().add(empty);
            return;
        }

        for(Habit habit : habits){
            habitListBox.getChildren().add(buildHabitCard(habit));
        }
    }

    private VBox buildHabitCard(Habit habit){
        LocalDate today = LocalDate.now();
        boolean doneToday = habitDAO.isLoggedOn(habit.getId(), today);
        int streak = habitDAO.getStreak(habit.getId());

        Label nameLabel = new Label(habit.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label streakLabel = new Label(streak > 0 ? "🔥 " + streak + " day streak" : "No streak yet");
        streakLabel.setStyle(streak > 0 ? "-fx-text-fill: #ff6b35; -fx-font-size: 12px;" : "-fx-text-fill: #999; -fx-font-size: 12px;");

        Button todayBtn = new Button(doneToday ? "✓ Done today" : "Mark done today");
        todayBtn.setStyle(doneToday ? "-fx-background-color: #2ed573; -fx-text-fill: #0f0f1a; -fx-font-weight: bold; -fx-background-radius: 6;" : "-fx-background-color: #e8e8ee; -fx-text-fill: #333; -fx-background-radius: 6;");
        todayBtn.setOnAction(e -> {
            if (habitDAO.isLoggedOn(habit.getId(), today)){
                habitDAO.unlogDate(habit.getId(), today);
            }else{
                habitDAO.logDate(habit.getId(), today);
            }
            refresh();
        });

        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4757; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> {
            habitDAO.delete(habit.getId());
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, nameLabel, spacer, todayBtn, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox weekGrid = buildWeekGrid(habit.getId(), today);

        VBox card = new VBox(8, topRow, streakLabel, weekGrid);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return card;
    }

    private HBox buildWeekGrid(int habitId, LocalDate today){
        LocalDate weekStart = today.minusDays(6);
        Set<LocalDate> logged = habitDAO.getLoggedDatesInRange(habitId, weekStart, today);

        HBox grid = new HBox(6);
        grid.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 7; i++){
            LocalDate day = weekStart.plusDays(i);
            boolean isDone = logged.contains(day);

            VBox dayBox = new VBox(2);
            dayBox.setAlignment(Pos.CENTER);

            Label dot = new Label();
            dot.setPrefSize(20, 20);
            dot.setStyle("-fx-background-radius: 10; -fx-background-color: " + (isDone ? "#2ed573" : "#e0e0e6") + ";");

            Label dayLabel = new Label(day.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.getDefault()));
            dayLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #aaa;");

            dayBox.getChildren().addAll(dot, dayLabel);
            grid.getChildren().add(dayBox);
        }
        return grid;
    }
}
