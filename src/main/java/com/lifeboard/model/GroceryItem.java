package com.lifeboard.model;

public class GroceryItem {
    
    private int id;
    private String item;
    private boolean checked;
    private String createdAt;

    public GroceryItem(int id, String item, boolean checked, String createdAt){
        this.id = id;
        this.item = item;
        this.checked = checked;
        this.createdAt = createdAt;
    }

    public int getId(){return id;}
    public String getItem(){return item;}
    public boolean isChecked(){return checked;}
    public String getCreatedAt(){return createdAt;}
}
