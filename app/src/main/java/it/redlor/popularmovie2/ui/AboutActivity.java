package it.redlor.popularmovie2.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import it.redlor.popularmovie2.R;
import it.redlor.popularmovie2.databinding.ActivityAboutBinding;

/**
 * This activity shows information about the author and credits.
 */

public class AboutActivity extends AppCompatActivity {

    ActivityAboutBinding mActivityAboutBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityAboutBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
    }
}
