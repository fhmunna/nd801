package com.example.ianribas.mypopularmovies.moviedetail;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in specific dependencies to the
 * {@link MovieDetailPresenter}, namely the View and the movie id.
 */
@Module
public class MovieDetailPresenterModule {
    private final MovieDetailContract.View mView;
    private final long movieId;

    /**
     * This module is actually not necessary for the movie detail activity.
     * Maybe there is a better way of indicating that ...
     */
    public MovieDetailPresenterModule() {
        mView = null;
        movieId = 0;
    }

    public MovieDetailPresenterModule(MovieDetailContract.View mView, long movieId) {
        this.mView = mView;
        this.movieId = movieId;
    }

    @Provides
    MovieDetailContract.View provideMovieDetailContractView() {
        return mView;
    }

    @Provides
    long provideMovieId() {
        return movieId;
    }

    @Provides
    MovieDetailContract.Presenter provideMovieDetailContractPresenter(MovieDetailPresenter presenter) {
        return presenter;
    }
}
