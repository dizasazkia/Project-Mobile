package com.example.projek_mobile.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projek_mobile.BuildConfig;
import com.example.projek_mobile.R;
import com.example.projek_mobile.adapters.BreakingNewsAdapter;
import com.example.projek_mobile.adapters.NewsAdapter;
import com.example.projek_mobile.models.Article;
import com.example.projek_mobile.network.ApiClient;
import com.example.projek_mobile.network.ApiService;
import com.example.projek_mobile.utils.FixedWormDotsIndicator;
import com.example.projek_mobile.utils.HorizontalMarginItemDecoration;
import com.example.projek_mobile.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ViewPager2 viewPagerBreakingNews;
    private RecyclerView recyclerBreakingNews;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView errorMessage;
    private Button retryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout sourceFilterContainer;
    private TextView tvTrendingNews;
    private TextView tvTodaysNews;
    private NewsAdapter breakingNewsAdapter;
    private BreakingNewsAdapter breakingNewsCarouselAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Article> allArticles;
    private FixedWormDotsIndicator dotsIndicator;
    private HorizontalMarginItemDecoration viewPagerDecoration;
    private Call<com.example.projek_mobile.models.NewsResponse> currentCall;
    private String selectedSource = "All";

    private final Map<String, Integer> sourceLogos = new HashMap<String, Integer>() {{
        put("CNN", R.drawable.ic_cnn);
        put("Fox News", R.drawable.ic_fox_news);
        put("BBC News", R.drawable.ic_bbc_news);
        put("CNBC", R.drawable.ic_cnbc);
        put("ABC News", R.drawable.ic_abc_news);
        put("NBC News", R.drawable.ic_nbc_news);
        put("CBS News", R.drawable.ic_cbs_news);
        put("MSNBC", R.drawable.ic_msnbc);
        put("Associated Press", R.drawable.ic_associated_press);
        put("Reuters", R.drawable.ic_reuters);
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerBreakingNews = view.findViewById(R.id.viewPagerBreakingNews);
        dotsIndicator = view.findViewById(R.id.dotsIndicator);
        recyclerBreakingNews = view.findViewById(R.id.recyclerBreakingNews);
        progressBar = view.findViewById(R.id.progressBar);
        errorContainer = view.findViewById(R.id.errorContainer);
        errorMessage = view.findViewById(R.id.errorMessage);
        retryButton = view.findViewById(R.id.retryButton);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        sourceFilterContainer = view.findViewById(R.id.sourceFilterContainer);
        tvTrendingNews = view.findViewById(R.id.tvTrendingNews);
        tvTodaysNews = view.findViewById(R.id.tvTodaysNews);

        recyclerBreakingNews.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar.setVisibility(View.GONE);

        // Set fixed height for carousel
        viewPagerBreakingNews.post(() -> {
            if (isAdded()) {
                ViewGroup.LayoutParams params = viewPagerBreakingNews.getLayoutParams();
                params.height = getResources().getDimensionPixelSize(R.dimen.carousel_height);
                viewPagerBreakingNews.setLayoutParams(params);
            }
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isAdded()) {
                Log.d(TAG, "Swipe refresh triggered");
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    getTopHeadlines(true);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    showError("No network connection. Please try again.");
                }
            }
        });

        // Setup retry button
        retryButton.setOnClickListener(v -> {
            if (isAdded()) {
                Log.d(TAG, "Retry button clicked");
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    Log.d(TAG, "Network available, retrying...");
                    errorContainer.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    getTopHeadlines(false);
                } else {
                    Log.d(TAG, "No network available");
                    showError("No network connection. Please try again.");
                }
            } else {
                Log.d(TAG, "Fragment not attached");
            }
        });

        getTopHeadlines(false);
        return view;
    }

    private void getTopHeadlines(boolean isSwipeRefresh) {
        if (!isAdded()) {
            Log.d(TAG, "Fragment not added, aborting getTopHeadlines");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Log.d(TAG, "No network available");
            if (isSwipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                progressBar.setVisibility(View.GONE);
            }
            showError("No network connection. Please try again.");
            return;
        }

        // Hide UI elements during loading
        errorContainer.setVisibility(View.GONE);
        tvTrendingNews.setVisibility(View.GONE);
        tvTodaysNews.setVisibility(View.GONE);
        recyclerBreakingNews.setVisibility(View.GONE);
        viewPagerBreakingNews.setVisibility(View.GONE);
        dotsIndicator.setVisibility(View.GONE);
        sourceFilterContainer.setVisibility(View.GONE);

        // Show appropriate loading indicator
        if (isSwipeRefresh) {
            swipeRefreshLayout.setRefreshing(true);
            progressBar.setVisibility(View.GONE);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        // Cancel any existing call
        if (currentCall != null) {
            currentCall.cancel();
            Log.d(TAG, "Cancelled previous API call");
        }

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        currentCall = apiService.getTopHeadlines("us", BuildConfig.NEWS_API_KEY);

        currentCall.enqueue(new Callback<com.example.projek_mobile.models.NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.projek_mobile.models.NewsResponse> call, @NonNull Response<com.example.projek_mobile.models.NewsResponse> response) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API response");
                    return;
                }

                // Hide both loading indicators
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getArticles() != null) {
                    Log.d(TAG, "API response successful, articles: " + response.body().getArticles().size());
                    allArticles = response.body().getArticles();

                    // Show UI elements
                    tvTrendingNews.setVisibility(View.VISIBLE);
                    tvTodaysNews.setVisibility(View.VISIBLE);
                    recyclerBreakingNews.setVisibility(View.VISIBLE);
                    viewPagerBreakingNews.setVisibility(View.VISIBLE);
                    dotsIndicator.setVisibility(View.VISIBLE);
                    sourceFilterContainer.setVisibility(View.VISIBLE);

                    // Setup carousel
                    List<Article> sublist = allArticles.subList(0, Math.min(5, allArticles.size()));
                    breakingNewsCarouselAdapter = new BreakingNewsAdapter(getContext(), sublist);
                    viewPagerBreakingNews.setAdapter(breakingNewsCarouselAdapter);
                    dotsIndicator.setViewPager(viewPagerBreakingNews);

                    // Set layout and decoration
                    int horizontalMargin = getResources().getDimensionPixelOffset(R.dimen.viewpager_item_margin);
                    viewPagerBreakingNews.setClipToPadding(false);
                    viewPagerBreakingNews.setClipChildren(false);
                    viewPagerBreakingNews.setOffscreenPageLimit(3);
                    viewPagerBreakingNews.setPadding(40, 0, 40, 0);

                    RecyclerView recyclerView = (RecyclerView) viewPagerBreakingNews.getChildAt(0);
                    recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

                    // Remove old decoration if exists
                    if (viewPagerDecoration != null) {
                        recyclerView.removeItemDecoration(viewPagerDecoration);
                    }
                    viewPagerDecoration = new HorizontalMarginItemDecoration(horizontalMargin);
                    recyclerView.addItemDecoration(viewPagerDecoration);

                    viewPagerBreakingNews.setPageTransformer((page, position) -> {
                        float scale = 1 - Math.abs(position) * 0.1f;
                        page.setScaleX(scale);
                        page.setScaleY(scale);
                        page.setAlpha(1 - Math.abs(position) * 0.2f);
                        page.setTranslationX(-position * page.getWidth() * 0.1f);
                    });

                    // Setup auto-scroll
                    sliderRunnable = () -> {
                        if (isAdded() && breakingNewsCarouselAdapter != null && breakingNewsCarouselAdapter.getItemCount() > 0) {
                            int currentItem = viewPagerBreakingNews.getCurrentItem();
                            int nextItem = (currentItem + 1) % breakingNewsCarouselAdapter.getItemCount();
                            viewPagerBreakingNews.setCurrentItem(nextItem, true);
                            sliderHandler.postDelayed(sliderRunnable, 5000);
                        }
                    };
                    sliderHandler.postDelayed(sliderRunnable, 5000);

                    // Setup source filters and display news
                    setupSourceFilters();
                    filterNewsBySource("All");

                } else {
                    Log.d(TAG, "API response failed or empty, code: " + response.code());
                    showError("Failed to load news. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.projek_mobile.models.NewsResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    Log.d(TAG, "Fragment not added, ignoring API failure");
                    return;
                }

                Log.e(TAG, "API call failed: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
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
        recyclerBreakingNews.setVisibility(View.GONE);
        viewPagerBreakingNews.setVisibility(View.GONE);
        dotsIndicator.setVisibility(View.GONE);
        sourceFilterContainer.setVisibility(View.GONE);
        tvTrendingNews.setVisibility(View.GONE);
        tvTodaysNews.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupSourceFilters() {
        if (!isAdded()) {
            Log.d(TAG, "Fragment not added, ignoring setupSourceFilters");
            return;
        }

        sourceFilterContainer.removeAllViews();

        ImageView allLogo = new ImageView(getContext());
        allLogo.setImageResource(R.drawable.ic_all_sources);
        CardView allCard = setupSourceLogoStyle(allLogo);
        if (allCard != null) {
            allLogo.setOnClickListener(v -> {
                if (isAdded()) {
                    selectedSource = "All";
                    highlightSelectedLogo(allCard);
                    filterNewsBySource("All");
                }
            });
            sourceFilterContainer.addView(allCard);
        }

        for (Map.Entry<String, Integer> entry : sourceLogos.entrySet()) {
            String sourceName = entry.getKey();
            Integer drawableRes = entry.getValue();

            ImageView logo = new ImageView(getContext());
            logo.setImageResource(drawableRes);
            CardView cardView = setupSourceLogoStyle(logo);
            if (cardView != null) {
                logo.setOnClickListener(v -> {
                    if (isAdded()) {
                        selectedSource = sourceName;
                        highlightSelectedLogo(cardView);
                        filterNewsBySource(sourceName);
                    }
                });
                sourceFilterContainer.addView(cardView);
            }
        }
    }

    private CardView setupSourceLogoStyle(ImageView logo) {
        if (!isAdded()) {
            return null;
        }

        int size = getResources().getDimensionPixelSize(R.dimen.source_logo_size);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        logo.setLayoutParams(imageParams);
        logo.setPadding(16, 16, 16, 16);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setBackgroundResource(R.drawable.bg_unselected_filter);

        CardView cardView = new CardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(size, size);
        cardParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(size / 2f);
        cardView.setCardElevation(8f);
        cardView.setUseCompatPadding(true);
        cardView.setPreventCornerOverlap(false);
        cardView.addView(logo);
        return cardView;
    }

    private void highlightSelectedLogo(CardView selectedCard) {
        if (!isAdded() || sourceFilterContainer == null) {
            return;
        }

        for (int i = 0; i < sourceFilterContainer.getChildCount(); i++) {
            View child = sourceFilterContainer.getChildAt(i);
            if (child instanceof CardView) {
                CardView card = (CardView) child;
                if (card.getChildAt(0) instanceof ImageView) {
                    ((ImageView) card.getChildAt(0)).setBackgroundResource(R.drawable.bg_unselected_filter);
                }
            }
        }

        if (selectedCard.getChildAt(0) instanceof ImageView) {
            ((ImageView) selectedCard.getChildAt(0)).setBackgroundResource(R.drawable.bg_selected_filter);
        }
    }

    private void filterNewsBySource(String source) {
        if (!isAdded() || allArticles == null) {
            return;
        }

        List<Article> filtered = new ArrayList<>();
        List<Article> carouselArticles = new ArrayList<>();

        // Ambil artikel yang ada di carousel (5 artikel pertama)
        if (allArticles.size() >= 5) {
            carouselArticles = allArticles.subList(0, 5);
        } else if (!allArticles.isEmpty()) {
            carouselArticles = allArticles.subList(0, allArticles.size());
        }

        if (source.equals("All")) {
            filtered.addAll(allArticles);
        } else {
            for (Article article : allArticles) {
                if (article.getSource() != null && source.equals(article.getSource().getName())) {
                    filtered.add(article);
                }
            }
        }

        filtered.removeAll(carouselArticles);

        TextView noDataMessage = getView().findViewById(R.id.noDataMessage);
        if (noDataMessage == null) {
            Log.w(TAG, "noDataMessage view not found");
            return;
        }

        if (filtered.isEmpty()) {
            noDataMessage.setText("No articles available for this source");
            noDataMessage.setVisibility(View.VISIBLE);
            recyclerBreakingNews.setVisibility(View.GONE);
            viewPagerBreakingNews.setVisibility(View.VISIBLE);
            dotsIndicator.setVisibility(View.VISIBLE);
            sourceFilterContainer.setVisibility(View.VISIBLE);
            tvTrendingNews.setVisibility(View.VISIBLE);
            tvTodaysNews.setVisibility(View.VISIBLE);
        } else {
            breakingNewsAdapter = new NewsAdapter(getContext(), filtered);
            recyclerBreakingNews.setAdapter(breakingNewsAdapter);
            recyclerBreakingNews.setVisibility(View.VISIBLE);
            noDataMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isAdded()) {
            setupSourceFilters();
            if (breakingNewsAdapter != null) {
                breakingNewsAdapter.notifyDataSetChanged();
            }
            if (breakingNewsCarouselAdapter != null) {
                breakingNewsCarouselAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderRunnable != null && isAdded()) {
            sliderHandler.postDelayed(sliderRunnable, 5000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        if (sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
            sliderRunnable = null;
        }
        if (viewPagerDecoration != null && viewPagerBreakingNews != null) {
            RecyclerView recyclerView = (RecyclerView) viewPagerBreakingNews.getChildAt(0);
            if (recyclerView != null) {
                recyclerView.removeItemDecoration(viewPagerDecoration);
            }
            viewPagerDecoration = null;
        }
    }
}