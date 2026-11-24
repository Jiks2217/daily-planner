package com.group5.dailyplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReviewDetailSheet extends BottomSheetDialogFragment {
    private ReviewItem item;
    public ReviewDetailSheet(ReviewItem item) { this.item = item; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_review_detail, container, false);
        ((TextView) view.findViewById(R.id.tvDetailDate)).setText(item.date);
        ((TextView) view.findViewById(R.id.tvDetailScore)).setText("Score: " + item.score + "/100");
        ((TextView) view.findViewById(R.id.tvDetailAcc)).setText(item.accomplishment.isEmpty() ? "None" : item.accomplishment);
        ((TextView) view.findViewById(R.id.tvDetailChal)).setText(item.challenges.isEmpty() ? "None" : item.challenges);
        ((TextView) view.findViewById(R.id.tvDetailImp)).setText(item.improve.isEmpty() ? "None" : item.improve);
        return view;
    }
}
