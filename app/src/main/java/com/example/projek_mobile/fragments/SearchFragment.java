package com.example.projek_mobile.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projek_mobile.R;
import com.example.projek_mobile.adapters.NewsAdapter;
import com.example.projek_mobile.models.Article;
import com.example.projek_mobile.models.NewsResponse;
import com.example.projek_mobile.network.ApiClient;
import com.example.projek_mobile.network.ApiService;
import com.example.projek_mobile.utils.Constants;
import com.example.projek_mobile.utils.NetworkUtils;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private EditText editSearch;
    private LinearLayout categoryContainer;
    private RecyclerView searchRecyclerView;
    private LinearLayout errorContainer;
    private TextView errorMessage;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private NewsAdapter adapter;
    private View rootView;
    private final List<String> categories = Arrays.asList("all", "technology", "business", "entertainment", "sports", "health");
    private String selectedCategory = "general";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<NewsResponse> currentCall;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        editSearch = rootView.findViewById(R.id.edit_search);
        categoryContainer = rootView.findViewById(R.id.categoryContainer);
        searchRecyclerView = rootView.findViewById(R.id.searchRecyclerView);
        errorContainer = rootView.findViewById(R.id.errorContainer);
        errorMessage = rootView.findViewById(R.id.errorMessage);
        retryButton = rootView.findViewById(R.id.retryButton);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        progressBar = rootView.findViewById(R.id.progressBar);

        // Gunakan ProgressBar dengan animasi yang lebih halus
        progressBar.setIndeterminate(true); // Pastikan animasi indeterminate aktif
        progressBar.setVisibility(View.GONE);
        retryButton.setEnabled(true);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isAdded() || isLoading) {
                Log.d(TAG, "Fragment not added or loading, ignoring swipe refresh");
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            Log.d(TAG, "Swipe refresh triggered");
            String query = editSearch.getText().toString().trim();
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                if (!query.isEmpty()) {
                    searchNews(query, true);
                } else {
                    loadNewsByCategory(selectedCategory, true);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
                showError("No network connection. Please try again.");
            }
        });

        retryButton.setOnClickListener(v -> {
            if (!isAdded() || isLoading) {
                Log.d(TAG, "Fragment not added or loading, ignoring retry");
                return;
            }
            Log.d(TAG, "Retry button clicked");
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                errorContainer.setVisibility(View.GONE);
                updateProgressBarVisibility(true);
                searchRecyclerView.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                String query = editSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchNews(query, false);
                } else {
                    loadNewsByCategory(selectedCategory, false);
                }
            } else {
                Log.d(TAG, "No network available");
                showError("No network connection. Please try again.");
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isAdded() || isLoading) {
                    Log.d(TAG, "Fragment not added or loading, ignoring text change");
                    return;
                }
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        searchNews(query, false);
                    } else {
                        loadNewsByCategory(selectedCategory, false);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300); // Kurangi delay menjadi 300ms
            }
        });

        loadCategoryChips();
        loadNewsByCategory(selectedCategory, false);

        return rootView;
    }

    private void loadCategoryChips() {
        if (!isAdded() || isLoading) {
            Log.d(TAG, "Fragment not added or loading, ignoring loadCategoryChips");
            return;
        }

        categoryContainer.removeAllViews();

        for (String category : categories) {
            String displayText = category.substring(0, 1).toUpperCase() + category.substring(1);
            String mappedCategory = category.equals("all") ? "general" : category;

            TextView chip = new TextView(getContext());
            chip.setText(displayText);
            chip.setTextSize(16);
            chip.setPadding(32, 24, 32, 24);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(params);

            if (mappedCategory.equals(selectedCategory)) {
                chip.setBackgroundResource(R.drawable.bg_category_selected);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.background_color));
            } else {
                chip.setBackgroundResource(R.drawable.bg_category_unselected);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_filter));
            }

            chip.setOnClickListener(v -> {
                if (!isAdded() || isLoading) {
                    Log.d(TAG, "Fragment not added or loading, ignoring chip click");
                    return;
                }
                selectedCategory = mappedCategory;
                editSearch.setText("");
                loadCategoryChips();
                loadNewsByCategory(mappedCategory, false);
            });

            categoryContainer.addView(chip);
        }
    }

    private void searchNews(String query, boolean isSwipeRefresh) {
        if (!isAdded() || isLoading) {
            Log.d(TAG, "Fragment not added or loading, ignoring searchNews");
            if (isSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        isLoading = true;
        updateProgressBarVisibility(true);
        searchRecyclerView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);

        if (isSwipeRefresh) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.d(TAG, "No network available");
            isLoading = false;
            updateProgressBarVisibility(false);
            if (isSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            showError("No network connection. Please try again.");
            return;
        }

        if (currentCall != null) {
            currentCall.cancel();
            Log.d(TAG, "Cancelled previous API call");
        }

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        currentCall = apiService.searchNews(query, Constants.API_KEY);
        currentCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API response");
                    return;
                }

                isLoading = false;
                updateProgressBarVisibility(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getArticles() != null) {
                    Log.d(TAG, "Search API response successful, articles: " + response.body().getArticles().size());
                    List<Article> articles = response.body().getArticles();
                    adapter = new NewsAdapter(getContext(), articles);
                    searchRecyclerView.setAdapter(adapter);
                    searchRecyclerView.setVisibility(View.VISIBLE);
                    errorContainer.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Search API response failed or empty, code: " + response.code());
                    showError("Failed to load news. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API failure");
                    return;
                }

                isLoading = false;
                Log.e(TAG, "Search API call failed: " + t.getMessage());
                updateProgressBarVisibility(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void loadNewsByCategory(String category, boolean isSwipeRefresh) {
        if (!isAdded() || isLoading) {
            Log.d(TAG, "Fragment not added or loading, ignoring loadNewsByCategory");
            if (isSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            return;
        }

        isLoading = true;
        updateProgressBarVisibility(true);
        searchRecyclerView.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);

        if (isSwipeRefresh) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.d(TAG, "No network available");
            isLoading = false;
            updateProgressBarVisibility(false);
            if (isSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            showError("No network connection. Please try again.");
            return;
        }

        if (currentCall != null) {
            currentCall.cancel();
            Log.d(TAG, "Cancelled previous API call");
        }

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        currentCall = apiService.getCategoryNews("us", category, Constants.API_KEY);
        currentCall.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API response");
                    return;
                }

                isLoading = false;
                updateProgressBarVisibility(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getArticles() != null) {
                    Log.d(TAG, "Category API response successful, articles: " + response.body().getArticles().size());
                    List<Article> articles = response.body().getArticles();
                    adapter = new NewsAdapter(getContext(), articles);
                    searchRecyclerView.setAdapter(adapter);
                    searchRecyclerView.setVisibility(View.VISIBLE);
                    errorContainer.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "Category API response failed or empty, code: " + response.code());
                    showError("Failed to load news. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API failure");
                    return;
                }

                isLoading = false;
                Log.e(TAG, "Category API call failed: " + t.getMessage());
                updateProgressBarVisibility(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void showError(String message) {
        if (!isAdded()) {
            Log.d(TAG, "Fragment not added, ignoring showError");
            return;
        }

        errorMessage.setText(message);
        errorContainer.setVisibility(View.VISIBLE);
        searchRecyclerView.setVisibility(View.GONE);
        updateProgressBarVisibility(false);
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateProgressBarVisibility(boolean visible) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
            });
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isAdded()) {
            loadCategoryChips();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null;
        }
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
    }
}