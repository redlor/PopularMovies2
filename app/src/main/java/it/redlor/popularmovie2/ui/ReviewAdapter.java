package it.redlor.popularmovie2.ui;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.redlor.popularmovie2.BR;
import it.redlor.popularmovie2.databinding.ReviewBinding;
import it.redlor.popularmovie2.pojos.Review;

/**
 * Created by Hp on 11/03/2018.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> mReviewsList;

    public ReviewAdapter(List<Review> reviewsList) {
        mReviewsList = new ArrayList<>();
        this.mReviewsList = reviewsList;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return ReviewViewHolder.create(layoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.bind(mReviewsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mReviewsList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mViewDataBinding;

        public ReviewViewHolder(final ReviewBinding reviewBinding) {
            super(reviewBinding.getRoot());
            this.mViewDataBinding = reviewBinding;
        }

        public static ReviewViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ReviewBinding reviewBinding = ReviewBinding.inflate(inflater, parent, false);
            return new ReviewViewHolder(reviewBinding);
        }

        void bind(final Review review) {
            mViewDataBinding.setVariable(BR.review, review);
            mViewDataBinding.executePendingBindings();
        }
    }
}
