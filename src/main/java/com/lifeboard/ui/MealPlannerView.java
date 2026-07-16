package com.lifeboard.ui;

import com.lifeboard.dao.GroceryDAO;
import com.lifeboard.dao.MealPlanDAO;
import com.lifeboard.model.GroceryItem;
import com.lifeboard.model.MealEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class MealPlannerView extends VBox{
    
    private final MealPlanDAO mealPlanDAO = new MealPlanDAO();
    private final GroceryDAO groceryDAO = new GroceryDAO();

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] MEAL_TYPES = {"Breakfast", "Launch", "Dinner"};

    private final GridPane mealGrid = new GridPane();
    private final VBox groceryListBox = new VBox(6);
    private final TextField groceryField = new TextField();

    public MealPlannerView(){
        setSpacing(16);

        Label header = new Label("Meal Planner");
        header.getStyleClass().add("page-header");

        VBox mealCard = buildMealCard();
        VBox groceryCard = buildGroceryCard();

        ScrollPane scrollPane = new ScrollPane(new VBox(16, mealCard, groceryCard));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, scrollPane);

        refreshMealGrid();
        refreshGroceryList();
    }

    private VBox buildMealCard(){
        Label title = new Label("This Week");
        title.getStyleClass().add("card-title");

        mealGrid.setHgap(8);
        mealGrid.setVgap(8);

        Label cornerLabel = new Label("");
        mealGrid.add(cornerLabel, 0, 0);

        for (int col = 0; col < DAYS.length; col++){
            Label dayLabel = new Label(DAYS[col].substring(0, 3));
            dayLabel.getStyleClass().add("text-muted");
            dayLabel.setPrefWidth(110);
            dayLabel.setAlignment(Pos.CENTER);
            mealGrid.add(dayLabel, col + 1, 0);
        }

        for (int row = 0; row < MEAL_TYPES.length; row++){
            Label mealTypeLabel = new Label(MEAL_TYPES[row]);
            mealTypeLabel.getStyleClass().add("text-muted");
            mealGrid.add(mealTypeLabel, 0, row + 1);
        }

        VBox card = new VBox(10, title, mealGrid);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("card");
        return card;
    }

    private void refreshMealGrid(){
        List<MealEntry> entries = mealPlanDAO.getAll();
        Map<String, String> lookup = new java.util.HashMap<>();
        for (MealEntry entry : entries){
            lookup.put(entry.getDayOfWeek() + "|" + entry.getMealType(), entry.getMealName());
        }

        for (int row = 0; row < MEAL_TYPES.length; row++){
            for (int col = 0; col < DAYS.length; col++){
                String day = DAYS[col];
                String mealType = MEAL_TYPES[row];
                String key = day + "|" + mealType;
                String existing = lookup.getOrDefault(key, "");

                TextField cellField = new TextField(existing);
                cellField.setPromptText("...");
                cellField.setPrefWidth(110);
                cellField.setOnAction(e -> mealPlanDAO.upsert(day, mealType, cellField.getText().trim()));
                cellField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused){
                        mealPlanDAO.upsert(day, mealType, cellField.getText().trim());
                    }
                });
                mealGrid.add(cellField, col + 1, row + 1);
            }
        }
    }

    private VBox buildGroceryCard(){
        Label title = new Label("Grocery List");
        title.getStyleClass().add("card-title");

        groceryField.setPromptText("Add item...");
        groceryField.setPrefWidth(220);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addGroceryItem());
        groceryField.setOnAction(e -> addGroceryItem());

        Button clearBtn = new Button("Clear Checked");
        clearBtn.getStyleClass().add("button-secondary");
        clearBtn.setOnAction(e -> {
            groceryDAO.clearChecked();
            refreshGroceryList();
        });

        HBox inputRow = new HBox(8, groceryField, addBtn, clearBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, title, inputRow, groceryListBox);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("card");
        return card;
    }

    private void addGroceryItem(){
        String item = groceryField.getText().trim();
        if (item.isEmpty()){
            return;
        }
        groceryDAO.add(item);
        groceryField.clear();
        refreshGroceryList();
    }

    private void refreshGroceryList(){
        groceryListBox.getChildren().clear();
        List<GroceryItem> items = groceryDAO.getAll();

        if (items.isEmpty()){
            Label empty = new Label("No items yet - add one above!");
            empty.getStyleClass().add("text-muted");
            groceryListBox.getChildren().add(empty);
            return;
        }

        for (GroceryItem item : items){
            groceryListBox.getChildren().add(buildGroceryRow(item));
        }
    }

    private HBox buildGroceryRow(GroceryItem item){
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(item.isChecked());
        checkBox.setOnAction(e -> {
            groceryDAO.setChecked(item.getId(), checkBox.isSelected());
            refreshGroceryList();
        });

        Label itemLabel = new Label(item.getItem());
        itemLabel.getStyleClass().add(item.isChecked() ? "text-strikethrough" : "text-primary");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            groceryDAO.delete(item.getId());
            refreshGroceryList();
        });

        HBox row = new HBox(10, checkBox, itemLabel, spacer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.getStyleClass().add("row-card");
        return row;
    }
}
