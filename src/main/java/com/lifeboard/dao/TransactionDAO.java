package com.lifeboard.dao;

import com.lifeboard.db.Database;
import com.lifeboard.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public List<Transaction> getAll(){
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC, id DESC";

        Connection conn = Database.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        list.add(mapRow(rs));
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public void insert(String description, double amount, String category, String type, String date){
        String sql = "INSERT INTO transactions (description, amount, category, type, transaction_date) VALUES (?, ?, ?, ?, ?)";

        Connection conn = Database.getConnection();
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, description);
            stmt.setDouble(2, amount);
            stmt.setString(3, category);
            stmt.setString(4, type);
            stmt.setString(5, date);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(int id){
        String sql = "DELETE FROM transactions WHERE id = ?";

        Connection conn = Database.getConnection();
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public double getMonthTotal(String type, LocalDate monthAnchor){
        String monthPrefix = monthAnchor.toString().substring(0, 7);
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM transactions WHERE type = ? AND transaction_date LIKE ?";

        Connection conn = Database.getConnection();
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, type);
            stmt.setString(2, monthPrefix + "%");
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getDouble("total");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public double getRangeTotal(String type, LocalDate from, LocalDate to){
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM transactions WHERE type = ? AND transaction_date BETWEEN ? AND ?";

        Connection conn = Database.getConnection();
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, type);
            stmt.setString(2, from.toString());
            stmt.setString(3, to.toString());
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return rs.getDouble("total");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public double getAllTimeBalance(){
        String sql = "SELECT " + 
            "COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) AS balance " +
            "FROM transactions";

        Connection conn = Database.getConnection();
        try(PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        return rs.getDouble("balance");
                    }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException{
        return new Transaction(
            rs.getInt("id"),
            rs.getString("description"),
            rs.getDouble("amount"),
            rs.getString("category"),
            rs.getString("type"),
            rs.getString("transaction_date")
        );
    }
}
