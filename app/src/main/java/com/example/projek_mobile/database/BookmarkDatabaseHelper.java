package com.example.projek_mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.projek_mobile.models.Article;

import java.util.ArrayList;
import java.util.List;

public class BookmarkDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmark_db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "bookmarks";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_SOURCE = "source";
    private static final String COLUMN_PUBLISHED_AT = "publishedAt";

    public BookmarkDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_NAME + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_TITLE + " TEXT, "
                        + COLUMN_URL + " TEXT UNIQUE, "
                        + COLUMN_IMAGE + " TEXT, "
                        + COLUMN_DESCRIPTION + " TEXT, "
                        + COLUMN_SOURCE + " TEXT, "
                        + COLUMN_PUBLISHED_AT + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBookmark(Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, article.getTitle());
        values.put(COLUMN_URL, article.getUrl());
        values.put(COLUMN_IMAGE, article.getUrlToImage());
        values.put(COLUMN_DESCRIPTION, article.getDescription());
        if (article.getSource() != null) {
            values.put(COLUMN_SOURCE, article.getSource().getName());
        }
        values.put(COLUMN_PUBLISHED_AT, article.getPublishedAt());

        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeBookmark(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_URL + "=?", new String[]{url});
        db.close();
    }

    public List<Article> getAllBookmarks() {
        List<Article> bookmarks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Article article = new Article();
                article.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                article.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)));
                article.setUrlToImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
                // Tambahkan pengecekan null untuk kolom description
                article.setDescription(cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)) ? "" : cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                article.setPublishedAt(cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT)) ? "" : cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT)));

                Article.Source source = article.new Source();
                source.setName(cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_SOURCE)) ? "Unknown" : cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE)));
                article.setSource(source);

                bookmarks.add(article);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return bookmarks;
    }

    public boolean isBookmarked(String url) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_URL + "=?", new String[]{url}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }
}