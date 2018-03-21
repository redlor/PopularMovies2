package it.redlor.popularmovie2.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import it.redlor.popularmovie2.R;
import it.redlor.popularmovie2.databinding.ActivityMainBinding;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.ui.adapters.MovieRecyclerAdapter;
import it.redlor.popularmovie2.ui.callbacks.MovieClickCallback;
import it.redlor.popularmovie2.viewmodel.MoviesListViewModel;
import it.redlor.popularmovie2.viewmodel.ViewModelFactory;


public class MainActivity extends AppCompatActivity implements MovieClickCallback, HasSupportFragmentInjector {

    private static final String CLICKED_MOVIE = "clicked_movie";
    private static final String SPINNER_SELECTION = "spinnerSelection";
    private static final String RECYCLER_VIEW_STATE = "rvState";
    private static final String SEARCH_QUERY = "query";
    private static final String SEARCH_LIST = "list";

    // Declare a variable to check if in Dual Pane mode
    public static boolean mTwoPane;

    private String mQuery;
    private Parcelable mListState;
    private Handler mHandler;
    private SearchView mActionSearchView;
    private MenuItem mActionSearchMenuItem;
    private ArrayList<ResultMovie> mSearchSavedList;

    MovieRecyclerAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    MoviesListViewModel mViewModel;
    ActivityMainBinding mActivityMainBinding;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    ViewModelFactory mViewModelFactory;

    List<String> mSpinnerList;

    // Method to dynamically calculate how many columns should be shown
    private static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mActivityMainBinding.loadingIndicator.setVisibility(View.VISIBLE);

        // Check if on a tablet
        if (mActivityMainBinding.detailsLinearLayout != null) {
            mTwoPane = true;
        }

        if (internetAvailable()) {
            setOnlineUI();

        } else {
            setOfflineUI();
        }

        mViewModel = ViewModelProviders.of(this, mViewModelFactory)
                .get(MoviesListViewModel.class);

        mHandler = new Handler();
        mSearchSavedList = new ArrayList<ResultMovie>();

