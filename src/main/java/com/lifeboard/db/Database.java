package com.lifeboard.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    
    private static final String URL = "jdbc:sqlite:lifeboard.db";
    private static Connection connection;

    public static Connection getConnection(){
        if (connection == null){
            try{
                connection = DriverManager.getConnection(URL);
            }catch(SQLException e){
                throw new RuntimeException("Failed to connect to database", e);
            }
        }
        return connection;
    }

    public static void initialize(){
        String tasks = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    category TEXT,
                    priority INTEGER DEFAULT 2,
                    due_date TEXT,
                    completed INTEGER DEFAULT 0,
                    created_at TEXT NOT NULL
                )
                """;
        
        String habits = """
                CREATE TABLE IF NOT EXISTS habits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
                """;

        String habitLogs = """
                CREATE TABLE IF NOT EXISTS habit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    habit_id INTEGER NOT NULL,
                    log_date TEXT NOT NULL,
                    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
                    UNIQUE(habit_id, log_date)
                )
                """;

        String transactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT,
                    type TEXT NOT NULL,
                    transaction_date TEXT NOT NULL
                )
                """;

        String journal = """
                CREATE TABLE IF NOT EXISTS journal_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entry_date TEXT NOT NULL UNIQUE,
                    content TEXT NOT NULL,
                    mood TEXT,
                    created_at TEXT NOT NULL
                )
                """;

        try (Statement stmt = getConnection().createStatement()){
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(tasks);
            stmt.execute(habits);
            stmt.execute(habitLogs);
            stmt.execute(transactions);
            stmt.execute(journal);
            try{
                stmt.execute("ALTER TABLE journal_entries ADD COLUMN photo_path TEXT");
            }catch (SQLException e){

            }
        }catch(SQLException e){
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
