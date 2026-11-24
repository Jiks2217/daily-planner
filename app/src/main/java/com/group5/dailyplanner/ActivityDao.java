package com.group5.dailyplanner;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ActivityDao {
    @Insert
    void insert(ActivityItem activity);

    @Update
    void update(ActivityItem activity);

    @Delete
    void delete(ActivityItem activity);

    // Automatically updates the UI when data changes
    @Query("SELECT * FROM activities ORDER BY time ASC")
    LiveData<List<ActivityItem>> getAllActivities();
}
