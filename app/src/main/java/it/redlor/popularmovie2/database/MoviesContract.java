package it.redlor.popularmovie2.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the DB
 */

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "it.redlor.popularmovie2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String FAVOURITES_PATH = "favourites";

    public MoviesContract() {
    }

    public static class FavouritesMoviesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(FAVOURITES_PATH)
                .build();

        public static final String TABLE_NAME = "favourites";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_VOTE_AVERAGE = "voteaverage";
        public static final String COLUMN_NAME_RELEASE_DATE = "releasedate";
        public static final String COLUMN_NAME_POSTER_PATH = "posterpath";
        public static final String COLUMN_NAME_OVERVIEW = "overview";

    }
}
