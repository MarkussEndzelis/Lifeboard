package com.lifeboard.model;

public class Goal {

    private int id;
    private String title;
    private double targetValue;
    private double currentValue;
    private String unit;
    private String deadline;
    private boolean completed;
    private String createdAt;

    public Goal(int id, String title, double targetValue, double currentValue, String unit, String deadline, boolean completed, String createdAt){
        this.id = id;
        this.title = title;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit;
        this.deadline = deadline;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public int getId(){return id;}
    public String getTitle(){return title;}
    public double getTargetValue(){return targetValue;}
    public double getCurrentValue(){return currentValue;}
    public String getUnit(){return unit;}
    public String getDeadline(){return deadline;}
    public boolean isCompleted(){return completed;}
    public String getCreatedAt(){return createdAt;}

    public double getProgressFraction(){
        if (targetValue <= 0) return 0;
        return Math.min(1.0, currentValue / targetValue);
    }
}