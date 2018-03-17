package it.redlor.popularmovie2.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static it.redlor.popularmovie2.database.MoviesContract.FavouritesMoviesEntry.TABLE_NAME;

/**
 * Created by Hp on 12/03/2018.
 */

public class MoviesContentProvider extends ContentProvider {

    public static final int CODE_FAVOURITES = 100;
    public static final int CODE_FAVOURITES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MoviesContract.FAVOURITES_PATH, CODE_FAVOURITES);
        uriMatcher.addURI(authority, MoviesContract.FAVOURITES_PATH + "/#", CODE_FAVOURITES_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case CODE_FAVOURITES:
                cursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default: throw new UnsupportedOperationException("Uri unknown: "+ uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);


        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw  new RuntimeException("Not implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues  contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri finalUri;

        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITES:
                long id = db.insert(TABLE_NAME, null, contentValues);
                if (id > 0) {
                    finalUri = ContentUris.withAppendedId(MoviesContract.FavouritesMoviesEntry.CONTENT_URI, id);
                } else  {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
                default:
                    throw new UnsupportedOperationException("Uri unknown: " +  uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);

        return finalUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int numRowsDeleted;

        switch (match) {
            case CODE_FAVOURITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                numRowsDeleted = db.delete(TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Uri unknown: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw  new RuntimeException("Not implemented");
    }
}
