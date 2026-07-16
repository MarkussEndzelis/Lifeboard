package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.MealEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MealPlanDAO {

    public List<MealEntry> getAll(){
        List<MealEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM meal_plan";
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

    public MealEntry get(String dayOfWeek, String mealType){
        String sql = "SELECT * FROM meal_plan WHERE day_of_week = ? AND meal_type = ?";
        Connection conn = Database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, dayOfWeek);
            stmt.setString(2, mealType);
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

    public void upsert(String dayOfWeek, String mealType, String mealName){
        MealEntry existing = get(dayOfWeek, mealType);
        Connection conn = Database.getConnection();

        if (existing != null){
            String sql = "UPDATE meal_plan SET meal_name = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, mealName);
                stmt.setInt(2, existing.getId());
                stmt.executeUpdate();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }else{
            String sql = "INSERT INTO meal_plan (day_of_week, meal_type, meal_name) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, dayOfWeek);
                stmt.setString(2, mealType);
                stmt.setString(3, mealName);
                stmt.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    private MealEntry mapRow(ResultSet rs) throws SQLException {
        return new MealEntry(
            rs.getInt("id"),
            rs.getString("day_of_week"),
            rs.getString("meal_type"),
            rs.getString("meal_name")
        );
    }
}
