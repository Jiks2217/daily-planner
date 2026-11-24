package com.group5.dailyplanner;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insert(ReviewItem review);

    @Update
    void update(ReviewItem review);

    @Query("SELECT * FROM reviews WHERE date = :date LIMIT 1")
    ReviewItem getReviewByDate(String date);

    @Query("SELECT * FROM reviews ORDER BY date DESC")
    LiveData<List<ReviewItem>> getAllReviews();
}
