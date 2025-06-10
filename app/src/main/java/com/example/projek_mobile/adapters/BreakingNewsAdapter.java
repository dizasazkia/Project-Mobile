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
import androidx.recyclerview.widget.RecyclerView;

import com.example.projek_mobile.R;
import com.example.projek_mobile.activities.DetailActivity;
import com.example.projek_mobile.models.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BreakingNewsAdapter extends RecyclerView.Adapter<BreakingNewsAdapter.CarouselViewHolder> {

    private final Context context;
    private final List<Article> articleList;

    public BreakingNewsAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_breaking_news, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.tvTitle.setText(article.getTitle() != null ? article.getTitle() : "Tidak ada judul");

        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get().load(article.getUrlToImage()).into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (article.getUrl() == null) {
                Log.w("BreakingNewsAdapter", "Cannot open DetailActivity: URL is null for article: " + article.getTitle());
                return;
            }
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("article", article); // Mengirim objek Article lengkap
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList != null ? articleList.size() : 0;
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCarouselImage);
            tvTitle = itemView.findViewById(R.id.tvCarouselTitle);
        }
    }
}