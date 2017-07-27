package com.meetferrytan.popularmovies.data.component;

import android.content.Context;

import com.meetferrytan.popularmovies.data.module.AppModule;
import com.meetferrytan.popularmovies.data.module.NetModule;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;

/**
 * Created by ferrytan on 7/4/17.
 */

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {
    // downstream components need these exposed with the return type
    // method name does not really matter
    Retrofit retrofit();
    Context getApplicationContext();
}