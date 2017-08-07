package com.meetferrytan.popularmovies.presentation.moviediscovery;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import com.meetferrytan.popularmovies.data.local.MovieDbContract;
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
        implements MovieDiscoveryContract.View,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MovieDetailFragment.DetailFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.MovieItemClickListener {

    public static final String FRAGMENT_TAG_DETAIL = "detail_fragment";
    public static final int SORT_RATING = 0;
    public static final int SORT_POPULARITY = 1;
    public static final int SPAN_COUNT_PORTRAIT = 2;
    public static final int SPAN_COUNT_LANDSCAPE = 4;

    public static final int FAVORITE_LOADER_ID = 0;

    public static final String OUTSTATE_LIST_STATE = "list_state";
    public static final String OUTSTATE_MOVIES_LIST = "movies_list";
    public static final String OUTSTATE_HAS_MORE_DATA = "has_more";
    public static final String OUTSTATE_SHOW_FAVORITE = "show_favorite";
    public static final String OUTSTATE_SELECTED_ITEM = "selected_item";

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
    private boolean showFavorite;

    private MovieAdapter mMovieAdapter;
    private int savedSelectedIndex;

    private Handler mTransactionHandler;

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
        mTransactionHandler = new Handler();
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
            showFavorite = savedInstanceState.getBoolean(OUTSTATE_SHOW_FAVORITE);
            savedSelectedIndex = savedInstanceState.getInt(OUTSTATE_SELECTED_ITEM);
            if (showFavorite) {
                getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.title_favorite));
                reloadFavoriteData();
            } else {
                getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.pref_sort_title) + " " + getString(sortState == SORT_RATING ? R.string.pref_sort_rating_entry : R.string.pref_sort_popularity_entry));
                boolean hasMoreData = savedInstanceState.getBoolean(OUTSTATE_HAS_MORE_DATA);
                ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(OUTSTATE_MOVIES_LIST);
                showResult(movies, hasMoreData);
            }
            recyclerview.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(OUTSTATE_LIST_STATE));
        }
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showFavorite)
            reloadFavoriteData();
    }

    /**
     * show error view
     */
    @Override
    public void showError(int processId, int errorCode, String message) {
        showLoading(processId, false);
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
    public void showLoading(int processId, boolean show) {
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
            mMovieAdapter = new MovieAdapter(this, new ArrayList<>(movies), this);
            mMovieAdapter.setSelectedItemIndex(savedSelectedIndex);
            recyclerview.setAdapter(mMovieAdapter);

            if (isTwoPane) {
                // load first item on the list to the detail pane
                showDetail(mMovieAdapter.getSelectedItem());
            }
        } else {
            mMovieAdapter.addItems(movies);
        }
        mMovieAdapter.setLoadMoreEnabled(hasMoreData);
    }

    private void addDetailFragment(final Movie movie) {
        MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance(movie);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, movieDetailFragment, FRAGMENT_TAG_DETAIL)
                .commitAllowingStateLoss();
    }

    private void showDetail(Movie movie) {
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DETAIL) != null) {
            MovieDetailFragment movieDetailFragment = (MovieDetailFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DETAIL);

            if (movieDetailFragment.isInLayout())
                movieDetailFragment.updateMovieData(movie);
            else
                addDetailFragment(movie);
        } else {
            addDetailFragment(movie);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuFavorite = menu.getItem(0);
        menuFavorite.setChecked(showFavorite);
        menuFavorite.setIcon(showFavorite ? R.drawable.ic_favorite_enabled : R.drawable.ic_favorite_disabled);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            case R.id.favorite:
                showFavorite = !showFavorite;
                savedSelectedIndex = 0; // reset selected index on toggling favorite view
                if (showFavorite) {
                    getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.title_favorite));
                    reloadFavoriteData();
                } else {
                    if (mMovieAdapter == null) {
                        loadData();
                    } else {
                        getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.pref_sort_title) + " " + getString(sortState == SORT_RATING ? R.string.pref_sort_rating_entry : R.string.pref_sort_popularity_entry));
                        recyclerview.setAdapter(mMovieAdapter);
                        if (isTwoPane) showDetail(mMovieAdapter.getSelectedItem());
                    }
                }
                invalidateOptionsMenu();
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
        outState.putBoolean(OUTSTATE_SHOW_FAVORITE, showFavorite);
        outState.putInt(OUTSTATE_SELECTED_ITEM, savedSelectedIndex);
    }

    @OnClick(R.id.txv_error)
    public void retryLoadingData() {
        loadData();
    }

    public void loadData() {
        switch (sortState) {
            case SORT_RATING:
                getPresenter().loadTopRatedMovies();
                getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.pref_sort_title) + " " + getString(R.string.pref_sort_rating_entry));
                break;
            case SORT_POPULARITY:
                getPresenter().loadPopularMovies();
                getSupportActionBar().setTitle(getString(R.string.title_activity_movie_discovery) + " - " + getString(R.string.pref_sort_title) + " " + getString(R.string.pref_sort_popularity_entry));
                break;
        }
    }

    public void reloadFavoriteData() {
        getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_key))) {
            int newSortState = Integer.parseInt(sharedPreferences.getString(key, String.valueOf(sortState)));
            if (newSortState != sortState) {
                sortState = newSortState;
                mMovieAdapter = null;
                getPresenter().reset();

                if (!showFavorite) {
                    recyclerview.setAdapter(null);
                    loadData();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onFavoriteModified() {
        if (showFavorite)
            reloadFavoriteData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(MovieDbContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (showFavorite) {
            int indexId = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_ID);
            int IndexTitle = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_TITLE);
            int indexPosterPath = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_POSTER_PATH);
            int indexOverview = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_OVERVIEW);
            int indexVoteAverage = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE);
            int indexReleaseDate = data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE);

            ArrayList<Movie> favoriteMovies = new ArrayList<>();
            try {
                while (data.moveToNext()) {
                    Movie movie = new Movie();
                    movie.setId(data.getString(indexId));
                    movie.setTitle(data.getString(IndexTitle));
                    movie.setPosterImagePath(data.getString(indexPosterPath));
                    movie.setSynopsys(data.getString(indexOverview));
                    movie.setRating(data.getDouble(indexVoteAverage));
                    movie.setReleaseDate(data.getString(indexReleaseDate));

                    favoriteMovies.add(movie);
                }
            } finally {
                data.close();
            }
            MovieAdapter favoriteAdapter = new MovieAdapter(this, favoriteMovies, this);
            favoriteAdapter.setSelectedItemIndex(savedSelectedIndex);
            favoriteAdapter.setShowLoadMoreView(false);
            recyclerview.setAdapter(favoriteAdapter);
            if (isTwoPane) showDetail(favoriteAdapter.getSelectedItem());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onMovieItemClick(View view, int position, Movie movie) {
        if (isTwoPane) {
            if (savedSelectedIndex != position) {
                savedSelectedIndex = position;
                showDetail(movie);
            }
        } else {
            Intent intent = new Intent(MovieDiscoveryActivity.this, MovieDetailActivity.class);
            intent.putExtra(AppConstants.BUNDLE_MOVIE, movie);
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
}