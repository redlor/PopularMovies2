package it.redlor.popularmovie2.ui.adapters;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.redlor.popularmovie2.BR;
import it.redlor.popularmovie2.databinding.TrailerBinding;
import it.redlor.popularmovie2.pojos.Trailer;
import it.redlor.popularmovie2.ui.callbacks.VideoClickCallback;

/**
 * * This class creates an adapter for the trailers in the RecyclerView
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private VideoClickCallback mVideoClickCallback;
    private List<Trailer> mTrailersList;

    public TrailerAdapter(List<Trailer> trailersList, VideoClickCallback videoClickCallback) {
        mTrailersList = new ArrayList<>();
        this.mTrailersList = trailersList;
        this.mVideoClickCallback = videoClickCallback;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return TrailerViewHolder.create(layoutInflater, parent, mVideoClickCallback);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.bind(mTrailersList.get(position), mVideoClickCallback);
    }

    @Override
    public int getItemCount() {
        return mTrailersList.size();
    }

    static class TrailerViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mViewDataBinding;

        public TrailerViewHolder(final TrailerBinding trailerBinding, final VideoClickCallback videoClickCallback) {
            super(trailerBinding.getRoot());
            this.mViewDataBinding = trailerBinding;
            trailerBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    videoClickCallback.openTrailer(trailerBinding.getTrailer().getKey());
                }
            });
        }

        public static TrailerViewHolder create(LayoutInflater inflater, ViewGroup parent, VideoClickCallback videoClickCallback) {
            TrailerBinding trailerBinding = TrailerBinding.inflate(inflater, parent, false);
            return new TrailerViewHolder(trailerBinding, videoClickCallback);
        }

        void bind(final Trailer trailer, final VideoClickCallback videoClickCallback) {
            mViewDataBinding.setVariable(BR.trailer, trailer);
            mViewDataBinding.setVariable(BR.callback, videoClickCallback);
            mViewDataBinding.executePendingBindings();
        }
    }
}
