package com.lifeboard.util;

import com.lifeboard.dao.GoalDAO;
import com.lifeboard.dao.HabitDAO;
import com.lifeboard.dao.JournalDAO;
import com.lifeboard.dao.TaskDAO;
import com.lifeboard.dao.TransactionDAO;
import com.lifeboard.model.Goal;
import com.lifeboard.model.Habit;
import com.lifeboard.model.Task;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class WeeklyReviewExporter {
    
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();

    private final TaskDAO taskDAO = new TaskDAO();
    private final HabitDAO habitDAO = new HabitDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final JournalDAO journalDAO = new JournalDAO();
    private final GoalDAO goalDAO = new GoalDAO();

    private PDDocument doc;
    private PDPage page;
    private PDPageContentStream stream;
    private float cursorY;

    private PDType1Font fontRegular;
    private PDType1Font fontBold;

    public void exportToFile(File file, LocalDate weekStart, LocalDate today) throws IOException {
        doc = new PDDocument();
        fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        newPage();

        writeTitle("Weekly Review");
        writeText(weekStart + " - " + today, fontRegular, 11, 20);

        writeSection("Tasks", buildTaskLines(weekStart, today));
        writeSection("Habits", buildHabitLines(weekStart, today));
        writeSection("Budget", buildBudgetLines(weekStart, today));
        writeSection("Journal", buildJournalLines(weekStart, today));
        writeSection("Goals", buildGoalLines(today));

        stream.close();
        doc.save(file);
        doc.close();  
    }

    private List<String> buildTaskLines(LocalDate weekStart, LocalDate today){
        List<Task> tasks = taskDAO.getAll();
        int overdue = 0, dueThisWeekTotal = 0, dueThisWeekCompleted = 0;

        for (Task t : tasks){
            if (t.getDueDate() == null) continue;
            LocalDate due = LocalDate.parse(t.getDueDate());
            if (!t.isCompleted() && due.isBefore(today)) overdue++;
            if (!due.isBefore(weekStart) && !due.isAfter(today)){
                dueThisWeekTotal++;
                if (t.isCompleted()) dueThisWeekCompleted++;
            }
        }

        return List.of(
            dueThisWeekCompleted + " of " + dueThisWeekTotal + " tasks due this week completed",
            overdue + " task" + (overdue == 1 ? "" : "s") + " overdue"
        );
    }

    private List<String> buildHabitLines(LocalDate weekStart, LocalDate today){
        List<Habit> habits = habitDAO.getAll();
        if (habits.isEmpty()){
            return List.of("No habits tracked yet.");
        }
        List<String> lines = new java.util.ArrayList<>();
        for (Habit h : habits){
            int logged = habitDAO.getLoggedDatesInRange(h.getId(), weekStart, today).size();
            int streak = habitDAO.getStreak(h.getId());
            lines.add(h.getName() + ": " + logged + "/7 days this week - current streak " + streak);
        }
        return lines;
    }

    private List<String> buildBudgetLines(LocalDate weekStart, LocalDate today){
        double income = transactionDAO.getRangeTotal("income", weekStart, today);
        double expense = transactionDAO.getRangeTotal("expense", weekStart, today);
        double net = income - expense;
        return List.of(
            String.format("Income: $%.2f   Expenses: $%.2f", income, expense),
            String.format("Net this week: %s$%.2f", net >= 0 ? "+" : "-", Math.abs(net))
        );
    }

    private List<String> buildJournalLines(LocalDate weekStart, LocalDate today){
        int written = 0;
        LocalDate cursor = weekStart;
        while (!cursor.isAfter(today)){
            if(journalDAO.getByDate(cursor) != null) written++;
            cursor = cursor.plusDays(1);
        }
        return List.of(written + " of 7 days this week have an entry");
    }

    private List<String> buildGoalLines(LocalDate today){
        List<Goal> goals = goalDAO.getAll();
        int totalCompleted = 0, behindSchedule = 0;

        for (Goal g : goals){
            if (g.isCompleted()){
                totalCompleted++;
                continue;
            }
            if (g.getDeadline() == null) continue;

            LocalDate created = LocalDate.parse(g.getCreatedAt().substring(0, 10));
            LocalDate deadline = LocalDate.parse(g.getDeadline());
            long totalDays = ChronoUnit.DAYS.between(created, deadline);
            long elapsedDays = ChronoUnit.DAYS.between(created, today);
            if (totalDays <= 0) continue;

            double expectedFraction = Math.min(1.0, Math.max(0.0, (double) elapsedDays / totalDays));
            if (g.getProgressFraction() < expectedFraction - 0.1){
                behindSchedule++;
            }
        }

        return List.of(
            totalCompleted + " goal" + (totalCompleted == 1 ? "" : "s") + " completed so far",
            behindSchedule + " goal" + (behindSchedule == 1 ? "" : "s") + " behind schedule"  
        );
    }

    private void newPage() throws IOException {
        if (stream != null){
            stream.close();
        }
        page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        stream = new PDPageContentStream(doc, page);
        cursorY = PAGE_HEIGHT - MARGIN;
    }

    private void ensureSpace(float neededHeight) throws IOException {
        if (cursorY - neededHeight < MARGIN){
            newPage();
        }
    }

    private void writeTitle(String text) throws IOException {
        ensureSpace(30);
        stream.beginText();
        stream.setFont(fontBold, 20);
        stream.newLineAtOffset(MARGIN, cursorY);
        stream.showText(text);
        stream.endText();
        cursorY -= 30;
    }

    private void writeText(String text, PDType1Font font, float size, float gapAfter) throws IOException {
        ensureSpace(size + gapAfter);
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(MARGIN, cursorY);
        stream.showText(text);
        stream.endText();
        cursorY -= (size + gapAfter);
    }

    private void writeSection(String heading, List<String> lines) throws IOException {
        ensureSpace(24);
        writeText(heading, fontBold, 14, 10);
        for (String line : lines){
            writeText(line, fontRegular, 11, 14);
        }
        cursorY -= 10;
    }
}
