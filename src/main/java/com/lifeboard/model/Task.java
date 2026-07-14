package com.lifeboard.model;

public class Task {

    private int id;
    private String title;
    private String category;
    private int priority;
    private String dueDate;
    private boolean completed;
    private String createdAt;

    public Task(int id, String title, String category, int priority, String dueDate, boolean completed, String createdAt){
        this.id = id;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public int getId(){return id;}
    public String getTitle(){return title;}
    public String getCategory(){return category;}
    public int getPriority(){return priority;}
    public String getDueDate(){return dueDate;}
    public boolean isCompleted(){return completed;}
    public String getCreatedAt(){return createdAt;}

    public void setTitle(String title){this.title = title;}
    public void setCategory(String category){this.category = category;}
    public void setPriority(int priority){this.priority = priority;}
    public void setDueDate(String dueDate){this.dueDate = dueDate;}
    public void setCompleted(boolean completed){this.completed = completed; }

    public String getPriorityLabel(){
        return switch (priority){
            case 1 -> "High";
            case 3 -> "Low";
            default -> "Medium";
        };
    }
}
