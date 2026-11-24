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

    // We changed 'time' to 'startTime' here so it matches the new database table
    @Query("SELECT * FROM activities ORDER BY startTime ASC")
    LiveData<List<ActivityItem>> getAllActivities();
}
