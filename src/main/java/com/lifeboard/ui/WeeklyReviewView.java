package com.lifeboard.ui;

import com.lifeboard.dao.GoalDAO;
import com.lifeboard.dao.HabitDAO;
import com.lifeboard.dao.JournalDAO;
import com.lifeboard.dao.TaskDAO;
import com.lifeboard.dao.TransactionDAO;
import com.lifeboard.model.Goal;
import com.lifeboard.model.Habit;
import com.lifeboard.model.Task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class WeeklyReviewView extends VBox {
    
    private final TaskDAO taskDAO = new TaskDAO();
    private final HabitDAO habitDAO = new HabitDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final JournalDAO journalDAO = new JournalDAO();
    private final GoalDAO goalDAO = new GoalDAO();

    private final LocalDate today = LocalDate.now();
    private final LocalDate weekStart = today.minusDays(6);

    public WeeklyReviewView(){
        setSpacing(16);

        Label header = new Label("Weekly Review");
        header.getStyleClass().add("page-header");

        Label range = new Label(weekStart + " - " + today);
        range.getStyleClass().add("text-muted");

        VBox content = new VBox(16);
        content.getChildren().addAll(
            buildTasksCard(),
            buildHabitsCard(),
            buildBudgetCard(),
            buildJournalCard(),
            buildGoalsCard(),
            buildInsightsCard()
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, range, scrollPane);
    }

    private int overdueCount;
    private int dueThisWeekCompleted;
    private int dueThisWeekTotal;

    private VBox buildTasksCard(){
        List<Task> tasks = taskDAO.getAll();
        overdueCount = 0;
        dueThisWeekCompleted = 0;
        dueThisWeekTotal = 0;

        for (Task t : tasks){
            if (t.getDueDate() == null) continue;
            LocalDate due = LocalDate.parse(t.getDueDate());
            if (!t.isCompleted() && due.isBefore(today)){
                overdueCount++;
            }
            if (!due.isBefore(weekStart) && !due.isAfter(today)){
                dueThisWeekTotal++;
                if (t.isCompleted()) dueThisWeekCompleted++;
            }
        }

        Label title = new Label("Tasks");
        title.getStyleClass().add("card-title");

        Label line1 = new Label(dueThisWeekCompleted + " of " + dueThisWeekTotal + " tasks due this week completed");
        Label line2 = new Label(overdueCount + " task" + (overdueCount == 1 ? "" : "s") + " overdue");
        line2.getStyleClass().add(overdueCount > 0 ? "text-warning" : "text-muted");

        VBox card = new VBox(6, title, line1, line2);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }

    private int totalCheckIns;
    private int totalPossibleCheckIns;

    private VBox buildHabitsCard(){
        List<Habit> habits = habitDAO.getAll();
        totalCheckIns = 0;
        totalPossibleCheckIns = habits.size() * 7;

        Label title = new Label("Habits");
        title.getStyleClass().add("card-title");

        VBox card = new VBox(6, title);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");

        if (habits.isEmpty()){
            Label empty = new Label("No habits tracked yet.");
            empty.getStyleClass().add("text-muted");
            card.getChildren().add(empty);
            return card;
        }

        for (Habit h : habits){
            int loggedInRange = habitDAO.getLoggedDatesInRange(h.getId(), weekStart, today).size();
            totalCheckIns += loggedInRange;
            int streak = habitDAO.getStreak(h.getId());

            Label row = new Label(h.getName() + ": " + loggedInRange + "/7 days this week - current streak " + streak);
            card.getChildren().add(row);
        }
        return card;
    }

    private double weekIncome;
    private double weekExpense;

    private VBox buildBudgetCard(){
        weekIncome = transactionDAO.getRangeTotal("income", weekStart, today);
        weekExpense = transactionDAO.getRangeTotal("expense", weekStart, today);
        double net = weekIncome - weekExpense;

        Label title = new Label("Budget");
        title.getStyleClass().add("card-title");

        Label line1 = new Label(String.format("Income: $%.2f   Expenses: $%.2f", weekIncome, weekExpense));
        Label line2 = new Label(String.format("Net this week: %s$%.2f", net >= 0 ? "+" : "-", Math.abs(net)));
        line2.getStyleClass().add(net >= 0 ? "text-success" : "text-warning");

        VBox card = new VBox(6, title, line1, line2);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }

    private int journalDaysWritten;

    private VBox buildJournalCard(){
        journalDaysWritten = 0;
        LocalDate cursor = weekStart;
        while (!cursor.isAfter(today)){
            if (journalDAO.getByDate(cursor) != null){
                journalDaysWritten++;
            }
            cursor = cursor.plusDays(1);
        }

        Label title = new Label("Journal");
        title.getStyleClass().add("card-title");

        Label line = new Label(journalDaysWritten + " of 7 days this week have an entry");

        VBox card = new VBox(6, title, line);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }

    private int goalsBehindSchedule;
    private int goalsCompletedThisWeek;

    private VBox buildGoalsCard(){
        List<Goal> goals = goalDAO.getAll();
        goalsBehindSchedule = 0;
        goalsCompletedThisWeek = 0;
        int totalCompleted = 0;

        for (Goal g : goals){
            if (g.isCompleted()){
                totalCompleted++;
                continue;
            }
            if (g.getDeadline() == null) continue;

            LocalDate created = LocalDate.parse(g.getCreatedAt().substring(0, 10));
            LocalDate deadline = LocalDate.parse(g.getDeadline());
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(created, deadline);
            long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(created, today);

            if (totalDays <= 0) continue;
            double expectedFraction = Math.min(1.0, Math.max(0.0, (double) elapsedDays / totalDays));

            if (g.getProgressFraction() < expectedFraction - 0.1){
                goalsBehindSchedule++;
            }
        }

        Label title = new Label("Goals");
        title.getStyleClass().add("card-title");

        Label line1 = new Label(totalCompleted + " goal" + (totalCompleted == 1 ? "" : "s") + " completed so far");
        Label line2 = new Label(goalsBehindSchedule + " goal" + (goalsBehindSchedule == 1 ? "" : "s") + " behind schedule");
        line2.getStyleClass().add(goalsBehindSchedule > 0 ? "text-warning" : "text-muted");

        VBox card = new VBox(6, title, line1, line2);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        return card;
    }

    private VBox buildInsightsCard(){
        Label title = new Label("Insights");
        title.getStyleClass().add("card-title");

        VBox card = new VBox(6, title);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");

        if (overdueCount > 0){
            card.getChildren().add(new Label("You have " + overdueCount + " overdue task" + (overdueCount == 1 ? "" : "s") + " - wodth clearing those first."));
        }
        if (weekExpense > weekIncome){
            card.getChildren().add(new Label(String.format("You spent $%.2f more than you earned this week.", weekExpense - weekIncome)));
        }
        if (totalPossibleCheckIns > 0){
            double rate = (double) totalCheckIns / totalPossibleCheckIns;
            if (rate < 0.5){
                card.getChildren().add(new Label("Habit check-ins were under 50% this week."));
            }
        }
        if (journalDaysWritten == 0){
            card.getChildren().add(new Label("No journal entries this week."));
        }
        if (goalsBehindSchedule > 0){
            card.getChildren().add(new Label("" + goalsBehindSchedule + " goal" + (goalsBehindSchedule == 1 ? "" : "s") + " may need attention to hit their deadline."));
        }
        if (card.getChildren().size() == 1){
            card.getChildren().add(new Label("Nothing urgent - solid week."));
        }

        return card;
    }
}
