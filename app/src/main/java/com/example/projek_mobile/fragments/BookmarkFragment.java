package com.example.projek_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projek_mobile.R;
import com.example.projek_mobile.adapters.BookmarkAdapter;
import com.example.projek_mobile.database.BookmarkDatabaseHelper;
import com.example.projek_mobile.models.Article;

import java.util.List;

public class BookmarkFragment extends Fragment {

    private RecyclerView bookmarkRecyclerView;
    private BookmarkDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        bookmarkRecyclerView = view.findViewById(R.id.bookmarkRecyclerView);
        bookmarkRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        view.setBackgroundResource(R.color.background_color);

        dbHelper = new BookmarkDatabaseHelper(getContext());

        loadBookmarks();
        return view;
    }

    private void loadBookmarks() {
        List<Article> bookmarks = dbHelper.getAllBookmarks();
        BookmarkAdapter adapter = new BookmarkAdapter(getContext(), bookmarks);
        bookmarkRecyclerView.setAdapter(adapter);
    }
}