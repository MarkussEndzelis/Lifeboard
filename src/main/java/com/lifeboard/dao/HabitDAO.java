package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.Habit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HabitDAO {
    
    public List<Habit> getAll(){
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits ORDER BY created_at ASC";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        habits.add(new Habit(rs.getInt("id"), rs.getString("name"), rs.getString("created_at")));
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return habits;
    }

    public void insert(String name){
        String sql = "INSERT INTO habits (name, created_at) VALUES (?, ?)";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, name);
                    stmt.setString(2, LocalDateTime.now().toString());
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int habitId){
        String sql = "DELETE FROM habits WHERE id = ?";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, habitId);
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public boolean isLoggedOn(int habitId, LocalDate date){
        String sql = "SELECT 1 FROM habit_logs WHERE habit_id = ? AND log_date = ?";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, habitId);
                    stmt.setString(2, date.toString());
                    try(ResultSet rs = stmt.executeQuery()){
                        return rs.next();
                    }
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void logDate(int habitId, LocalDate date){
        String sql = "INSERT OR IGNORE INTO habit_logs (habit_id, log_date) VALUES (?, ?)";
    
        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, habitId);
                    stmt.setString(2, date.toString());
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void unlogDate(int habitId, LocalDate date){
        String sql = "DELETE FROM habit_logs WHERE habit_id = ? AND log_date = ?";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, habitId);
                    stmt.setString(2, date.toString());
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Set<LocalDate> getLoggedDatesInRange(int habitId, LocalDate from, LocalDate to){
        Set<LocalDate> dates = new HashSet<>();
        String sql = "SELECT log_date FROM habit_logs WHERE habit_id = ? AND log_date BETWEEN ? AND ?";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, habitId);
                    stmt.setString(2, from.toString());
                    stmt.setString(3, to.toString());
                    try (ResultSet rs = stmt.executeQuery()){
                        while(rs.next()){
                            dates.add(LocalDate.parse(rs.getString("log_date")));
                        }
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return dates;
    }
    public int getStreak(int habitId){
        LocalDate today = LocalDate.now();
        LocalDate cursor = today;

        if(!isLoggedOn(habitId, today)){
            cursor = today.minusDays(1);
            if(!isLoggedOn(habitId, cursor)){
                return 0;
            }
        }

        int streak = 0;
        while(isLoggedOn(habitId, cursor)){
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
