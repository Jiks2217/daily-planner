package com.group5.dailyplanner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activities")
public class ActivityItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String time;
    public String category;
    public boolean isCompleted;

    public ActivityItem(String title, String time, String category) {
        this.title = title;
        this.time = time;
        this.category = category;
        this.isCompleted = false;
    }
}
