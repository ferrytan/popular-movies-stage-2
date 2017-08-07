package com.meetferrytan.popularmovies.presentation.moviedetail;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.meetferrytan.popularmovies.PopularMoviesApp;
import com.meetferrytan.popularmovies.R;
import com.meetferrytan.popularmovies.data.component.DaggerActivityInjectorComponent;
import com.meetferrytan.popularmovies.data.entity.Movie;
import com.meetferrytan.popularmovies.data.entity.Review;
import com.meetferrytan.popularmovies.data.entity.Trailer;
import com.meetferrytan.popularmovies.data.local.MovieDbContract;
import com.meetferrytan.popularmovies.presentation.base.BaseFragment;
import com.meetferrytan.popularmovies.presentation.global.ErrorLoadingViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MovieDetailFragment extends BaseFragment<MovieDetailPresenter>
        implements MovieDetailContract.View,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_MOVIE = "movie";
    public static final int SPECIFIC_MOVIE_LOADER_ID = 1;
    @BindView(R.id.root)
    ScrollView root;
    @BindView(R.id.txv_title)
    TextView txvTitle;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.txv_year)
    TextView txvYear;
    @BindView(R.id.txv_month_day)
    TextView txvMonthDay;
    @BindView(R.id.txv_rating)
    TextView txvRating;
    @BindView(R.id.txv_synopsis)
    TextView txvSynopsis;
    @BindView(R.id.rv_trailers)
    RecyclerView rvTrailers;
    @BindView(R.id.rv_reviews)
    RecyclerView rvReviews;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    private Movie mMovie;
    private boolean isFavorited, loaderInitialized;
    private DetailFragmentListener mListener;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    public static MovieDetailFragment newInstance(Movie movie) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
    }

    @Override
    protected void initComponent() {
        mComponent = DaggerActivityInjectorComponent.builder()
                .netComponent(PopularMoviesApp.getNetComponent())
                .build();
        mComponent.inject(this);
    }

    @Override
    protected View createLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void startingUpFragment(View view, Bundle savedInstanceState) {

        mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>(), new TrailerAdapter.TrailerClickListener() {
            @Override
            public void onTrailerItemClicked(View view, Trailer trailer) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(trailer.getYoutubeVideoUrl()));
                startActivity(i);
            }
        });
        mTrailerAdapter.setState(ErrorLoadingViewHolder.STATE_LOADING);
        rvTrailers.setAdapter(mTrailerAdapter);

        mReviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
        mReviewAdapter.setState(ErrorLoadingViewHolder.STATE_LOADING);
        rvReviews.setAdapter(mReviewAdapter);

        updateMovieData(mMovie);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            if (context instanceof DetailFragmentListener)
                mListener = (DetailFragmentListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
            // do not throw exception, because detail activity might not implement DetailFragmentListener
        }
    }

    @Override
    public void showError(int processId, int errorCode, String message) {
        // nothing to do
        showLoading(processId, false);
        switch (processId) {
            case MovieDetailPresenter.PROCESS_DISTRIBUTE_MOVIE_DETAIL:
                break;
            case MovieDetailPresenter.PROCESS_GET_TRAILERS:
                mTrailerAdapter.setState(ErrorLoadingViewHolder.STATE_ERROR);
                break;
            case MovieDetailPresenter.PROCESS_GET_REVIEWS:
                mReviewAdapter.setState(ErrorLoadingViewHolder.STATE_ERROR);
                break;
        }
    }

    @Override
    public void showLoading(int processId, boolean show) {
        switch (processId) {
            case MovieDetailPresenter.PROCESS_DISTRIBUTE_MOVIE_DETAIL:
                break;
            case MovieDetailPresenter.PROCESS_GET_TRAILERS:
                mTrailerAdapter.setState(show ? ErrorLoadingViewHolder.STATE_LOADING : ErrorLoadingViewHolder.STATE_NORMAL);
                break;
            case MovieDetailPresenter.PROCESS_GET_REVIEWS:
                mReviewAdapter.setState(show ? ErrorLoadingViewHolder.STATE_LOADING : ErrorLoadingViewHolder.STATE_NORMAL);
                break;
        }
        // nothing to do
    }

    @Override
    public void displayMovieDetail(String id, String title, String posterImage, String year, String monthDay, String rating, String synopsys) {
        txvTitle.setText(title);

        Glide.with(this)
                .load(posterImage)
                .placeholder(R.drawable.placeholder)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image);

        txvYear.setText(year);
        txvMonthDay.setText(monthDay);
        txvRating.setText(rating);
        txvSynopsis.setText(synopsys);

        getPresenter().getTrailers(id);
        getPresenter().getReviews(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loaderInitialized)
            getActivity().getSupportLoaderManager().restartLoader(SPECIFIC_MOVIE_LOADER_ID, null, this);

    }

    @Override
    public void displayTrailers(List<Trailer> trailers) {
        if (trailers.size() > 0) {
            mTrailerAdapter.setState(ErrorLoadingViewHolder.STATE_NORMAL);
            mTrailerAdapter.updateData(trailers);
        } else {
            mTrailerAdapter.setState(ErrorLoadingViewHolder.STATE_EMPTY);
        }
    }

    @Override
    public void displayReviews(List<Review> reviews) {
        if(reviews.size() > 0) {
            mReviewAdapter = new ReviewAdapter(getActivity(), reviews);
            mReviewAdapter.updateData(reviews);
        }else{
            mReviewAdapter.setState(ErrorLoadingViewHolder.STATE_EMPTY);
        }
    }

    public void updateMovieData(Movie movie) {
        mMovie = movie;
        getPresenter().distributeMovieDetail(mMovie);
        startLoader();
    }

    @OnClick(R.id.fab)
    public void onViewClicked() {

        if (isFavorited) {
            int deletedCount = getActivity().getContentResolver().delete(MovieDbContract.MovieEntry.CONTENT_URI,
                    MovieDbContract.MovieEntry.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(mMovie.getId())});

            if (deletedCount <= 0) {
                return;
            }
            Snackbar.make(root, getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_ID, mMovie.getId());
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterImagePath());
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getSynopsys());
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getRating());
            contentValues.put(MovieDbContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            Uri insertUri = getActivity().getContentResolver().insert(MovieDbContract.MovieEntry.CONTENT_URI, contentValues);

            if (insertUri == null) {
                return;
            }
            Snackbar.make(root, getString(R.string.favorite_added), Snackbar.LENGTH_SHORT).show();
        }

        if (mListener != null) mListener.onFavoriteModified();

        updateFavoriteFab(!isFavorited);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {
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

            @Override
            public Cursor loadInBackground() {
                try {
                    return getActivity().getContentResolver().query(
                            MovieDbContract.MovieEntry.CONTENT_URI,
                            null,
                            MovieDbContract.MovieEntry.COLUMN_ID + " = ?",
                            new String[]{
                                    mMovie.getId()
                            },
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            boolean favorited = data.moveToFirst();
            Log.d("test", "favorited: " + isFavorited);
//            Log.d("test", "onLoadFinished: " + data.getString(data.getColumnIndex(MovieDbContract.MovieEntry.COLUMN_TITLE)) + " favorited: " + isFavorited);
            updateFavoriteFab(favorited);
        } finally {
            data.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface DetailFragmentListener {
        void onFavoriteModified();
    }

    private void updateFavoriteFab(boolean favorited) {
        isFavorited = favorited;
        fab.setActivated(isFavorited);
        fab.setBackgroundTintList(new ColorStateList(new int[][]
                {new int[]{0}}, new int[]{ContextCompat.getColor(getActivity(), isFavorited?R.color.favorite_yellow:R.color.colorAccent)}));
    }

    private void startLoader() {
        getActivity().getSupportLoaderManager().initLoader(SPECIFIC_MOVIE_LOADER_ID, null, MovieDetailFragment.this);
        loaderInitialized = true;
    }
}
