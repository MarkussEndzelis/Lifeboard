package com.lifeboard.model;

public class Transaction {
    
    private int id;
    private String description;
    private double amount;
    private String category;
    private String type;
    private String transactionDate;

    public Transaction(int id, String description, double amount, String category, String type, String transactionDate){
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.transactionDate = transactionDate;
    }

    public int getId(){
        return id;
    }
    public String getDescription(){
        return description;
    }
    public double getAmount(){
        return amount;
    }
    public String getCategory(){
        return category;
    }
    public String getType(){
        return type;
    }
    public String getTransactionDate(){
        return transactionDate;
    }

    public boolean isIncome(){
        return "income".equals(type);
    }
}
