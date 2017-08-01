package com.meetferrytan.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.meetferrytan.popularmovies.data.component.DaggerNetComponent;
import com.meetferrytan.popularmovies.data.component.NetComponent;
import com.meetferrytan.popularmovies.data.module.ApplicationModule;
import com.meetferrytan.popularmovies.data.module.NetworkModule;
import com.meetferrytan.popularmovies.util.AppConstants;

/**
 * Created by ferrytan on 7/4/17.
 */

public class PopularMoviesApp extends Application {
    private static NetComponent mNetComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mNetComponent = DaggerNetComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkModule(new NetworkModule(AppConstants.BASE_URL))
                .build();
        Stetho.initializeWithDefaults(this);
    }

    public static NetComponent getNetComponent() {
        return mNetComponent;
    }
}