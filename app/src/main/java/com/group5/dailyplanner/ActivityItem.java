package com.group5.dailyplanner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "activities")
public class ActivityItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String startTime; // e.g., "10:00 AM"
    public String endTime;   // e.g., "11:00 AM"
    public String category;
    public boolean isCompleted;

    public ActivityItem(String title, String startTime, String endTime, String category) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.isCompleted = false;
    }
}
