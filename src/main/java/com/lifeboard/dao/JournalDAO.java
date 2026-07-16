package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.JournalEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalDAO {
    
    public List<JournalEntry> getAll(){
        List<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM journal_entries ORDER BY entry_date DESC";
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

    public JournalEntry getByDate(LocalDate date){
        String sql = "SELECT * FROM journal_entries WHERE entry_date = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, date.toString());
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return mapRow(rs);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public void upsert(LocalDate date, String content, String mood, String photoPath){
        JournalEntry existing = getByDate(date);
        Connection conn = Database.getConnection();

        if (existing != null){
            String sql = "UPDATE journal_entries SET content = ?, mood = ?, photo_path = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, content);
                stmt.setString(2, mood);
                stmt.setString(3, photoPath);
                stmt.setInt(4, existing.getId());
                stmt.executeUpdate();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }else{
            String sql = "INSERT INTO journal_entries (entry_date, content, mood, created_at, photo_path) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, date.toString());
                stmt.setString(2, content);
                stmt.setString(3, mood);
                stmt.setString(4, LocalDateTime.now().toString());
                stmt.setString(5, photoPath);
                stmt.executeUpdate();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM journal_entries WHERE id = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public List<JournalEntry> search(String keyword){
        List<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM journal_entries WHERE content LIKE ? ORDER BY entry_date DESC";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    list.add(mapRow(rs));
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    private JournalEntry mapRow(ResultSet rs) throws SQLException {
        return new JournalEntry(
            rs.getInt("id"),
            rs.getString("entry_date"),
            rs.getString("content"),
            rs.getString("mood"),
            rs.getString("created_at"),
            rs.getString("photo_path")
        );
    }
}
