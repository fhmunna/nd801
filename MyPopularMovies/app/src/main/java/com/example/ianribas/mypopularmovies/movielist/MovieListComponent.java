package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.ApplicationModule;
import com.example.ianribas.mypopularmovies.data.source.MoviesRepositoryModule;
import com.example.ianribas.mypopularmovies.preferences.PreferencesModule;

import dagger.Component;

/**
 * This is the Dagger component for the Movie List.
 */
@Component(modules = {MovieListPresenterModule.class,MoviesRepositoryModule.class,
        PreferencesModule.class, ApplicationModule.class})
public interface MovieListComponent {
    void inject(MovieListActivity activity);
}
