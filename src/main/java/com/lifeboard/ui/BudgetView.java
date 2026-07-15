package com.lifeboard.ui;

import com.lifeboard.dao.TransactionDAO;
import com.lifeboard.model.Transaction;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class BudgetView extends VBox{
    
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final VBox transactionListBox = new VBox(8);
    private final VBox summaryBox = new VBox(4);

    private final TextField descField = new TextField();
    private final TextField amountField = new TextField();
    private final TextField categoryField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker();

    public BudgetView(){
        setSpacing(16);

        Label header = new Label("Budget");
        header.getStyleClass().add("page-header");

        summaryBox.setPadding(new Insets(16));
        summaryBox.getStyleClass().add("card");

        VBox form = buildForm();

        ScrollPane scrollPane = new ScrollPane(transactionListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, summaryBox, form, scrollPane);
        refresh();
    }

    private VBox buildForm(){
        descField.setPromptText("Description");
        descField.setPrefWidth(160);

        amountField.setPromptText("Amount");
        amountField.setPrefWidth(90);

        categoryField.setPromptText("Category (optional)");
        categoryField.setPrefWidth(140);

        typeBox.getItems().addAll("Expense", "Income");
        typeBox.setValue("Expense");

        datePicker.setValue(LocalDate.now());

        Button addBtn = new Button("Add Transaction");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setOnAction(e -> addTransaction());

        HBox row = new HBox(8, descField, amountField, categoryField, typeBox, datePicker, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(row);
        form.setPadding(new Insets(12));
        form.getStyleClass().add("card");
        return form;
    }

    private void addTransaction(){
        String desc = descField.getText().trim();
        String amountText = amountField.getText().trim();

        if(desc.isEmpty() || amountText.isEmpty()){
            return;
        }

        double amount;
        try{
            amount = Double.parseDouble(amountText);
        }catch(NumberFormatException e){
            return;
        }

        String category = categoryField.getText().trim();
        String type = typeBox.getValue().equals("Income") ? "income" : "expense";
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();

        transactionDAO.insert(desc, amount, category.isEmpty() ? null : category, type, date.toString());

        descField.clear();
        amountField.clear();
        categoryField.clear();
        typeBox.setValue("Expense");
        datePicker.setValue(LocalDate.now());

        refresh();
    }

    private void refresh(){
        refreshSummary();
        refreshList();
    }

    private void refreshSummary(){
        summaryBox.getChildren().clear();

        LocalDate now = LocalDate.now();
        double monthIncome = transactionDAO.getMonthTotal("income", now);
        double monthExpense = transactionDAO.getMonthTotal("expense", now);
        double allTimeBalance = transactionDAO.getAllTimeBalance();

        Label balanceLabel = new Label(formatMoney(allTimeBalance));
        balanceLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        balanceLabel.getStyleClass().add(allTimeBalance >= 0 ? "success-text" : "danger-text");

        Label balanceCaption = new Label("Total balance");
        balanceCaption.getStyleClass().add("text-muted");

        HBox monthRow = new HBox(24);
        monthRow.setAlignment(Pos.CENTER_LEFT);
        monthRow.setPadding(new Insets(8, 0, 0, 0));

        VBox incomeBox = new VBox(2, labeled("This month income", formatMoney(monthIncome), "success-text"));
        VBox expenseBox = new VBox(2, labeled("This month expenses", formatMoney(monthExpense), "danger-text"));

        monthRow.getChildren().addAll(incomeBox, expenseBox);
        summaryBox.getChildren().addAll(balanceCaption, balanceLabel, monthRow);
    }

    private VBox labeled(String caption, String value, String styleClass){
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("text-muted");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        valueLabel.getStyleClass().add(styleClass);
        return new VBox(2, captionLabel, valueLabel);
    }

    private void refreshList(){
        transactionListBox.getChildren().clear();
        List<Transaction> transactions = transactionDAO.getAll();

        if (transactions.isEmpty()){
            Label empty = new Label("No transactions yet - add one above!");
            empty.getStyleClass().add("text-muted");
            transactionListBox.getChildren().add(empty);
            return;
        }

        for (Transaction t : transactions){
            transactionListBox.getChildren().add(buildTransactionRow(t));
        }
    }

    private HBox buildTransactionRow(Transaction t){
        Label descLabel = new Label(t.getDescription());
        descLabel.getStyleClass().add("text-primary");

        Label categoryLabel = new Label(t.getCategory() != null ? t.getCategory() : "");
        categoryLabel.getStyleClass().add("text-muted");

        Label dateLabel = new Label(t.getTransactionDate());
        dateLabel.getStyleClass().add("text-muted");

        String sign = t.isIncome() ? "+" : "-";
        Label amountLabel = new Label(sign + formatMoney(t.getAmount()));
        amountLabel.setStyle("-fx-font-weight: bold;");
        amountLabel.getStyleClass().add(t.isIncome() ? "success-text" : "danger-text");

        Button deleteBtn = new Button("X");
        deleteBtn.getStyleClass().add("button-icon");
        deleteBtn.setOnAction(e -> {
            transactionDAO.delete(t.getId());
            refresh();
        });

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, descLabel, categoryLabel, spacer, dateLabel, amountLabel, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.getStyleClass().add("row-card");
        return row;
    }

    private String formatMoney(double amount){
        return String.format(Locale.US, "$%.2f", amount);
    }
}
