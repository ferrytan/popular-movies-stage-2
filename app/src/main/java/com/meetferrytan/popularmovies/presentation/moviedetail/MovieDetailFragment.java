package com.meetferrytan.popularmovies.presentation.moviedetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.meetferrytan.popularmovies.presentation.base.BaseFragment;

import butterknife.BindView;

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

    private Movie mMovie;

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
        if(getArguments()!=null) {
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
        getPresenter().processMovieDetail(mMovie);
    }

    @Override
    public void showError(int errorCode, String message) {
        // nothing to do
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean show) {
        // nothing to do
    }

    @Override
    public void displayMovieDetail(String title, String posterImage, String year, String monthDay, String rating, String synopsys) {
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
    }
}
