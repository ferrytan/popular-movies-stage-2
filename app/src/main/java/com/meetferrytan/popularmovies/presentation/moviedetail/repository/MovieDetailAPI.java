package com.meetferrytan.popularmovies.presentation.moviedetail.repository;

import com.meetferrytan.popularmovies.data.entity.Review;
import com.meetferrytan.popularmovies.data.entity.Trailer;
import com.meetferrytan.popularmovies.rest.ResponseCollection;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ferrytan on 8/2/17.
 */

public interface MovieDetailAPI {
    @GET("movie/{movie_id}/videos")
    Observable<ResponseCollection<Trailer>> getMovieTrailers(@Path("movie_id") String id, @Query("api_key") String apiKey);
    @GET("movie/{movie_id}/reviews")
    Observable<ResponseCollection<Review>> getMovieReviews(@Path("movie_id") String id, @Query("api_key") String apiKey);
}
