package com.example.projek_mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projek_mobile.R;
import com.example.projek_mobile.database.BookmarkDatabaseHelper;
import com.example.projek_mobile.models.Article;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvSource, tvDescription;
    private ImageView ivNews, ivBookmarkDetail;
    private Button btnOpenFullNews;
    private ImageButton btnBack;
    private BookmarkDatabaseHelper dbHelper;
    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi view
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvSource = findViewById(R.id.tvDetailSource);
        tvDescription = findViewById(R.id.tvDetailDescription);
        ivNews = findViewById(R.id.ivDetailNews);
        btnOpenFullNews = findViewById(R.id.btnOpenFullNews);
        btnBack = findViewById(R.id.btnBack);
        ivBookmarkDetail = findViewById(R.id.ivBookmarkDetail);

        // Inisialisasi database
        dbHelper = new BookmarkDatabaseHelper(this);

        // Ambil objek Article dari Intent
        Intent intent = getIntent();
        article = (Article) intent.getSerializableExtra("article");

        // Set data ke view
        if (article != null) {
            tvTitle.setText(article.getTitle() != null ? article.getTitle() : "Tidak ada judul");
            tvSource.setText(article.getSource() != null && article.getSource().getName() != null ? article.getSource().getName() : "Unknown Source");
            tvDescription.setText(article.getDescription() != null ? article.getDescription() : "Tidak ada deskripsi tersedia");

            if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
                Picasso.get().load(article.getUrlToImage()).into(ivNews);
            } else {
                ivNews.setImageResource(R.drawable.placeholder_image);
            }

            // Simpan URL untuk tombol full news
            String newsUrl = article.getUrl();
            btnOpenFullNews.setOnClickListener(v -> {
                if (newsUrl != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
                    startActivity(browserIntent);
                } else {
                    Log.w("DetailActivity", "newsUrl is null, cannot open full news");
                }
            });

            // Tampilkan status bookmark hanya jika URL tidak null
            if (article.getUrl() != null) {
                boolean isBookmarked = dbHelper.isBookmarked(article.getUrl());
                ivBookmarkDetail.setImageResource(
                        isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border
                );

                // Logika bookmark
                ivBookmarkDetail.setOnClickListener(v -> {
                    boolean currentlyBookmarked = dbHelper.isBookmarked(article.getUrl());
                    if (currentlyBookmarked) {
                        dbHelper.removeBookmark(article.getUrl());
                        ivBookmarkDetail.setImageResource(R.drawable.ic_bookmark_border);
                    } else {
                        dbHelper.addBookmark(article);
                        ivBookmarkDetail.setImageResource(R.drawable.ic_bookmark_filled);
                    }
                });
            } else {
                Log.w("DetailActivity", "article.getUrl() is null, disabling bookmark button");
                ivBookmarkDetail.setEnabled(false);
                ivBookmarkDetail.setImageResource(R.drawable.ic_bookmark_border);
            }
        } else {
            Log.e("DetailActivity", "Article is null");
            tvTitle.setText("Tidak ada judul");
            tvSource.setText("Unknown Source");
            tvDescription.setText("Tidak ada deskripsi tersedia");
            ivNews.setImageResource(R.drawable.placeholder_image);
            ivBookmarkDetail.setEnabled(false);
            ivBookmarkDetail.setImageResource(R.drawable.ic_bookmark_border);
        }

        // Tombol Back
        btnBack.setOnClickListener(v -> onBackPressed());
    }
}