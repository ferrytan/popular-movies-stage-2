package com.meetferrytan.popularmovies.presentation.moviedetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.meetferrytan.popularmovies.R;
import com.meetferrytan.popularmovies.data.entity.Trailer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ferrytan on 7/4/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    private Context mContext;
    private List<Trailer> mData;
    private TrailerClickListener mListener;

    public TrailerAdapter(@NonNull Context context, @NonNull List<Trailer> data, @NonNull TrailerClickListener listener) {
        this.mContext = context;
        this.mData = data;
        this.mListener = listener;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_trailer, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bindView(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.image)
        ImageView mImgThumbnail;

        TrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindView(final Trailer trailer){

            Glide.with(mContext)
                    .load(trailer.getYoutubeThumbnailUrl())
                    .placeholder(R.drawable.placeholder)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mImgThumbnail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onTrailerItemClicked(view, trailer);
                }
            });
        }
    }

    public interface TrailerClickListener{
        void onTrailerItemClicked(View view, Trailer trailer);
    }
}
