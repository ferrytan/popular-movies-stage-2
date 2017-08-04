package com.meetferrytan.popularmovies.presentation.moviedetail;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MovieDetailFragment extends BaseFragment<MovieDetailPresenter>
        implements MovieDetailContract.View {
    public static final String ARG_MOVIE = "movie";
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
    private boolean isFavorited;

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
        updateMovieData(mMovie);
    }

    @Override
    public void showError(int processId, int errorCode, String message) {
        // nothing to do
        showLoading(processId, false);
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(int processId, boolean show) {
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
    public void displayTrailers(List<Trailer> trailers) {
        TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), trailers, new TrailerAdapter.TrailerClickListener() {
            @Override
            public void onTrailerItemClicked(View view, Trailer trailer) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(trailer.getYoutubeVideoUrl()));
                startActivity(i);
            }
        });

        rvTrailers.setAdapter(trailerAdapter);
    }

    @Override
    public void displayReviews(List<Review> reviews) {
        ReviewAdapter reviewAdapter = new ReviewAdapter(getActivity(), reviews);
        rvReviews.setAdapter(reviewAdapter);
    }

    public void updateMovieData(Movie movie) {
        mMovie = movie;
        getPresenter().distributeMovieDetail(mMovie);
    }

    @OnClick(R.id.fab)
    public void onViewClicked() {

        if(isFavorited){

        }else {
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
            Toast.makeText(getActivity(), insertUri.toString(), Toast.LENGTH_SHORT).show();
        }

        isFavorited = !isFavorited;
        fab.setActivated(isFavorited);
    }
}
