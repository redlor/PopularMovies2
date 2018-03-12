package it.redlor.popularmovie2.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import it.redlor.popularmovie2.BuildConfig;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.pojos.Review;
import it.redlor.popularmovie2.pojos.ReviewsRoot;
import it.redlor.popularmovie2.pojos.Trailer;
import it.redlor.popularmovie2.pojos.VideosRoot;
import it.redlor.popularmovie2.service.MoviesApiInterface;

/**
 * ViewModel for the Details Screen
 */

public class MovieViewModel extends ViewModel {

    // The API key is on file in gitignore
    private static final String API_KEY = BuildConfig.API_KEY;

    MutableLiveData<ResultMovie> resultMovie;
    private Application application;
    MutableLiveData<List<Trailer>> mTrailersList;
    MutableLiveData<List<Review>> mReviewsList;
    MutableLiveData<Trailer> trailer;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private MoviesApiInterface mMoviesApiInterface;

    @Inject
    public MovieViewModel(MoviesApiInterface moviesApiInterface, Application application) {
        this.mMoviesApiInterface = moviesApiInterface;
        this.application = application;
        this.resultMovie = new MutableLiveData<>();
        this.trailer = new MutableLiveData<>();
        this.mTrailersList = new MutableLiveData<>();
        this.mReviewsList = new MutableLiveData<>();
    }

    public ResultMovie getResultMovie() {
        return resultMovie.getValue();
    }

    public void setResultMovie(ResultMovie resultMovie) {
        this.resultMovie.setValue(resultMovie);
    }

    public LiveData<List<Trailer>> getTrailers() {
        loadTrailers();
        return mTrailersList;
    }

    public LiveData<List<Review>> getReviews() {
        loadReviews();
        return mReviewsList;
    }

    private void loadTrailers() {
        mCompositeDisposable.add(mMoviesApiInterface.getVideos(
                this.resultMovie.getValue().getId(), API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VideosRoot>() {
                    @Override
                    public void accept(VideosRoot videosRoot) throws Exception {
                        mTrailersList.setValue(new ArrayList<>());
                        for (Trailer trailer : videosRoot.getTrailers()) {
                            if (videosRoot.getTrailers() != null) {
                                mTrailersList.getValue().add(trailer);
                            }
                        }
                    }

                }));
    }

    private void loadReviews() {
        mCompositeDisposable.add(mMoviesApiInterface.getReviews(
                this.resultMovie.getValue().getId(), API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<ReviewsRoot>() {
                   @Override
                   public void accept(ReviewsRoot reviewsRoot) throws Exception {
                       mReviewsList.setValue(new ArrayList<>());
                       for (Review review : reviewsRoot.getReviews()) {
                           if (reviewsRoot.getReviews() != null) {
                               mReviewsList.getValue().add(review);
                           }
                       }
                   }
               })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }

}
