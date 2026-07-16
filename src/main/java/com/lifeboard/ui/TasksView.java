package com.lifeboard.ui;

import com.lifeboard.dao.TaskDAO;
import com.lifeboard.model.Task;
import javafx.scene.layout.Region;

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
    private int editingTaskId = -1;

    private final TextField titleField = new TextField();
    private final TextField categoryField = new TextField();
    private final ComboBox<String> priorityBox = new ComboBox<>();
    private final DatePicker dueDatePicker = new DatePicker();

    public TasksView(){
        setSpacing(16);

        Label header = new Label("Tasks");
        header.getStyleClass().add("page-number");

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
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addTask());

        HBox row = new HBox(8, titleField, categoryField, priorityBox, dueDatePicker, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(row);
        form.setPadding(new Insets(12));
        form.getStyleClass().add("card");
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
            empty.getStyleClass().add("text-muted");
            taskListBox.getChildren().add(empty);
            return;
        }

        for (Task task : tasks){
            if (task.getId() == editingTaskId){
                taskListBox.getChildren().add(buildEditRow(task));
            }else{
                taskListBox.getChildren().add(buildTaskRow(task));
            }
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
        titleLabel.getStyleClass().add(task.isCompleted() ? "text-strikethrough" : "text-primary");
        Label priorityLabel = new Label(task.getPriorityLabel());
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 10; "  + priorityColor(task.getPriority()));

        Label categoryLabel = new Label(task.getCategory() != null ? task.getCategory() : "");
        categoryLabel.getStyleClass().add("text-muted");
        
        Label dueLabel = new Label(task.getDueDate() != null ? "Due " + task.getDueDate() : "");
        dueLabel.getStyleClass().add("text-muted");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-secondary");
        editBtn.setOnAction(e -> {
            editingTaskId = task.getId();
            refresh();
        });

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            taskDAO.delete(task.getId());
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, checkBox, titleLabel, priorityLabel, categoryLabel, spacer, dueLabel, editBtn, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.getStyleClass().add("row-card");
        return row;
    }

    private HBox buildEditRow(Task task){
        TextField editTitle = new TextField(task.getTitle());
        editTitle.setPrefWidth(150);

        TextField editCategory = new TextField(task.getCategory() != null ? task.getCategory() : "");
        editCategory.setPromptText("Category");
        editCategory.setPrefWidth(100);

        ComboBox<String> editPriority = new ComboBox<>();
        editPriority.getItems().addAll("High", "Medium", "Low");
        editPriority.setValue(task.getPriorityLabel());

        DatePicker editDate = new DatePicker();
        if (task.getDueDate() != null){
            editDate.setValue(LocalDate.parse(task.getDueDate()));
        }

        Button saveBtn = new Button("Save");
        saveBtn.setMinWidth(Region.USE_PREF_SIZE);
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            String newTitle = editTitle.getText().trim();
            if(newTitle.isEmpty()){
                return;
            }
            String newCategory = editCategory.getText().trim();
            int newPriority = priorityLabelToInt(editPriority.getValue());
            String newDueDate = editDate.getValue() != null ? editDate.getValue().toString() : null;

            taskDAO.update(task.getId(), newTitle, newCategory.isEmpty() ? null : newCategory, newPriority, newDueDate);
            editingTaskId = -1;
            refresh();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setMinWidth(Region.USE_PREF_SIZE);
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> {
            editingTaskId = -1;
            refresh();
        });

        HBox row = new HBox(8, editTitle, editCategory, editPriority, editDate, saveBtn, cancelBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.getStyleClass().add("row-card");
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
