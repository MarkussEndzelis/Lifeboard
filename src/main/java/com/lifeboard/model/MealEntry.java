package com.lifeboard.model;

public class MealEntry {
    
    private int id;
    private String dayOfWeek;
    private String mealType;
    private String mealName;

    public MealEntry(int id, String dayOfWeek, String mealType, String mealName){
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.mealName = mealName;
    }

    public int getId(){return id;}
    public String getDayOfWeek(){return dayOfWeek;}
    public String getMealType(){return mealType;}
    public String getMealName(){return mealName;}
}
