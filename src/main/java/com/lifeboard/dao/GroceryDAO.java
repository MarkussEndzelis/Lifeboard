package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.GroceryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroceryDAO {
    
    public List<GroceryItem> getAll(){
        List<GroceryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM grocery_items ORDER BY checked ASC, id ASC";
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

    public void add(String item){
        String sql = "INSERT INTO grocery_items (item, checked, created_at) VALUES (?, 0, ?)";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, item);
            stmt.setString(2, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setChecked(int id, boolean checked){
        String sql = "UPDATE grocery_items SET checked = ? WHERE id = ?";
        Connection conn = Database.getConnection();

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, checked ? 1 : 0);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM grocery_items WHERE id = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void clearChecked(){
        String sql = "DELETE FROM grocery_items WHERE checked = 1";
        Connection conn = Database.getConnection();

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private GroceryItem mapRow(ResultSet rs) throws SQLException {
        return new GroceryItem(
            rs.getInt("id"),
            rs.getString("item"),
            rs.getInt("checked") == 1,
            rs.getString("created_at")
        );
    }
}
