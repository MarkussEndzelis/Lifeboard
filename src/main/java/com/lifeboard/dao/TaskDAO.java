package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    
    public List<Task> getAll(){
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY completed ASC, priority ASC, due_date IS NULL, due_date ASC";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        tasks.add(mapRow(rs));
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return tasks;
    }

    public void insert(String title, String category, int priority, String dueDate){
        String sql = "INSERT INTO tasks (title, category, priority, due_date, completed, created_at) VALUES (?, ?, ?, ?, 0, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, title);
            stmt.setString(2, category);
            stmt.setInt(3, priority);
            stmt.setString(4, dueDate);
            stmt.setString(5, LocalDateTime.now().toString());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setCompleted(int id, boolean completed){
        String sql = "UPDATE tasks SET completed = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, completed ? 1 : 0);
                    stmt.setInt(2, id);
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void update(int id, String title, String category, int priority, String dueDate){
        String sql = "UPDATE tasks SET title = ?, category = ?, priority = ?, due_date = ?, WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)){
                    stmt.setString(1, title);
                    stmt.setString(2, category);
                    stmt.setInt(3, priority);
                    stmt.setString(4, dueDate);
                    stmt.setInt(5, id);
                    stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("category"),
            rs.getInt("priority"),
            rs.getString("due_date"),
            rs.getInt("completed") == 1,
            rs.getString("created_at")
        );
    }
}
