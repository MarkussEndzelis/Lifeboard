package com.lifeboard.ui;

import com.lifeboard.dao.JournalDAO;
import com.lifeboard.model.JournalEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


public class JournalView extends VBox {
    
    private final JournalDAO journalDAO = new JournalDAO();
    private final VBox entryListBox = new VBox(10);

    private final TextArea todayContent = new TextArea();
    private final ComboBox<String> moodBox = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Label saveStatus = new Label();
    private final ImageView todayPhotoPreview = new ImageView();
    private String currentPhotoPath = null;

    public JournalView(){
        setSpacing(16);

        Label header = new Label("Journal");
        header.getStyleClass().add("page-header");

        VBox todayCard = buildTodayCard();
        HBox searchRow = buildSearchRow();

        ScrollPane scrollPane = new ScrollPane(entryListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, todayCard, searchRow, scrollPane);
        loadToday();
        refreshList("");
    }

    private VBox buildTodayCard(){
        Label title = new Label("Today - " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM, d", Locale.ENGLISH)));
        title.getStyleClass().add("card-title");

        moodBox.getItems().addAll("😊 Good", "😐 Okay", "😔 Rough", "🔥 Great", "😴 Tired");
        moodBox.setPromptText("Mood (optional)");

        todayContent.setPromptText("How was today? Whats on your mind...");
        todayContent.setWrapText(true);
        todayContent.setPrefRowCount(4);

        Button photoBtn = new Button("Add Photo");
        photoBtn.getStyleClass().add("button-icon");
        photoBtn.setOnAction(e -> choosePhoto(photoBtn));

        Button removePhotoBtn = new Button("Remove");
        removePhotoBtn.getStyleClass().add("button-icon");
        removePhotoBtn.setOnAction(e -> {
            currentPhotoPath = null;
            todayPhotoPreview.setImage(null);
            todayPhotoPreview.setVisible(false);
            todayPhotoPreview.setManaged(false);
        });

        todayPhotoPreview.setFitWidth(80);
        todayPhotoPreview.setFitHeight(80);
        todayPhotoPreview.setPreserveRatio(true);
        todayPhotoPreview.setVisible(false);
        todayPhotoPreview.setManaged(false);

        Button saveBtn = new Button("Save Entry");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> saveToday());

        HBox controlsRow = new HBox(10, moodBox, photoBtn, removePhotoBtn, saveBtn, saveStatus);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, title, todayContent, todayPhotoPreview, controlsRow);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("card");
        return card;
    }

    private void choosePhoto(Button anchor){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a photo");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Window window = anchor.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null){
            currentPhotoPath = file.getAbsolutePath();
            todayPhotoPreview.setImage(new Image(file.toURI().toString()));
            todayPhotoPreview.setVisible(true);
            todayPhotoPreview.setManaged(true);
        }
    }

    private HBox buildSearchRow(){
        searchField.setPromptText("Search entries...");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshList(newVal));

        Label label = new Label("Past Entries");
        label.getStyleClass().add("card-title");

        Button exportBtn = new Button("Export All");
        exportBtn.getStyleClass().add("button-icon");
        exportBtn.setOnAction(e -> exportEntries(exportBtn));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, label, spacer, exportBtn,  searchField);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void exportEntries(Button anchor){
        List<JournalEntry> entries = journalDAO.getAll();

        if (entries.isEmpty()){
            saveStatus.setText("Nothing to export yet");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Journal");
        chooser.setInitialFileName("journal_export.txt");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text file", "*.txt")  
        );
        Window window = anchor.getScene().getWindow();
        File file = chooser.showSaveDialog(window);
        if (file == null){
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("LifeBoard Journal Export\n");
        sb.append("=========================\n\n");

        for (JournalEntry entry : entries){
            LocalDate date = LocalDate.parse(entry.getEntryDate());
            String formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));

            sb.append(formattedDate).append("\n");
            if (entry.getMood() != null){
                sb.append("Mood: ").append(entry.getMood()).append("\n");
            }
            sb.append(entry.getContent()).append("\n");
            if (entry.getPhotoPath() != null){
                sb.append("[Photo attached: ").append(entry.getPhotoPath()).append("]\n");
            }
            sb.append("\n-------------------------\n\n");
        }

        try (FileWriter writer = new FileWriter(file)){
            writer.write(sb.toString());
            saveStatus.setText("Exported");
        }catch (IOException e){
            e.printStackTrace();
            saveStatus.setText("Export failed");
        }
    }

    private void loadToday(){
        JournalEntry existing = journalDAO.getByDate(LocalDate.now());
        if (existing != null){
            todayContent.setText(existing.getContent());
            if (existing.getMood() != null){
                moodBox.setValue(existing.getMood());
            }
        }
    }

    private void saveToday(){
        String content = todayContent.getText().trim();
        if (content.isEmpty()){
            saveStatus.setText("Write something first");
            return;
        }
        String mood = moodBox.getValue();
        journalDAO.upsert(LocalDate.now(), content, mood, currentPhotoPath);
        refreshList(searchField.getText());
        saveStatus.setText("Saved");
    }

    private void refreshList(String keyword){
        entryListBox.getChildren().clear();

        List<JournalEntry> entries = keyword == null || keyword.isBlank()
            ? journalDAO.getAll()
            : journalDAO.search(keyword);
        
        List<JournalEntry> past = entries.stream()
            .filter(e -> !e.getEntryDate().equals(LocalDate.now().toString()))
            .toList();

        if (past.isEmpty()){
            Label empty = new Label(keyword == null || keyword.isBlank()
                ? "No past entries yet."
                : "No entries match your search.");
            empty.getStyleClass().add("text-muted");
            entryListBox.getChildren().add(empty);
            return;
        }

        for (JournalEntry entry : past){
            entryListBox.getChildren().add(buildEntryCard(entry));
        }
    }

    private VBox buildEntryCard(JournalEntry entry){
        LocalDate date = LocalDate.parse(entry.getEntryDate());
        String formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH));

        Label dateLabel = new Label(formattedDate);
        dateLabel.getStyleClass().add("card-title");

        Label moodLabel = new Label(entry.getMood() != null ? entry.getMood() : "");
        moodLabel.getStyleClass().add("accent-text");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            journalDAO.delete(entry.getId());
            refreshList(searchField.getText());
        });

        HBox topRow = new HBox(10, dateLabel, spacer, moodLabel, deleteBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label contentLabel = new Label(entry.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("text-primary");

        VBox card = new VBox(8, topRow, contentLabel);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }
}
