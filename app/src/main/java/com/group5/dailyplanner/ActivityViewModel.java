package com.group5.dailyplanner;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ActivityViewModel extends AndroidViewModel {
    private ActivityDao activityDao;
    private LiveData<List<ActivityItem>> allActivities;

    public ActivityViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        activityDao = db.activityDao();
        allActivities = activityDao.getAllActivities();
    }

    public void insert(ActivityItem activity) {
        new Thread(() -> activityDao.insert(activity)).start();
    }
    
    public void update(ActivityItem activity) {
        new Thread(() -> activityDao.update(activity)).start();
    }

    public void delete(ActivityItem activity) {
        new Thread(() -> activityDao.delete(activity)).start();
    }

    public LiveData<List<ActivityItem>> getAllActivities() {
        return allActivities;
    }
}
