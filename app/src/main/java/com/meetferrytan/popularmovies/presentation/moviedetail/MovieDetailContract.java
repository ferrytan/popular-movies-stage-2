package com.meetferrytan.popularmovies.presentation.moviedetail;

import com.meetferrytan.popularmovies.data.entity.Movie;
import com.meetferrytan.popularmovies.data.entity.Review;
import com.meetferrytan.popularmovies.data.entity.Trailer;
import com.meetferrytan.popularmovies.presentation.base.BaseContract;

import java.util.List;

public interface MovieDetailContract {

    interface View extends BaseContract.View {
        void displayMovieDetail(String id, String title, String posterImage, String year, String monthDay, String rating, String synopsys);
        void displayTrailers(List<Trailer> trailers);
        void displayReviews(List<Review> reviews);
    }

    interface Presenter extends BaseContract.Presenter<View> {
        void distributeMovieDetail(Movie movie);
        void getTrailers(String movieId);
        void getReviews(String movieId);
    }
}