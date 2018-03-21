package it.redlor.popularmovie2.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import it.redlor.popularmovie2.BuildConfig;
import it.redlor.popularmovie2.database.MoviesContract.FavouritesMoviesEntry;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.pojos.Review;
import it.redlor.popularmovie2.pojos.ReviewsRoot;
import it.redlor.popularmovie2.pojos.Trailer;
import it.redlor.popularmovie2.pojos.VideosRoot;
import it.redlor.popularmovie2.utils.MoviesApiInterface;

/**
 * ViewModel for the Details Screen
 */

public class MovieViewModel extends ViewModel {

    // The API key is on file in gitignore
    private static final String API_KEY = BuildConfig.API_KEY;

    MutableLiveData<ResultMovie> resultMovie;
    MutableLiveData<List<Trailer>> mTrailersList;
    MutableLiveData<List<Review>> mReviewsList;
    MutableLiveData<Trailer> trailer;
    MutableLiveData<Boolean> favourite;
    private Application application;
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
        this.favourite = new MutableLiveData<>();
        this.favourite.setValue(false);
    }

    public ResultMovie getResultMovie() {
        return resultMovie.getValue();
    }

    public void setResultMovie(ResultMovie resultMovie) {
        this.resultMovie.setValue(resultMovie);
        this.favourite.setValue(resultMovie.isFavourite());
    }

    public LiveData<List<Trailer>> getTrailers() {
        loadTrailers();
        return mTrailersList;
    }

    public LiveData<List<Review>> getReviews() {
        loadReviews();
        return mReviewsList;
    }

    public MutableLiveData<Boolean> getFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite.setValue(favourite);
        this.resultMovie.getValue().setFavourite(favourite);
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

    // Builds ContentValues
    private ContentValues createContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavouritesMoviesEntry._ID, getResultMovie().getId());
        contentValues.put(FavouritesMoviesEntry.COLUMN_NAME_TITLE, getResultMovie().getTitle());
        contentValues.put(FavouritesMoviesEntry.COLUMN_NAME_VOTE_AVERAGE, getResultMovie().getVoteAverage());
        contentValues.put(FavouritesMoviesEntry.COLUMN_NAME_RELEASE_DATE, getResultMovie().getReleaseDate());
        contentValues.put(FavouritesMoviesEntry.COLUMN_NAME_POSTER_PATH, getResultMovie().getPosterPath());
        contentValues.put(FavouritesMoviesEntry.COLUMN_NAME_OVERVIEW, getResultMovie().getOverview());

        return contentValues;
    }

    public void addFavourite(ContentResolver contentResolver) {
        mCompositeDisposable.add(Single.create((SingleEmitter<Uri> emitter) -> {
            Uri uri = contentResolver
                    .insert(FavouritesMoviesEntry.CONTENT_URI,
                            createContentValues());
            if (!emitter.isDisposed()) {
                emitter.onSuccess(uri);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (uri != null) {
                            setFavourite(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    public void removeFavourite(ContentResolver contentResolver) {
        String id = Integer.toString(getResultMovie().getId());
        final Uri uri = FavouritesMoviesEntry.CONTENT_URI
                .buildUpon()
                .appendPath(id)
                .build();

        mCompositeDisposable.add(Single.create((SingleEmitter<Integer> emitter) -> {
            int deletedMovie = contentResolver.delete(uri, null, null);
            if (!emitter.isDisposed()) {
                emitter.onSuccess(deletedMovie);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        if (integer > 0) {
                            setFavourite(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }

}
