package com.meetferrytan.popularmovies.presentation.moviediscovery;

import com.meetferrytan.popularmovies.BuildConfig;
import com.meetferrytan.popularmovies.data.entity.Movie;
import com.meetferrytan.popularmovies.presentation.base.BasePresenter;
import com.meetferrytan.popularmovies.presentation.moviediscovery.repository.MovieDiscoveryAPI;
import com.meetferrytan.popularmovies.rest.ResponseCollection;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MovieDiscoveryPresenter extends BasePresenter<MovieDiscoveryContract.View> implements MovieDiscoveryContract.Presenter {
    public static final int PROCESS_LOAD_POPULAR_MOVIES = 0;
    public static final int PROCESS_LOAD_TOP_RATED_MOVIES = 1;
    private MovieDiscoveryAPI mAPI;
    private int mPage;

    @Inject
    public MovieDiscoveryPresenter(Retrofit retrofit) {
        mAPI = retrofit.create(MovieDiscoveryAPI.class);
        reset();
    }

    @Override
    public void loadPopularMovies() {
        getView().showLoading(PROCESS_LOAD_POPULAR_MOVIES, true);
        mPage++;

        Disposable disposable = mAPI.getPopularMovies(BuildConfig.TMDB_API_KEY, mPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseCollection<Movie>>() {
                    @Override
                    public void accept(@NonNull ResponseCollection<Movie> responseCollection) throws Exception {
                        boolean hasMoreData = mPage<responseCollection.getTotalPages();
                        getView().showLoading(PROCESS_LOAD_POPULAR_MOVIES, false);
                        getView().showResult(responseCollection.getResults(), hasMoreData);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        processError(PROCESS_LOAD_POPULAR_MOVIES, throwable);
                    }
                });
        addDisposable(disposable);
    }

    @Override
    public void loadTopRatedMovies() {
        getView().showLoading(PROCESS_LOAD_TOP_RATED_MOVIES, true);
        mPage++;
        Disposable disposable = mAPI.getTopRatedMovies(BuildConfig.TMDB_API_KEY, mPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseCollection<Movie>>() {
                    @Override
                    public void accept(@NonNull ResponseCollection<Movie> responseCollection) throws Exception {
                        boolean hasMoreData = mPage<responseCollection.getTotalPages();
                        getView().showLoading(PROCESS_LOAD_POPULAR_MOVIES, false);
                        getView().showResult(responseCollection.getResults(), hasMoreData);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        processError(PROCESS_LOAD_TOP_RATED_MOVIES, throwable);
                    }
                });
        addDisposable(disposable);
    }

    public void reset(){
        mPage = 0;
    }
}