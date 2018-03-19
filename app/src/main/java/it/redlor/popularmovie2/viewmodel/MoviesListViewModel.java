package it.redlor.popularmovie2.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import it.redlor.popularmovie2.BuildConfig;
import it.redlor.popularmovie2.database.MoviesContract.FavouritesMoviesEntry;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.pojos.Root;
import it.redlor.popularmovie2.service.MoviesApiInterface;

/**
 * ViewModel for the list of Movies
 */

public class MoviesListViewModel extends ViewModel {

    // The API key is on file in gitignore
    private static final String API_KEY = BuildConfig.API_KEY;


    private Application mApplication;
    private MoviesApiInterface mMoviesApiInterface;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private MutableLiveData<List<ResultMovie>> mMoviesList;


    @Inject
    public MoviesListViewModel(MoviesApiInterface popularMoviesApiInterface, Application application) {
        this.mMoviesApiInterface = popularMoviesApiInterface;
        this.mApplication = application;
        mMoviesList = new MutableLiveData<>();
    }

    public LiveData<List<ResultMovie>> getMostPopularMoviesList(ContentResolver contentResolver) {
        loadMostPopularMovies(contentResolver);
        return mMoviesList;
    }

    public LiveData<List<ResultMovie>> getTopRatedMoviesList(ContentResolver contentResolver) {
        loadTopRatedMovies(contentResolver);
        return mMoviesList;
    }

    public LiveData<List<ResultMovie>> getFavourites(ContentResolver contentResolver) {
        loadFavourites(contentResolver);
        return mMoviesList;
    }

    public LiveData<List<ResultMovie>> getSearchedMovie(ContentResolver contentResolver, String query) {
        loadSearchedMovie(contentResolver, query);
        return mMoviesList;
    }

    public void setMoviesList(MutableLiveData<List<ResultMovie>> mMoviesList) {
        this.mMoviesList = mMoviesList;
    }

    //This method calls the Top Rated Movies
    private void loadTopRatedMovies(ContentResolver contentResolver) {

        mCompositeDisposable.add((Disposable) mMoviesApiInterface.getTopRatedRepo(API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Root>() {
                    @Override
                    public void accept(Root root) throws Exception {
                        mMoviesList.setValue(new ArrayList<>());
                        for (ResultMovie resultMovie : root.getResults()) {
                            if (root.getResults() != null) {
                                mMoviesList.getValue().add(resultMovie);
                                checkFavouriteStatus(resultMovie, contentResolver);
                            }
                        }

                    }
                }));
    }

    //This method calls the Most Popular Movies
    private void loadMostPopularMovies(ContentResolver contentResolver) {

        mCompositeDisposable.add((Disposable) mMoviesApiInterface.getRepository(API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Root>() {
                    @Override
                    public void accept(Root root) throws Exception {
                        mMoviesList.setValue(new ArrayList<>());
                        for (ResultMovie resultMovie : root.getResults()) {
                            if (root.getResults() != null) {
                                mMoviesList.getValue().add(resultMovie);
                                checkFavouriteStatus(resultMovie, contentResolver);
                            }
                        }
                    }
                }));
    }

    // This method loads the movie in the DB with a Cursor and RXJava
    private void loadFavourites(ContentResolver contentResolver) {
        mCompositeDisposable.add(Single.create((SingleEmitter<Cursor> emitter) -> {
            Cursor cursor = contentResolver.query(FavouritesMoviesEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (!emitter.isDisposed()) {
                emitter.onSuccess(cursor);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Cursor>() {

                    @Override
                    public void onSuccess(Cursor cursor) {
                        mMoviesList.setValue(readMovies(cursor));
                    }

                    private ArrayList<ResultMovie> readMovies(Cursor cursor) {
                        ArrayList<ResultMovie> movies = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            int id = cursor.getColumnIndex(FavouritesMoviesEntry._ID);
                            int title = cursor.getColumnIndex(FavouritesMoviesEntry.COLUMN_NAME_TITLE);
                            int voteAverage = cursor.getColumnIndex(FavouritesMoviesEntry.COLUMN_NAME_VOTE_AVERAGE);
                            int releaseDate = cursor.getColumnIndex(FavouritesMoviesEntry.COLUMN_NAME_RELEASE_DATE);
                            int posterPath = cursor.getColumnIndex(FavouritesMoviesEntry.COLUMN_NAME_POSTER_PATH);
                            int overview = cursor.getColumnIndex(FavouritesMoviesEntry.COLUMN_NAME_OVERVIEW);

                            cursor.moveToPosition(i);

                            ResultMovie resultMovie = new ResultMovie();
                            resultMovie.setId(cursor.getInt(id));
                            resultMovie.setTitle(cursor.getString(title));
                            resultMovie.setVoteAverage(cursor.getDouble(voteAverage));
                            resultMovie.setReleaseDate(cursor.getString(releaseDate));
                            resultMovie.setPosterPath(cursor.getString(posterPath));
                            resultMovie.setOverview(cursor.getString(overview));
                            resultMovie.setFavourite(true);
                            movies.add(resultMovie);
                        }
                        return movies;
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));

    }

    // This method is to check if a movie is already in the DB
    public void checkFavouriteStatus(ResultMovie resultMovie, ContentResolver resolver) {
        Cursor cursor = resolver.query(FavouritesMoviesEntry.CONTENT_URI,
                null,
                "_id=?",
                new String[]{Integer.toString(resultMovie.getId())},
                null);

        if (cursor.getCount() > 0) {
            System.out.println("true");
            resultMovie.setFavourite(true);
        } else {
            resultMovie.setFavourite(false);
        }
    }

    // This method loads the movies matching the search query
    private void loadSearchedMovie(ContentResolver contentResolver, String query) {
        mCompositeDisposable.add((Disposable) mMoviesApiInterface.getSearchedMovie(API_KEY, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Root>() {
                    @Override
                    public void accept(Root root) throws Exception {
                        mMoviesList.setValue(new ArrayList<>());
                        for (ResultMovie resultMovie : root.getResults()) {
                            if (root.getResults() != null) {
                                mMoviesList.getValue().add(resultMovie);
                                checkFavouriteStatus(resultMovie, contentResolver);
                            }
                        }
                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mCompositeDisposable.clear();
    }


}
