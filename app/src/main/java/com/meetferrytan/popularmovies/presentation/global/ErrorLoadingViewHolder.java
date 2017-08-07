package com.meetferrytan.popularmovies.presentation.global;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meetferrytan.popularmovies.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ferrytan on 8/6/17.
 */

public class ErrorLoadingViewHolder extends RecyclerView.ViewHolder {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_EMPTY = 3;

    @BindView(R.id.txv_info)
    TextView txvInfo;
    @BindView(R.id.loader)
    ProgressBar progressBar;

    public ErrorLoadingViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindView(int state, String message) {
        switch (state) {
            case STATE_LOADING:
                txvInfo.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case STATE_ERROR:
            case STATE_EMPTY:
                txvInfo.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                txvInfo.setText(message);
                break;
        }
    }
}
