package com.meetferrytan.popularmovies.presentation.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.meetferrytan.popularmovies.data.component.ActivityInjectorComponent;

import javax.inject.Inject;

import butterknife.ButterKnife;

public abstract class BaseActivity<P extends BaseContract.Presenter>
        extends AppCompatActivity implements BaseContract.View {
    private P mPresenter;
    protected ActivityInjectorComponent mComponent;

    protected abstract void initComponent();

    public abstract int setLayoutRes();

    public abstract void startingUpActivity(Bundle savedInstanceState);

    protected P getPresenter() {
        return mPresenter;
    }

    @Inject
    public void setPresenter(P presenter){
        this.mPresenter = presenter;
        mPresenter.attachView(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set content via create layout implement
        setContentView(setLayoutRes());
        ButterKnife.bind(this);

        initComponent();
        // init action
        startingUpActivity(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return true;
    }
}