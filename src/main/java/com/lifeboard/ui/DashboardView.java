package com.lifeboard.ui;

import com.lifeboard.dao.HabitDAO;
import com.lifeboard.dao.TaskDAO;
import com.lifeboard.dao.TransactionDAO;
import com.lifeboard.model.Habit;
import com.lifeboard.model.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardView extends VBox{
    
    private final TaskDAO taskDAO = new TaskDAO();
    private final HabitDAO habitDAO = new HabitDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public DashboardView(){
        setSpacing(20);

        getChildren().addAll(
            buildHeader(),
            buildBudgetCard(),
            buildTasksCard(),
            buildHabitsCard()
        );
    }

    private VBox buildHeader(){
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH));

        Label welcome = new Label("Welcome back 👋");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label dateLabel = new Label(today);
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");

        return new VBox(2, welcome, dateLabel);
    }

    private VBox buildBudgetCard(){
        double balance = transactionDAO.getAllTimeBalance();
        double monthIncome = transactionDAO.getMonthTotal("income", LocalDate.now());
        double monthExpense = transactionDAO.getMonthTotal("expense", LocalDate.now());

        Label title = new Label("Budget");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label balanceLabel = new Label(formatMoney(balance));
        balanceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: "
            + (balance >= 0 ? "#2ed573" : "#ff4757") + ";");

        Label detail = new Label("This month: +" + formatMoney(monthIncome) + " / -" + formatMoney(monthExpense));
        detail.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        VBox card = new VBox(4, title, balanceLabel, detail);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return card;
    }

    private VBox buildTasksCard(){
        List<Task> allTasks = taskDAO.getAll();
        List<Task> incomplete = allTasks.stream().filter(t -> !t.isCompleted()).limit(5).toList();

        Label title = new Label("Upcoming Tasks");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox list = new VBox(6);
        if(incomplete.isEmpty()){
            Label empty = new Label("No pending tasks - nice work!");
            empty.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            list.getChildren().add(empty);
        }else{
            for (Task t : incomplete){
                list.getChildren().add(buildTaskRow(t));
            }
        }

        VBox card = new VBox(10, title, list);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return card;
    }

    private HBox buildTaskRow(Task t){
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + priorityColor(t.getPriority()) + "; -fx-font-size: 10px;");

        Label titleLabel = new Label(t.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dueLabel = new Label(t.getDueDate() != null ? "Due " + t.getDueDate() : "");
        dueLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");

        HBox row = new HBox(8, dot, titleLabel, spacer, dueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String priorityColor(int priority){
        return switch (priority){
            case 1 -> "#ff4757";
            case 3 -> "#2ed573";
            default -> "#ffd700";
        };
    }

    private VBox buildHabitsCard(){
        List<Habit> habits = habitDAO.getAll();

        Label title = new Label("Habit Streaks");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox list = new VBox(6);
        if (habits.isEmpty()){
            Label empty = new Label("No habits tracked yet.");
            empty.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            list.getChildren().add(empty);
        }else{
            for (Habit h : habits){
                int streak = habitDAO.getStreak(h.getId());
                boolean doneToday = habitDAO.isLoggedOn(h.getId(), LocalDate.now());

                Label nameLabel = new Label(h.getName());
                nameLabel.setStyle("-fx-font-size: 13px;");

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label statusLabel = new Label(doneToday ? "✓" : "-");
                statusLabel.setStyle("-fx-text-fill: " + (doneToday ? "#2ed573" : "#ccc") + "; -fx-font-weight: bold;");

                Label streakLabel = new Label(streak > 0 ? "🔥 " + streak : "");
                streakLabel.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 12px;");

                HBox row = new HBox(8, nameLabel, spacer, streakLabel, statusLabel);
                row.setAlignment(Pos.CENTER_LEFT);
                list.getChildren().add(row);
            }
        }

        VBox card = new VBox(10, title, list);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        return card;
    }

    private String formatMoney(double amount){
        return String.format(Locale.US, "$%.2f", amount);
    }
}
