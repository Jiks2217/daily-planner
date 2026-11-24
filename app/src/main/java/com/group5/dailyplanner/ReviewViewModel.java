package com.group5.dailyplanner;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewViewModel extends AndroidViewModel {
    private ReviewDao reviewDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        reviewDao = AppDatabase.getInstance(application).reviewDao();
    }

    public void saveReview(ReviewItem review, boolean isUpdate) {
        executor.execute(() -> {
            if (isUpdate) reviewDao.update(review);
            else reviewDao.insert(review);
        });
    }

    public void getReview(String date, ReviewCallback callback) {
        executor.execute(() -> {
            ReviewItem item = reviewDao.getReviewByDate(date);
            callback.onResult(item);
        });
    }

    public interface ReviewCallback {
        void onResult(ReviewItem item);
    }
}
