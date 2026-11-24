package com.group5.dailyplanner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date; // Format: "yyyy-MM-dd"
    public String accomplishment;
    public String challenges;
    public String learned;
    public String improve;

    public ReviewItem(String date, String accomplishment, String challenges, String learned, String improve) {
        this.date = date;
        this.accomplishment = accomplishment;
        this.challenges = challenges;
        this.learned = learned;
        this.improve = improve;
    }
}
