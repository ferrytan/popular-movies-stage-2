package com.meetferrytan.popularmovies.presentation.moviediscovery;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meetferrytan.popularmovies.PopularMoviesApp;
import com.meetferrytan.popularmovies.R;
import com.meetferrytan.popularmovies.data.component.DaggerActivityInjectorComponent;
import com.meetferrytan.popularmovies.data.entity.Movie;
import com.meetferrytan.popularmovies.presentation.base.BaseActivity;
import com.meetferrytan.popularmovies.presentation.moviedetail.MovieDetailActivity;
import com.meetferrytan.popularmovies.presentation.moviedetail.MovieDetailFragment;
import com.meetferrytan.popularmovies.presentation.settings.SettingsActivity;
import com.meetferrytan.popularmovies.util.AppConstants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class MovieDiscoveryActivity extends BaseActivity<MovieDiscoveryPresenter>
        implements MovieDiscoveryContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int SORT_RATING = 0;
    public static final int SORT_POPULARITY = 1;
    public static final int SPAN_COUNT_PORTRAIT = 2;
    public static final int SPAN_COUNT_LANDSCAPE = 4;

    public static final String OUTSTATE_LIST_STATE = "list_state";
    public static final String OUTSTATE_MOVIES_LIST = "movies_list";
    public static final String OUTSTATE_HAS_MORE_DATA = "has_more";

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.loader)
    ProgressBar mProgressBar;
    @BindView(R.id.txv_error)
    TextView mTxvError;

    private int sortState;
    private boolean isTwoPane;

    private MovieAdapter mMovieAdapter;

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void initComponent() {
        mComponent = DaggerActivityInjectorComponent.builder()
                .netComponent(PopularMoviesApp.getNetComponent())
                .build();
        mComponent.inject(this);
    }

    @Override
    public int setLayoutRes() {
        return R.layout.activity_movie_discovery;
    }

    @Override
    public void startingUpActivity(Bundle savedInstanceState) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_movie_discovery);

        isTwoPane = getResources().getBoolean(R.bool.two_pane);

        final int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || isTwoPane ?
                SPAN_COUNT_PORTRAIT : SPAN_COUNT_LANDSCAPE;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                MovieAdapter movieAdapter = (MovieAdapter) recyclerview.getAdapter();
                if (movieAdapter != null && movieAdapter.getItemViewType(position) == MovieAdapter.VIEWTYPE_DATA) {
                    return 1;
                } else {
                    return spanCount;
                }
            }
        });
        recyclerview.setLayoutManager(gridLayoutManager);


        sortState = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_sort_key), String.valueOf(sortState)));
        if (savedInstanceState == null) {
            loadData();
        } else {
            boolean hasMoreData = savedInstanceState.getBoolean(OUTSTATE_HAS_MORE_DATA);
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(OUTSTATE_MOVIES_LIST);

            showResult(movies, hasMoreData);
            recyclerview.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(OUTSTATE_LIST_STATE));
        }
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * show error view
     */
    @Override
    public void showError(int errorCode, String message) {
        Log.d(getClass().getSimpleName(), "showError() called with: errorCode = [" + errorCode + "], message = [" + message + "]");
        if (mMovieAdapter == null) {
            mTxvError.setVisibility(View.VISIBLE);
        } else {
            mTxvError.setVisibility(View.GONE);
        }
    }

    /**
     * show loading view
     */
    @Override
    public void showLoading(boolean show) {
        mTxvError.setVisibility(View.GONE);
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * show result to UI
     *
     * @param movies      list of result
     * @param hasMoreData indicate whether list is allowed to load more data
     */
    @Override
    public void showResult(List<Movie> movies, boolean hasMoreData) {
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(this, new ArrayList<>(movies), new MovieAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, Movie movie) {
                    Intent intent = new Intent(MovieDiscoveryActivity.this, MovieDetailActivity.class);
                    intent.putExtra(AppConstants.BUNDLE_MOVIE, movie);
                    if(isTwoPane){
                        showDetail(movie);
                    }else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptions options = ActivityOptions.
                                    makeSceneTransitionAnimation(MovieDiscoveryActivity.this, view, getString(R.string.activity_image_trans));
                            startActivity(intent, options.toBundle());
                        } else {
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onLoadMoreDataClick() {
                    loadData();
                }
            });
            recyclerview.setAdapter(mMovieAdapter);

            if(isTwoPane){
                // load first item on the list to the detail pane
                showDetail(mMovieAdapter.getItem(0));
            }
        } else {
            mMovieAdapter.addItems(movies);
        }
        mMovieAdapter.setLoadMoreEnabled(hasMoreData);
    }

    private void showDetail(Movie movie) {
        MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance(movie);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, movieDetailFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Parcelable gridInstanceState = recyclerview.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(OUTSTATE_LIST_STATE, gridInstanceState);
        if (mMovieAdapter != null) {
            outState.putParcelableArrayList(OUTSTATE_MOVIES_LIST, mMovieAdapter.getItems());
            outState.putBoolean(OUTSTATE_HAS_MORE_DATA, mMovieAdapter.isLoadMoreEnabled());
        }
    }

    @OnClick(R.id.txv_error)
    public void retryLoadingData() {
        loadData();
    }

    public void loadData() {
        switch (sortState) {
            case SORT_RATING:
                getPresenter().loadTopRatedMovies();
                break;
            case SORT_POPULARITY:
                getPresenter().loadPopularMovies();
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {

            int newSortState = Integer.parseInt(sharedPreferences.getString(key, String.valueOf(sortState)));
            if (newSortState != sortState) {
                sortState = newSortState;
                mMovieAdapter = null;
                recyclerview.setAdapter(null);
                getPresenter().reset();
                loadData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


}

