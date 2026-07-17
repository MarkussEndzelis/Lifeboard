package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.Goal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {
    
    public List<Goal> getAll(){
        List<Goal> list = new ArrayList<>();
        String sql = "SELECT * FROM goals ORDER BY completed ASC, deadline IS NULL, deadline ASC";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        list.add(mapRow(rs));
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public void insert(String title, double targetValue, String unit, String deadline){
        insert(title, targetValue, unit, deadline, "MANUAL", null);
    }

    public void insert(String title, double targetValue, String unit, String deadline, String linkType, Integer linkedHabitId){
        String sql = "INSERT INTO goals (title, target_value, current_value, unit, deadline, completed, created_at, link_type, linked_habit_id) VALUES (?, ?, 0, ?, ?, 0, ?, ?, ?)";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, title);
            stmt.setDouble(2, targetValue);
            stmt.setString(3, unit);
            stmt.setString(4, deadline);
            stmt.setString(5, LocalDateTime.now().toString());
            stmt.setString(6, linkType != null ? linkType : "MANUAL");
            if (linkedHabitId != null){
                stmt.setInt(7, linkedHabitId);
            }else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void updateProgress(int id, double currentValue){
        String sql = "UPDATE goals SET current_value = ? WHERE id = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setDouble(1, currentValue);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setCompleted(int id, boolean completed){
        String sql = "UPDATE goals SET completed ? WHERE id = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, completed ? 1 : 0);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM goals WHERE id = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void syncLinkedGoals(HabitDAO habitDAO, TransactionDAO transactionDAO){
        for (Goal goal : getAll()){
            if ("BUDGET".equals(goal.getLinkType())){
                double balance = transactionDAO.getAllTimeBalance();
                updateProgress(goal.getId(), balance);
            }else if ("HABIT".equals(goal.getLinkType()) && goal.getLinkedHabitId() != null){
                int streak = habitDAO.getStreak(goal.getLinkedHabitId());
                updateProgress(goal.getId(), streak);
            }
        }
    }

    private Goal mapRow(ResultSet rs) throws SQLException {
        int linkedHabitId = rs.getInt("linked_habit_id");
        boolean linkedHabitIdWasNull = rs.wasNull();
        return new Goal(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getDouble("target_value"),
            rs.getDouble("current_value"),
            rs.getString("unit"),
            rs.getString("deadline"),
            rs.getInt("completed") == 1,
            rs.getString("created_at"),
            rs.getString("link_type"),
            linkedHabitIdWasNull ? null : linkedHabitId
        );
    }
}
