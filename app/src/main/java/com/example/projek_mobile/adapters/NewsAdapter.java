package com.example.projek_mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.projek_mobile.database.BookmarkDatabaseHelper;
import com.example.projek_mobile.models.Article;
import com.example.projek_mobile.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<Article> articleList;
    private final BookmarkDatabaseHelper dbHelper;

    public NewsAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
        this.dbHelper = new BookmarkDatabaseHelper(context);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articleList.get(position);

        // Set theme-based background
        holder.itemView.setBackgroundResource(R.color.background_color);

        // Set data to views
        holder.textTitle.setText(article.getTitle() != null ? article.getTitle() : "Tidak ada judul");
        holder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));

        String sourceName = (article.getSource() != null && article.getSource().getName() != null)
                ? article.getSource().getName()
                : "Unknown Source";
        holder.textSource.setText(sourceName);
        holder.textSource.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));

        // Set time ago using TimeUtils
        if (article.getPublishedAt() != null && !article.getPublishedAt().isEmpty()) {
            String timeAgo = TimeUtils.getTimeAgo(article.getPublishedAt());
            holder.textTime.setText(timeAgo);
        } else {
            holder.textTime.setText("Unknown time");
        }
        holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));

        // Load image with fallback
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get().load(article.getUrlToImage()).into(holder.imageNews);
        } else {
            holder.imageNews.setImageResource(R.drawable.placeholder_image);
        }

        // Bookmark handling
        if (article.getUrl() != null) {
            boolean isBookmarked = dbHelper.isBookmarked(article.getUrl());
            holder.ivBookmark.setImageResource(
                    isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border
            );
            holder.ivBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_color_primary));
        } else {
            Log.w("NewsAdapter", "URL is null for article: " + article.getTitle());
            holder.ivBookmark.setEnabled(false);
            holder.ivBookmark.setImageResource(R.drawable.ic_bookmark_border);
            holder.ivBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_color_secondary));
        }

        holder.ivBookmark.setOnClickListener(v -> {
            boolean currentlyBookmarked = dbHelper.isBookmarked(article.getUrl());
            if (currentlyBookmarked) {
                dbHelper.removeBookmark(article.getUrl());
                holder.ivBookmark.setImageResource(R.drawable.ic_bookmark_border);
                holder.ivBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_color_primary));
            } else {
                dbHelper.addBookmark(article);
                holder.ivBookmark.setImageResource(R.drawable.ic_bookmark_filled);
                holder.ivBookmark.setColorFilter(ContextCompat.getColor(context, R.color.text_color_primary));
            }
        });

        // Open detail
        holder.itemView.setOnClickListener(v -> {
            if (article.getUrl() == null) {
                Log.w("NewsAdapter", "Cannot open DetailActivity: URL is null for article: " + article.getTitle());
                return;
            }
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("article", article);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageNews, ivBookmark;
        TextView textTitle, textSource, textTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageNews = itemView.findViewById(R.id.ivNews);
            ivBookmark = itemView.findViewById(R.id.ivBookmark);
            textTitle = itemView.findViewById(R.id.tvTitle);
            textSource = itemView.findViewById(R.id.tvSource);
            textTime = itemView.findViewById(R.id.tvTime);
        }
    }
}