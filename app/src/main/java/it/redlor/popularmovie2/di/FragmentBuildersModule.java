package it.redlor.popularmovie2.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.redlor.popularmovie2.ui.DetailsFragment;

/**
 * Module for the Fragment
 */

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract DetailsFragment bindFragmentDetails();
}
