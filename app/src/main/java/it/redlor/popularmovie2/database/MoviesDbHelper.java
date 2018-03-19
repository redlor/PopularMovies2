package it.redlor.popularmovie2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.redlor.popularmovie2.database.MoviesContract.FavouritesMoviesEntry;

/**
 * Helper for the Db, creates the SQLite table
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";

    private static final int DATABASE_VERSION = 1;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + FavouritesMoviesEntry.TABLE_NAME + " (" +
                        FavouritesMoviesEntry._ID + " INTEGER PRIMARY KEY," +
                        FavouritesMoviesEntry.COLUMN_NAME_TITLE + " TEXT," +
                        FavouritesMoviesEntry.COLUMN_NAME_POSTER_PATH + " TEXT," +
                        FavouritesMoviesEntry.COLUMN_NAME_VOTE_AVERAGE + " REAL," +
                        FavouritesMoviesEntry.COLUMN_NAME_RELEASE_DATE + " INTEGER," +
                        FavouritesMoviesEntry.COLUMN_NAME_OVERVIEW + " TEXT)";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavouritesMoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
