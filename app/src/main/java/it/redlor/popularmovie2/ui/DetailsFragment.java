package it.redlor.popularmovie2.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import it.redlor.popularmovie2.BR;
import it.redlor.popularmovie2.R;
import it.redlor.popularmovie2.databinding.FragmentDetailsBinding;
import it.redlor.popularmovie2.di.Injectable;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.pojos.Review;
import it.redlor.popularmovie2.pojos.Trailer;
import it.redlor.popularmovie2.ui.adapters.ReviewAdapter;
import it.redlor.popularmovie2.ui.adapters.TrailerAdapter;
import it.redlor.popularmovie2.ui.callbacks.VideoClickCallback;
import it.redlor.popularmovie2.utils.SimpleDividerItemDecoration;
import it.redlor.popularmovie2.viewmodel.MovieViewModel;
import it.redlor.popularmovie2.viewmodel.ViewModelFactory;

/**
 * Fragment with Movies's details
 */

public class DetailsFragment extends Fragment implements Injectable, VideoClickCallback {

    private static final String CLICKED_MOVIE = "clicked_movie";
    private static final String SPINNER_SELECTION = "spinnerSelection";
    private static final String PATH_YOUTUBE_APP = "vnd.youtube:";
    private static final String PATH_YOUTUBE_WEB = "http://www.youtube.com/watch?v=";

    FragmentDetailsBinding fragmentDetailsBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    MovieViewModel movieViewModel;
    TrailerAdapter trailerAdapter;
    ReviewAdapter reviewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false);
        return fragmentDetailsBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = new Bundle();
        bundle = getArguments();
        ResultMovie resultMovie = bundle.getParcelable(CLICKED_MOVIE);
        int spinnerPosition = bundle.getInt(SPINNER_SELECTION);

        movieViewModel = ViewModelProviders.of(this, viewModelFactory).get(MovieViewModel.class);
        movieViewModel.setResultMovie(resultMovie);
        fragmentDetailsBinding.setVariable(BR.movieViewModel, movieViewModel);

        if (internetAvailable()) {
            movieViewModel.getTrailers().observe(this, mTrailersList -> setTrailers(mTrailersList));
            movieViewModel.getReviews().observe(this, mReviewsList -> setReviews(mReviewsList));
            fragmentDetailsBinding.reviewsLabelTv.setVisibility(View.VISIBLE);
        }

        // Check if a movie is already in the DB and change the icon accordingly
        movieViewModel.getFavourite().observe(this, favourite -> {
            if (favourite) {
                fragmentDetailsBinding.star.setImageResource(R.drawable.ic_star_white_36dp);
            } else {
                fragmentDetailsBinding.star.setImageResource(R.drawable.ic_star_outline_white_36dp);
            }
        });
        // If the icon is pressed, save or remove the movie in the DB
        fragmentDetailsBinding.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultMovie.isFavourite()) {
                    movieViewModel.removeFavourite(getActivity().getContentResolver());
                    fragmentDetailsBinding.star.setImageResource(R.drawable.ic_star_outline_white_36dp);
                } else {
                    movieViewModel.addFavourite(getActivity().getContentResolver());
                    fragmentDetailsBinding.star.setImageResource(R.drawable.ic_star_white_36dp);
                }
                // This check will update the left pane when changing favourite status in dual pane mode
                if (spinnerPosition == 2) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        getActivity().setTitle(resultMovie.getTitle());

    }

    private void setTrailers(List<Trailer> trailersList) {
        fragmentDetailsBinding.trailersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fragmentDetailsBinding.trailersRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        trailerAdapter = new TrailerAdapter(trailersList, this);
        fragmentDetailsBinding.trailersRecyclerView.setAdapter(trailerAdapter);
    }

    private void setReviews(List<Review> reviewList) {
        fragmentDetailsBinding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(reviewList);
        fragmentDetailsBinding.reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    @Override
    public void openTrailer(String key) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PATH_YOUTUBE_APP + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(PATH_YOUTUBE_WEB + key));
        try {
            this.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            this.startActivity(webIntent);
        }
    }

    public boolean internetAvailable() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
