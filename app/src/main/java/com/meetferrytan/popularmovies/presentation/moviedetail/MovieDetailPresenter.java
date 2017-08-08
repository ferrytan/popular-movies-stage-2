package com.meetferrytan.popularmovies.presentation.moviedetail;

import com.meetferrytan.popularmovies.BuildConfig;
import com.meetferrytan.popularmovies.PopularMoviesApp;
import com.meetferrytan.popularmovies.R;
import com.meetferrytan.popularmovies.data.entity.Movie;
import com.meetferrytan.popularmovies.data.entity.Review;
import com.meetferrytan.popularmovies.data.entity.Trailer;
import com.meetferrytan.popularmovies.presentation.base.BasePresenter;
import com.meetferrytan.popularmovies.presentation.moviedetail.repository.MovieDetailAPI;
import com.meetferrytan.popularmovies.rest.ResponseCollection;
import com.meetferrytan.popularmovies.util.AppConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static com.meetferrytan.popularmovies.util.AppConstants.ERROR_MESSAGE_DEFAULT;

public class MovieDetailPresenter extends BasePresenter<MovieDetailContract.View> implements MovieDetailContract.Presenter {
    public static final int PROCESS_DISTRIBUTE_MOVIE_DETAIL = 0;
    public static final int PROCESS_GET_TRAILERS = 1;
    public static final int PROCESS_GET_REVIEWS = 2;
    MovieDetailAPI mAPI;
    @Inject
    public MovieDetailPresenter(Retrofit retrofit) {
        mAPI = retrofit.create(MovieDetailAPI.class);
    }

    @Override
    public void distributeMovieDetail(Movie movie) {
        getView().showLoading(PROCESS_DISTRIBUTE_MOVIE_DETAIL, true);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMAT, Locale.getDefault());
            Date releaseDate = dateFormat.parse(movie.getReleaseDate());

            SimpleDateFormat dateFormatYear = new SimpleDateFormat(AppConstants.DATE_FORMAT_YEAR, Locale.getDefault());
            SimpleDateFormat dateFormatMonthDay = new SimpleDateFormat(AppConstants.DATE_FORMAT_MONTH_DAY, Locale.getDefault());

            String movieId = movie.getId();
            String movieTitle = movie.getTitle();
            String posterImageUrl = movie.getPosterImageFullUrl();
            String year = dateFormatYear.format(releaseDate);
            String monthDay = dateFormatMonthDay.format(releaseDate);
            String strRatingFormat = (PopularMoviesApp.getNetComponent().getApplicationContext()).getString(R.string.label_rating);
            String rating = String.format(strRatingFormat, movie.getRating());
            String synopsis = movie.getSynopsys();
            getView().showLoading(PROCESS_DISTRIBUTE_MOVIE_DETAIL, false);
            getView().displayMovieDetail(movieId, movieTitle, posterImageUrl, year, monthDay, rating, synopsis);
        }catch (ParseException pe) {
            pe.printStackTrace();
            getView().showError(PROCESS_DISTRIBUTE_MOVIE_DETAIL, -1, ERROR_MESSAGE_DEFAULT);
        }
    }

    @Override
    public void getTrailers(String movieId) {
        getView().showLoading(PROCESS_GET_TRAILERS, true);
        Disposable disposable = mAPI.getMovieTrailers(movieId, BuildConfig.TMDB_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseCollection<Trailer>>() {
                    @Override
                    public void accept(@NonNull ResponseCollection<Trailer> responseCollection) throws Exception {
                        getView().showLoading(PROCESS_GET_TRAILERS, false);
                        getView().displayTrailers(responseCollection.getResults());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        processError(PROCESS_GET_TRAILERS, throwable);
                    }
                });
        addDisposable(disposable);
    }

    @Override
    public void getReviews(String movieId) {

        getView().showLoading(PROCESS_GET_REVIEWS, true);
        Disposable disposable = mAPI.getMovieReviews(movieId, BuildConfig.TMDB_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseCollection<Review>>() {
                    @Override
                    public void accept(@NonNull ResponseCollection<Review> responseCollection) throws Exception {
                        getView().showLoading(PROCESS_GET_REVIEWS, false);
                        getView().displayReviews(responseCollection.getResults());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        processError(PROCESS_GET_REVIEWS, throwable);
                    }
                });
        addDisposable(disposable);
    }
}