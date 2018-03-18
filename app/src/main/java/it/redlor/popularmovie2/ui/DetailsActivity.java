package it.redlor.popularmovie2.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import it.redlor.popularmovie2.R;
import it.redlor.popularmovie2.pojos.ResultMovie;



/**
 * Activity to show movie details.
 * It passes data to the Fragment
 */

public class DetailsActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    private static final String CLICKED_MOVIE = "clicked_movie";

    String mQuery;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        ResultMovie resultMovie = intent.getParcelableExtra(CLICKED_MOVIE);
        mQuery = intent.getStringExtra("search");
        System.out.println("query " + mQuery);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CLICKED_MOVIE, resultMovie);
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                        R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.details_container, detailsFragment)
                .commit();

    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
                /*if (internetAvailable()) {
                    Intent returnIntent = new Intent(DetailsActivity.this, MainActivity.class);
                    returnIntent.putExtra("query", mQuery);
                    startActivity(returnIntent);
                    return true;
                } */
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
