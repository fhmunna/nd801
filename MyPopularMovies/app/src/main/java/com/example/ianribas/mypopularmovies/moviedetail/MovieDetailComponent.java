package com.example.ianribas.mypopularmovies.moviedetail;

import com.example.ianribas.mypopularmovies.AbstractNetworkAwareActivity;
import com.example.ianribas.mypopularmovies.ApplicationModule;
import com.example.ianribas.mypopularmovies.data.source.MoviesRepositoryModule;

import dagger.Component;

/**
 * This is the Dagger component for the Movie Detail. It needs to inject i
 */
@Component(modules = {MovieDetailPresenterModule.class, MoviesRepositoryModule.class,
        ApplicationModule.class})
public interface MovieDetailComponent {
    void inject(AbstractNetworkAwareActivity activity);
    void inject(MovieDetailFragment fragment);
}