        // Restore the position of scrolling, the query in the SearchView and the list if in Search Mode
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(RECYCLER_VIEW_STATE);
            mQuery = savedInstanceState.getString(SEARCH_QUERY);
            mSearchSavedList = savedInstanceState.getParcelableArrayList(SEARCH_LIST);
        }


        // Initialize Spinner
        mSpinnerList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sort_movies)));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mSpinnerList) {
            @Override
            public boolean isEnabled(int position) {
                // Disable Search Mode option since it is not intended to be clicked
                if (position == 3) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View spinnerView = super.getDropDownView(position, convertView, parent);
                TextView spinnerTextView = (TextView) spinnerView;
                if (position == 3) {
                    //Set the disabled spinner item color fade .
                    spinnerTextView.setTextColor(getResources().getColor(R.color.inactive_spinner_item));
                } else {
                    spinnerTextView.setTextColor(Color.BLACK);
                }
                return spinnerView;
            }
        };
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActivityMainBinding.spinner.setAdapter(arrayAdapter);

        // Use Shared preferences to retain the sort order when coming back from Details Activity
        SharedPreferences sharedPreferences = getPreferences(0);
        mActivityMainBinding.spinner.setSelection(sharedPreferences.getInt(SPINNER_SELECTION, 0));

        // If the last visited tab was Search Mode, with this check we open the first tab when launching the app
        // and then set the last visited from SharedPreferences
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 3) {
            mActivityMainBinding.spinner.setSelection(0);
        }

        // Defining the spinner items behavior
        mActivityMainBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        if (internetAvailable()) {
                            setOnlineUI();
                            mViewModel.getMostPopularMoviesList(getContentResolver()).observe(MainActivity.this, mMoviesList ->
                                    processResponse(mMoviesList));
                        } else {
                            setOfflineUI();
                        }
                        retainSortOrder();

                        break;
                    case 1:
                        if (internetAvailable()) {
                            setOnlineUI();
                            mViewModel.getTopRatedMoviesList(getContentResolver()).observe(MainActivity.this, mMoviesList ->
                                    processResponse(mMoviesList));
                        } else {
                            setOfflineUI();
                        }
                        retainSortOrder();
                        break;
                    case 2:
                        refreshFavourites();
                        retainSortOrder();
                        break;
                    case 3:
                        processResponse(mSearchSavedList);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // I added the onTouchListener because the position of the RecyclerView was retained also when changing spinner selection
        // Setting the list state to null when clicking on a new tab avoids that behaviour
        mActivityMainBinding.spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mListState = null;
                return false;
            }
        });
    }

    // Method to set the data from the ViewModel in the RecyclerView
    private void processResponse(List<ResultMovie> moviesList) {
        int numberOfColumns = calculateNoOfColumns(getApplicationContext());
        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mGridLayoutManager.onRestoreInstanceState(mListState);
        mActivityMainBinding.moviesRv.setLayoutManager(mGridLayoutManager);
        mAdapter = new MovieRecyclerAdapter(moviesList, mViewModel, this);
        mActivityMainBinding.moviesRv.setAdapter(mAdapter);
        mActivityMainBinding.loadingIndicator.setVisibility(View.GONE);
    }

    public boolean internetAvailable() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void setOnlineUI() {
        mActivityMainBinding.spinner.setVisibility(View.VISIBLE);
        mActivityMainBinding.moviesRv.setVisibility(View.VISIBLE);
        mActivityMainBinding.noInternetImage.setVisibility(View.GONE);
        mActivityMainBinding.noInternetText.setVisibility(View.GONE);
    }

    private void setOfflineUI() {
        mActivityMainBinding.moviesRv.setVisibility(View.GONE);
        mActivityMainBinding.loadingIndicator.setVisibility(View.GONE);
        mActivityMainBinding.noInternetImage.setVisibility(View.VISIBLE);
        mActivityMainBinding.noInternetText.setVisibility(View.VISIBLE);
        // Shows a text if no favourites movie in the DB yet
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 2) {
            mActivityMainBinding.noInternetImage.setVisibility(View.INVISIBLE);
            mActivityMainBinding.noInternetText.setText(getResources().getString(R.string.no_favourites));
        } else {
            mActivityMainBinding.noInternetImage.setImageResource(R.drawable.wifi);
            mActivityMainBinding.noInternetText.setText(getResources().getString(R.string.no_internet));
        }
        // Re-sizes the icon when in landscape to fit the screen
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE | mActivityMainBinding.spinner.getSelectedItemPosition() == 3) {
            mActivityMainBinding.noInternetImage.getLayoutParams().height = 256;
            mActivityMainBinding.noInternetImage.getLayoutParams().width = 256;
        }
    }

    @Override
    public void onClick(ResultMovie resultMovie) {
        //If on a tablet, load the fragment in dual pane
        if (internetAvailable() | resultMovie.isFavourite()) {
            if (mTwoPane) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                DetailsFragment detailsFragment = new DetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(CLICKED_MOVIE, resultMovie);
                bundle.putInt(SPINNER_SELECTION, mActivityMainBinding.spinner.getSelectedItemPosition());
                detailsFragment.setArguments(bundle);

                fragmentManager.beginTransaction()
                        .replace(R.id.details_container, detailsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

            } else {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(CLICKED_MOVIE, resultMovie);
                startActivity(intent);
            }
        } else {
            setOfflineUI();
        }
    }

    // This method saves the last sort order selected in Shared Preferences
    private void retainSortOrder() {
        SharedPreferences.Editor editor = getPreferences(0).edit();
        int sortOrder = mActivityMainBinding.spinner.getSelectedItemPosition();
        editor.putInt(SPINNER_SELECTION, sortOrder);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        mActionSearchMenuItem = menu.findItem(R.id.search_item);
        mActionSearchView = (SearchView) mActionSearchMenuItem.getActionView();
        mActionSearchView.setIconifiedByDefault(false);

        mActionSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Save the query in a global variable in order to restore it on orientation change
                mQuery = query;
                searchMovie(query);
                return false;
            }

            // Override this method to save the text actually written in the TextView when rotating the screen
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    mQuery = newText;
                }
                    return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    // Override this method to open the SearchView if it was open before orientation change
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mQuery != null && !mQuery.isEmpty()) {
            final String query = mQuery;

            if(mHandler != null
                    && mActionSearchView != null
                    && mActionSearchMenuItem != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActionSearchMenuItem.expandActionView();
                        mActionSearchView.setQuery(query, false);
                        mActionSearchView.clearFocus();
                    }
                });
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.about_menu_item:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Search method to search for movies by title.Defined in the ViewModel
    private void searchMovie(String query) {
        mActivityMainBinding.spinner.setSelection(3);
        mViewModel.getSearchedMovie(getContentResolver(), query).observe(MainActivity.this, mMoviesList -> {
            // Since the Search Mode is not selectable but it is re-triggered on orientation change if active, I save the list in
            // a global variable to restore it
            mSearchSavedList = mMoviesList;
            processResponse(mMoviesList);} );
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // I need to refresh the Favourites onResume otherwise it won't show the change
        // if a movie was removed from Favourites
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 2) {
                refreshFavourites();
        }
    }

    // Set the empty state if no favourites in the database, otherwise sho the list
    private void refreshFavourites() {
        mViewModel.getFavourites(getContentResolver()).observe(MainActivity.this, mMoviesList -> {
            if (mMoviesList.size() == 0 && mActivityMainBinding.spinner.getSelectedItemPosition() == 2) {
                setOfflineUI();
            } else {
                setOnlineUI();
                processResponse(mMoviesList);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Saving the last scrolled position in the RecyclerView
        if (mGridLayoutManager != null) {
            outState.putParcelable(RECYCLER_VIEW_STATE, mGridLayoutManager.onSaveInstanceState());
        }
        // If the user is in Search Mode when rotating, save the list
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 3) {
            outState.putParcelableArrayList(SEARCH_LIST, mSearchSavedList);
        }
        outState.putString(SEARCH_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }
}
