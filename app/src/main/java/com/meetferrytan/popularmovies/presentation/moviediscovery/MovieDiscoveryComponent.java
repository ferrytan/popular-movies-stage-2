package com.meetferrytan.popularmovies.presentation.moviediscovery;

import com.meetferrytan.popularmovies.data.component.NetComponent;
import com.meetferrytan.popularmovies.presentation.base.BaseContract;
import com.meetferrytan.popularmovies.util.ActivityScope;

import dagger.Component;

/**
 * Created by ferrytan on 7/4/17.
 */

@ActivityScope
@Component(dependencies = NetComponent.class)
public interface MovieDiscoveryComponent extends BaseContract.Component<MovieDiscoveryPresenter>{
    MovieDiscoveryPresenter presenter();
}
