package com.lifeboard.ui;

import com.lifeboard.dao.TaskDAO;
import com.lifeboard.model.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class TasksView extends VBox {
    
    private final TaskDAO taskDAO = new TaskDAO();
    private final VBox taskListBox = new VBox(8);

    private final TextField titleField = new TextField();
    private final TextField categoryField = new TextField();
    private final ComboBox<String> priorityBox = new ComboBox<>();
    private final DatePicker dueDatePicker = new DatePicker();

    public TasksView(){
        setSpacing(16);

        Label header = new Label("Tasks");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; ");

        VBox form = buildForm();

        ScrollPane scrollPane = new ScrollPane(taskListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, form, scrollPane);
        refresh();
    }

    private VBox buildForm(){
        titleField.setPromptText("Task title");
        titleField.setPrefWidth(200);

        categoryField.setPromptText("Category (optional)");
        categoryField.setPrefWidth(140);

        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue("Medium");

        dueDatePicker.setPromptText("Due date");

        Button addBtn = new Button("Add Task");
        addBtn.setStyle("-fx-background-color: #2ed573; -fx-text-fill: #0f0f1a; -fx-font-weight: bold; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> addTask());

        HBox row = new HBox(8, titleField, categoryField, priorityBox, dueDatePicker, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(row);
        form.setPadding(new Insets(12));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return form;
    }

    private void addTask(){
        String title = titleField.getText().trim();
        if (title.isEmpty()){
            return;
        }
        String category = categoryField.getText().trim();
        int priority = priorityLabelToInt(priorityBox.getValue());
        LocalDate date = dueDatePicker.getValue();
        String dueDate = date != null ? date.toString() : null;

        taskDAO.insert(title, category.isEmpty() ? null : category, priority, dueDate);

        titleField.clear();
        categoryField.clear();
        priorityBox.setValue("Medium");
        dueDatePicker.setValue(null);

        refresh();
    }

    private int priorityLabelToInt(String label){
        return switch (label){
            case "High" -> 1;
            case "Low" -> 3;
            default -> 2;
        };
    }

    private void refresh(){
        taskListBox.getChildren().clear();
        List<Task> tasks = taskDAO.getAll();

        if (tasks.isEmpty()){
            Label empty = new Label("No tasks yet - add one above!");
            empty.setStyle("-fx-text-fill: #999;");
            taskListBox.getChildren().add(empty);
            return;
        }

        for (Task task : tasks){
            taskListBox.getChildren().add(buildTaskRow(task));
        }
    }

    private HBox buildTaskRow(Task task){
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(task.isCompleted());
        checkBox.setOnAction(e -> {
            taskDAO.setCompleted(task.getId(), checkBox.isSelected());
            refresh();
        });

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle(task.isCompleted()
            ? "-fx-strikethrough: true; -fx-text-fill: #999; -fx-font-size: 14px;" : "-fx-font-size: 14px;");
        Label priorityLabel = new Label(task.getPriorityLabel());
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 10; "  + priorityColor(task.getPriority()));

        Label categoryLabel = new Label(task.getCategory() != null ? task.getCategory() : "");
        categoryLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        
        Label dueLabel = new Label(task.getDueDate() != null ? "Due " + task.getDueDate() : "");
        dueLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        Button deleteBtn = new Button("X");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4757; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> {
            taskDAO.delete(task.getId());
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, checkBox, titleLabel, priorityLabel, categoryLabel, spacer, dueLabel, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        return row;
    }

    private String priorityColor(int priority){
        return switch (priority){
            case 1 -> "-fx-background-color: #ffe0e0; -fx-text-fill: #d63031";
            case 3 -> "-fx-background-color: #e0f7e9; -fx-text-fill: #2ed573";
            default -> "-fx-background-color: #fff3cd; -fx-text-fill: #b8860b";
        };
    }
}
