package it.redlor.popularmovie2.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.redlor.popularmovie2.ui.DetailsActivity;
import it.redlor.popularmovie2.ui.MainActivity;
import it.redlor.popularmovie2.ui.SplashScreenActivity;

/**
 * Module for the activities
 */

@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector
    abstract DetailsActivity bindDetailsActivity();

    @ContributesAndroidInjector
    abstract SplashScreenActivity bindSplashActivity();
}
