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
import java.time.YearMonth;
import javafx.scene.layout.GridPane;

public class HabitsView extends VBox{
    
    private final HabitDAO habitDAO = new HabitDAO();
    private final VBox habitListBox = new VBox(10);
    private final TextField nameField = new TextField();
    private int expandedHabitId = -1;
    private YearMonth calendarMonth = YearMonth.now();

    public HabitsView(){
        setSpacing(16);

        Label header = new Label("Habits");
        header.getStyleClass().add("page-header");

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
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addHabit());

        HBox row = new HBox(8, nameField, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.getStyleClass().add("card");
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
            empty.getStyleClass().add("text-muted");
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
        nameLabel.getStyleClass().add("card-title");

        Label streakLabel = new Label(streak > 0 ? "🔥 " + streak + " day streak" : "No streak yet");
        streakLabel.getStyleClass().add(streak > 0 ? "accent-text" : "text-muted");
        streakLabel.setStyle("-fx-font-size: 12px;");

        Button todayBtn = new Button(doneToday ? "✓ Done today" : "Mark done today");
        todayBtn.getStyleClass().add(doneToday ? "button-primary" : "button-secondary");
        todayBtn.setOnAction(e -> {
            if (habitDAO.isLoggedOn(habit.getId(), today)){
                habitDAO.unlogDate(habit.getId(), today);
            }else{
                habitDAO.logDate(habit.getId(), today);
            }
            refresh();
        });

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            habitDAO.delete(habit.getId());
            refresh();
        });

        Button calendarBtn = new Button(expandedHabitId == habit.getId() ? "Hide Calendar" : "Calendar");
        calendarBtn.getStyleClass().add("button-secondary");
        calendarBtn.setOnAction(e -> {
            if (expandedHabitId == habit.getId()){
                expandedHabitId = -1;
            }else {
                expandedHabitId = habit.getId();
                calendarMonth = YearMonth.now();
            }
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, nameLabel, spacer, todayBtn, calendarBtn, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox weekGrid = buildWeekGrid(habit.getId(), today);

        VBox card = new VBox(8, topRow, streakLabel, weekGrid);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        
        if (expandedHabitId == habit.getId()){
            card.getChildren().add(buildMonthCalendar(habit.getId()));
        }
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
            dayLabel.getStyleClass().add("text-muted");
            dayLabel.setStyle("-fx-font-size: 9px;");

            dayBox.getChildren().addAll(dot, dayLabel);
            grid.getChildren().add(dayBox);
        }
        return grid;
    }

    private VBox buildMonthCalendar(int habitId){
        LocalDate monthStart = calendarMonth.atDay(1);
        LocalDate monthEnd = calendarMonth.atEndOfMonth();
        Set<LocalDate> logged = habitDAO.getLoggedDatesInRange(habitId, monthStart, monthEnd);

        Button prevBtn = new Button("<-");
        prevBtn.getStyleClass().add("button-secondary");
        prevBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            refresh();
        });

        Button nextBtn = new Button("->");
        nextBtn.getStyleClass().add("button-secondary");
        nextBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            refresh();
        });

        Label monthLabel = new Label(calendarMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + calendarMonth.getYear());
        monthLabel.getStyleClass().add("text-primary");

        HBox spacer1 = new HBox();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox spacer2 = new HBox();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox navRow = new HBox(8, prevBtn, spacer1, monthLabel, spacer2, nextBtn);
        navRow.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);

        String[] dayHeaders = {"M", "T", "W", "T", "F", "S", "S"};
        for (int col = 0; col < 7; col++){
            Label dayHeader = new Label(dayHeaders[col]);
            dayHeader.getStyleClass().add("text-muted");
            dayHeader.setPrefWidth(28);
            dayHeader.setAlignment(Pos.CENTER);
            grid.add(dayHeader, col, 0);
        }

        int daysInMonth = calendarMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        int row = 1;
        for (int day = 1; day <= daysInMonth; day++){
            LocalDate date = calendarMonth.atDay(day);
            int col = date.getDayOfWeek().getValue() - 1;

            boolean isDone = logged.contains(date);
            boolean isToday = date.equals(today);

            Label cell = new Label(String.valueOf(day));
            cell.setPrefSize(28, 28);
            cell.setAlignment(Pos.CENTER);
            String bg = isDone ? "#2ed573" : "#26262e";
            String textColor = isDone ? "#16161a" : "#8b8b93";
            String border = isToday ? "-fx-border-color: #e8a33d; -fx-border-width: 2; -fx-border-radius: 14;" : "";
            cell.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 14; -fx-font-size: 11px;" + border);
            
            grid.add(cell, col, row);

            if (col == 6){
                row++;
            }
        }
        VBox calendarBox = new VBox(10, navRow, grid);
        calendarBox.setPadding(new Insets(12, 0, 0,  0));
        return calendarBox;
    }
}
