package it.redlor.popularmovie2.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import it.redlor.popularmovie2.R;
import it.redlor.popularmovie2.databinding.ActivityMainBinding;
import it.redlor.popularmovie2.pojos.ResultMovie;
import it.redlor.popularmovie2.viewmodel.MoviesListViewModel;
import it.redlor.popularmovie2.viewmodel.ViewModelFactory;

public class MainActivity extends AppCompatActivity implements MovieClickCallback, HasSupportFragmentInjector {

    private static final String CLICKED_MOVIE = "clicked_movie";
    private static final String SPINNER_SELECTION = "spinnerSelection";
    // Declare a variable to check if in Dual Pane mode
    public static boolean mTwoPane;
    MovieRecyclerAdapter mAdapter;
    MoviesListViewModel mViewModel;
    ActivityMainBinding mActivityMainBinding;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    ViewModelFactory mViewModelFactory;

    String mQuery;

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
/*
        if (savedInstanceState != null && mActivityMainBinding.spinner.getSelectedItemPosition() == 3) {
            mQuery = savedInstanceState.getString("query");
            System.out.println(mQuery);
        }*/

        mViewModel = ViewModelProviders.of(this, mViewModelFactory)
                .get(MoviesListViewModel.class);

        Intent returnFromDetailsIntent = getIntent();
        String returnedQuery = returnFromDetailsIntent.getStringExtra("query");
        System.out.println("returned query" + returnedQuery);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.sort_movies,
                android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mActivityMainBinding.spinner.setAdapter(arrayAdapter);

        SharedPreferences sharedPreferences = getPreferences(0);
        mActivityMainBinding.spinner.setSelection(sharedPreferences.getInt(SPINNER_SELECTION, 0));
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 3 && returnedQuery != null) {
            searchMovie(returnedQuery);
        } else {
            setOfflineUI();

        }

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
                        mViewModel.getFavourites(getContentResolver()).observe(MainActivity.this, mMoviesList -> {
                                    if (mMoviesList.size() == 0 && mActivityMainBinding.spinner.getSelectedItemPosition() == 2) {
                                         setOfflineUI();
                                    } else {
                                        setOnlineUI();
                                        processResponse(mMoviesList);

                                    }
                        });
                        retainSortOrder();
                        break;
                    case 3:
                        if (internetAvailable()) {
                            retainSortOrder();
                            if (mQuery == null) {
                                setOfflineUI();
                            } else {
                                searchMovie(mQuery);
                            }
                        } else {
                            setOfflineUI();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    // Method to set the data from the ViewModel in the RecyclerView
    private void processResponse(List<ResultMovie> moviesList) {
        int numberOfColumns = calculateNoOfColumns(getApplicationContext());
        mActivityMainBinding.moviesRv.setLayoutManager(new GridLayoutManager(MainActivity.this, numberOfColumns));
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
        if (mActivityMainBinding.spinner.getSelectedItemPosition() == 3 && internetAvailable()) {
            mActivityMainBinding.noInternetImage.setImageResource(R.drawable.ic_magnify_white_24dp);
            mActivityMainBinding.noInternetText.setText(getResources().getString(R.string.new_search));
        } else if (mActivityMainBinding.spinner.getSelectedItemPosition() == 2){
            mActivityMainBinding.noInternetImage.setVisibility(View.INVISIBLE);
            mActivityMainBinding.noInternetText.setText(getResources().getString(R.string.no_favourites));
        } else {
            mActivityMainBinding.noInternetImage.setImageResource(R.drawable.wifi);
            mActivityMainBinding.noInternetText.setText(getResources().getString(R.string.no_internet));
        }
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
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                DetailsFragment detailsFragment = new DetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(CLICKED_MOVIE, resultMovie);
                detailsFragment.setArguments(bundle);

                fragmentManager.beginTransaction()
                        .replace(R.id.details_container, detailsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            } else {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("search", mQuery);
                System.out.println("query main passing: " + mQuery);
                intent.putExtra(CLICKED_MOVIE, resultMovie);
                System.out.println(resultMovie.toString());
                startActivity(intent);
            }
        } else {
            setOfflineUI();
        }
    }

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

        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                System.out.println("query main: " + mQuery);
                searchMovie(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.about_menu_item:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchMovie(String query) {
        mActivityMainBinding.spinner.setSelection(3);
        mViewModel.getSearchedMovie(getContentResolver(), query).observe(MainActivity.this, mMoviesList -> processResponse(mMoviesList));
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
