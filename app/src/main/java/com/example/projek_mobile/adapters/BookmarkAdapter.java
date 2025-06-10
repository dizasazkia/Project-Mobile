package com.example.projek_mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projek_mobile.R;
import com.example.projek_mobile.activities.DetailActivity;
import com.example.projek_mobile.models.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private Context context;
    private List<Article> bookmarkList;

    public BookmarkAdapter(Context context, List<Article> bookmarkList) {
        this.context = context;
        this.bookmarkList = bookmarkList;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Article article = bookmarkList.get(position);

        // Set theme-based background
        holder.itemView.setBackgroundResource(R.color.background_color);

        // Set data to TextViews with null checks
        if (holder.titleTextView != null) {
            holder.titleTextView.setText(article.getTitle() != null ? article.getTitle() : "Tidak ada judul");
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
        }
        if (holder.sourceTextView != null) {
            holder.sourceTextView.setText(article.getSource() != null && article.getSource().getName() != null ? article.getSource().getName() : "Unknown");
            holder.sourceTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));
        }
        if (holder.timeTextView != null) {
            holder.timeTextView.setText(article.getPublishedAt() != null ? article.getPublishedAt() : "Tidak ada waktu");
            holder.timeTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));
        }

        // Set image
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get().load(article.getUrlToImage()).into(holder.newsImageView);
        } else {
            holder.newsImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Click to open DetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("article", article);
            context.startActivity(intent);
        });

        // Hide bookmark button
        if (holder.bookmarkImageView != null) {
            holder.bookmarkImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, sourceTextView, timeTextView;
        ImageView newsImageView, bookmarkImageView;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTitle);
            sourceTextView = itemView.findViewById(R.id.tvSource);
            timeTextView = itemView.findViewById(R.id.tvTime);
            newsImageView = itemView.findViewById(R.id.ivNews);
            bookmarkImageView = itemView.findViewById(R.id.ivBookmark);
        }
    }
}