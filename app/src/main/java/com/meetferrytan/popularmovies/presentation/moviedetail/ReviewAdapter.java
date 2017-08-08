package com.meetferrytan.popularmovies.presentation.moviedetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meetferrytan.popularmovies.R;
import com.meetferrytan.popularmovies.data.entity.Review;
import com.meetferrytan.popularmovies.presentation.global.ErrorLoadingViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ferrytan on 7/4/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_DATA = 0;
    public static final int TYPE_OTHER = 1;
    private Context mContext;
    private List<Review> mData;
    private int mState;

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
        notifyDataSetChanged();
    }

    public ReviewAdapter(@NonNull Context context, @NonNull List<Review> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_DATA:
                View dataView = LayoutInflater.from(mContext).inflate(R.layout.item_review, parent, false);
                return new ReviewViewHolder(dataView);
            case TYPE_OTHER:
                View errorLoadingView = LayoutInflater.from(mContext).inflate(R.layout.holder_error_loading, parent, false);
                return new ErrorLoadingViewHolder(errorLoadingView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType){
            case TYPE_DATA:
                ((ReviewViewHolder)holder).bindView(mData.get(position));
                break;
            case TYPE_OTHER:
                String message = "";
                switch (mState){
                    case ErrorLoadingViewHolder.STATE_ERROR:
                        message = mContext.getString(R.string.trailer_load_error);
                        break;
                    case ErrorLoadingViewHolder.STATE_EMPTY:
                        message = mContext.getString(R.string.trailer_empty);
                        break;
                }
                ((ErrorLoadingViewHolder)holder).bindView(mState, message);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mState> ErrorLoadingViewHolder.STATE_NORMAL){
            return TYPE_OTHER;
        }
        return TYPE_DATA;
    }

    @Override
    public int getItemCount() {
        return mData.size() + (mState>ErrorLoadingViewHolder.STATE_NORMAL?1:0);
    }

    public void updateData(List<Review> newData){
        mState = ErrorLoadingViewHolder.STATE_NORMAL;
        mData = newData;
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.txv_author)
        TextView txvAuthor;
        @BindView(R.id.txv_content)
        TextView txvContent;
        ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(final Review review){
            txvAuthor.setText(review.getAuthor());
            txvContent.setText(mContext.getString(R.string.label_review_base, review.getContent()));
        }
    }
}
