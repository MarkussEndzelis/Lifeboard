package com.lifeboard.model;

public class JournalEntry {
    
    private int id;
    private String entryDate;
    private String content;
    private String mood;
    private String createdAt;
    private String photoPath;

    public JournalEntry(int id, String entryDate, String content, String mood, String createdAt, String photoPath){
        this.id = id;
        this.entryDate = entryDate;
        this.content = content;
        this.mood = mood;
        this.createdAt = createdAt;
        this.photoPath = photoPath;
    }

    public int getId(){return id;}
    public String getEntryDate(){return entryDate;}
    public String getContent(){return content;}
    public String getMood(){return mood;}
    public String getCreatedAt(){return createdAt;}
    public String getPhotoPath(){return photoPath;}
}
