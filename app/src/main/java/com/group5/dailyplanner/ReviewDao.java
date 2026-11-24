package com.group5.dailyplanner;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ReviewDao {
    @Insert
    void insert(ReviewItem review);

    @Update
    void update(ReviewItem review);

    @Query("SELECT * FROM reviews WHERE date = :date LIMIT 1")
    ReviewItem getReviewByDate(String date);
}
