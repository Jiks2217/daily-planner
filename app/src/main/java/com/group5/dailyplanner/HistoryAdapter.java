package com.group5.dailyplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<ReviewItem> reviews = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(ReviewItem item); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }

    public void setReviews(List<ReviewItem> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewItem item = reviews.get(position);
        holder.tvDate.setText(item.date);
        holder.tvScore.setText(item.score + "/100");
        holder.tvAccomplishment.setText("Accomplishment: " + (item.accomplishment.isEmpty() ? "None" : item.accomplishment));

        if (item.score >= 80) holder.tvScore.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        else if (item.score >= 50) holder.tvScore.setTextColor(android.graphics.Color.parseColor("#FFB74D"));
        else holder.tvScore.setTextColor(android.graphics.Color.parseColor("#E57373"));

        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });
    }

    @Override public int getItemCount() { return reviews.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvScore, tvAccomplishment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvScore = itemView.findViewById(R.id.tvHistoryScore);
            tvAccomplishment = itemView.findViewById(R.id.tvHistoryAccomplishment);
        }
    }
}
