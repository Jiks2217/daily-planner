package com.group5.dailyplanner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int score; // 0 to 100
    public String accomplishment;
    public String challenges;
    public String learned;
    public String improve;

    public ReviewItem(String date, int score, String accomplishment, String challenges, String learned, String improve) {
        this.date = date;
        this.score = score;
        this.accomplishment = accomplishment;
        this.challenges = challenges;
        this.learned = learned;
        this.improve = improve;
    }
}
