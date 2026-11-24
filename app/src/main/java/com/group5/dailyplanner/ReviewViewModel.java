package com.group5.dailyplanner;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewViewModel extends AndroidViewModel {
    private ReviewDao reviewDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private LiveData<List<ReviewItem>> allReviews;

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        reviewDao = AppDatabase.getInstance(application).reviewDao();
        allReviews = reviewDao.getAllReviews();
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

    public LiveData<List<ReviewItem>> getAllReviews() {
        return allReviews;
    }

    public interface ReviewCallback {
        void onResult(ReviewItem item);
    }
}
