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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ferrytan on 7/4/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context mContext;
    private List<Review> mData;

    public ReviewAdapter(@NonNull Context context, @NonNull List<Review> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bindView(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
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
