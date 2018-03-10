package it.redlor.popularmovie2.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import it.redlor.popularmovie2.viewmodel.MovieViewModel;
import it.redlor.popularmovie2.viewmodel.MoviesListViewModel;
import it.redlor.popularmovie2.viewmodel.ViewModelFactory;
import it.redlor.popularmovie2.viewmodel.ViewModelKey;

/**
 * Module that provides ViewModels
 */

@Module(includes = {ApiClientModule.class})
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MoviesListViewModel.class)
    abstract ViewModel bindListVM(MoviesListViewModel moviesListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MovieViewModel.class)
    abstract ViewModel bindMovieVM(MovieViewModel movieViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindVMFactory(ViewModelFactory viewModelFactory);
}
