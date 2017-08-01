package com.meetferrytan.popularmovies.data.component;

import com.meetferrytan.popularmovies.presentation.moviedetail.MovieDetailActivity;
import com.meetferrytan.popularmovies.presentation.moviediscovery.MovieDiscoveryActivity;
import com.meetferrytan.popularmovies.util.ActivityScope;

import dagger.Component;

/**
 * Created by ferrytan on 7/29/17.
 */

@ActivityScope
@Component(dependencies = NetComponent.class)
public interface ActivityInjectorComponent {
    void inject(MovieDiscoveryActivity movieDiscoveryActivity);
    void inject(MovieDetailActivity movieDetailActivity);
}
