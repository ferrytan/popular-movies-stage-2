package com.meetferrytan.popularmovies.presentation.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meetferrytan.popularmovies.data.component.ActivityInjectorComponent;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by ferrytan on 8/2/17.
 */

public abstract class BaseFragment<P extends BaseContract.Presenter>
        extends Fragment implements BaseContract.View {
    private P mPresenter;
    protected ActivityInjectorComponent mComponent;

    protected abstract void initComponent();

    protected abstract View createLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public abstract void startingUpFragment(View view, Bundle savedInstanceState);

    protected P getPresenter() {
        return mPresenter;
    }

    @Inject
    public void setPresenter(P presenter){
        this.mPresenter = presenter;
        mPresenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // load layout
        View view = createLayout(inflater, container, savedInstanceState);

        // inject butter knife to init view
        ButterKnife.bind(this, view);

        initComponent();
        // call init action
        startingUpFragment(view, savedInstanceState);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
    }
}