package com.lifeboard.ui;

import com.lifeboard.dao.GoalDAO;
import com.lifeboard.dao.HabitDAO;
import com.lifeboard.dao.TransactionDAO;
import com.lifeboard.model.Goal;
import com.lifeboard.model.Habit;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class GoalsView extends VBox {
    
    private final GoalDAO goalDAO = new GoalDAO();
    private final HabitDAO habitDAO = new HabitDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final VBox goalListBox = new VBox(10);
    private final ChoiceBox<String> linkTypeBox = new ChoiceBox<>();
    private final ComboBox<Habit> habitBox = new ComboBox<>();

    private final TextField titleField = new TextField();
    private final TextField targetField = new TextField();
    private final TextField unitField = new TextField();
    private final DatePicker deadlinePicker = new DatePicker();

    public GoalsView(){
        setSpacing(16);

        Label header = new Label("Goals");
        header.getStyleClass().add("page-header");

        VBox form = buildForm();

        ScrollPane scrollPane = new ScrollPane(goalListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, form, scrollPane);
        refresh();
    }

    private VBox buildForm(){
        titleField.setPromptText("Goal (e.g. Save for vacation)");
        titleField.setPrefWidth(200);

        targetField.setPromptText("Target (e.g. 2000)");
        targetField.setPrefWidth(100);

        unitField.setPromptText("Unit (e.g. $, books)");
        unitField.setPrefWidth(100);

        deadlinePicker.setPromptText("Deadline (optional)");

        linkTypeBox.getItems().addAll("Manual", "Track Budget Balance", "Track Habit Streak");
        linkTypeBox.setValue("Manual");

        habitBox.setPromptText("Which habit?");
        habitBox.setVisible(false);
        habitBox.setManaged(false);

        linkTypeBox.setOnAction(e -> {
            boolean showHabit = "Track Habit Streak".equals(linkTypeBox.getValue());
            habitBox.setVisible(showHabit);
            habitBox.setManaged(showHabit);
            if (showHabit){
                habitBox.getItems().setAll(habitDAO.getAll());
            }
            if ("Track Budget Balance".equals(linkTypeBox.getValue())){
                unitField.setText("$");
            }else if (showHabit){
                unitField.setText("days");
            }
        });

        Button addBtn = new Button("Add Goal");
        addBtn.setMinWidth(Region.USE_PREF_SIZE);
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addGoal());

        HBox row1 = new HBox(8, titleField, targetField, unitField, deadlinePicker, addBtn);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(8, new Label("Link to:"), linkTypeBox, habitBox);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(8, row1, row2);
        form.setPadding(new Insets(12));
        form.getStyleClass().add("card");
        return form;
    }

    private void addGoal(){
        String title = titleField.getText().trim();
        String targetText = targetField.getText().trim();

        if (title.isEmpty() || targetText.isEmpty()){
            return;
        }

        double target;
        try {
            target = Double.parseDouble(targetText);
        }catch (NumberFormatException e){
            return;
        }

        String unit = unitField.getText().trim();
        LocalDate deadline = deadlinePicker.getValue();

        String selectedLink = linkTypeBox.getValue();
        String linkType = "MANUAL";
        Integer linkedHabitId = null;

        if ("Track Budget Balance".equals(selectedLink)){
            linkType = "BUDGET";
        }else if ("Track Habit Streak".equals(selectedLink)){
            Habit selectedHabit = habitBox.getValue();
            if (selectedHabit == null){
                return;
            }
            linkType = "HABIT";
            linkedHabitId = selectedHabit.getId();
        }

        goalDAO.insert(title, target, unit.isEmpty() ? null : unit, deadline != null ? deadline.toString() : null, linkType, linkedHabitId);

        titleField.clear();
        targetField.clear();
        unitField.clear();
        deadlinePicker.setValue(null);
        linkTypeBox.setValue("Manual");
        habitBox.setVisible(false);
        habitBox.setManaged(false);

        refresh();
    }

    private void refresh(){
        goalDAO.syncLinkedGoals(habitDAO, transactionDAO);
        goalListBox.getChildren().clear();
        List<Goal> goals = goalDAO.getAll();

        if (goals.isEmpty()){
            Label empty = new Label("No goals yet - add one above!");
            empty.getStyleClass().add("text-muted");
            goalListBox.getChildren().add(empty);
            return;
        }

        for (Goal goal : goals){
            goalListBox.getChildren().add(buildGoalCard(goal));
        }
    }

    private VBox buildGoalCard(Goal goal){
        Label titleLabel = new Label(goal.getTitle());
        titleLabel.getStyleClass().add(goal.isCompleted() ? "text-strikethrough" : "card-title");

        CheckBox completeBox = new CheckBox("Done");
        completeBox.setSelected(goal.isCompleted());
        completeBox.setOnAction(e -> {
            goalDAO.setCompleted(goal.getId(), completeBox.isSelected());
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            goalDAO.delete(goal.getId());
            refresh();
        });

        HBox topRow = new HBox(10, titleLabel, spacer, completeBox, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String unit = goal.getUnit() != null ? goal.getUnit() : "";
        Label progressLabel = new Label(formatValue(goal.getCurrentValue()) + unit + " / " + formatValue(goal.getTargetValue()) + unit);
        progressLabel.getStyleClass().add("text-muted");

        StackPane progressBar = buildProgressBar(goal.getProgressFraction());

        TextField updateField = new TextField();
        updateField.setPromptText("Update progress...");
        updateField.setPrefWidth(140);
        updateField.setOnAction(e -> updateProgress(goal, updateField));

        Button updateBtn = new Button("Update");
        updateBtn.setMinWidth(Region.USE_PREF_SIZE);
        updateBtn.getStyleClass().add("button-secondary");
        updateBtn.setOnAction(e -> updateProgress(goal, updateField));

        HBox updateRow = new HBox(8, updateField, updateBtn);
        updateRow.setAlignment(Pos.CENTER_LEFT);

        VBox bottomInfo = new VBox(4);
        if (goal.getDeadline() != null){
            Label deadlineLabel = new Label("Deadline: " + goal.getDeadline());
            deadlineLabel.getStyleClass().add("text-muted");
            bottomInfo.getChildren().add(deadlineLabel);
        }

        VBox card;
        if (goal.isLinked()){
            Label linkedBadge = new Label(goal.getLinkType().equals("BUDGET") ? "Synced with Budget" : "Synced with Habit");
            linkedBadge.getStyleClass().add("text-muted");
            card = new VBox(8, topRow, progressLabel, progressBar, linkedBadge, bottomInfo);
        }else {
            card = new VBox(8, topRow, progressLabel, progressBar, updateRow, bottomInfo);
        }
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }

    private void updateProgress(Goal goal, TextField field){
        String text = field.getText().trim();
        if (text.isEmpty()){
            return;
        }
        try {
            double value = Double.parseDouble(text);
            goalDAO.updateProgress(goal.getId(), value);
            field.clear();
            refresh();
        }catch(NumberFormatException e){
            field.clear();
        }
    }

    private StackPane buildProgressBar(double fraction){
        javafx.scene.shape.Rectangle track = new javafx.scene.shape.Rectangle(300, 10);
        track.setArcWidth(10);
        track.setArcHeight(10);
        track.setStyle("-fx-fill: #26262e;");

        javafx.scene.shape.Rectangle fill = new javafx.scene.shape.Rectangle(300 * fraction, 10);
        fill.setArcWidth(10);
        fill.setArcHeight(10);
        fill.setStyle("-fx-fill: #e8a33d;");

        StackPane pane = new StackPane(track, fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        return pane;
    }

    private String formatValue(double value){
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.2f", value);
    }
}
