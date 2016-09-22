package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.di.named.IsTwoPane;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in specific dependencies to the
 * {@link MovieListPresenter}, namely the View and if we are in two pane mode.
 */
@Module
public class MovieListPresenterModule {
    private final MovieListContract.View mView;
    private final boolean mIsTwoPane;

    public MovieListPresenterModule(MovieListContract.View mView, boolean isTwoPane) {
        this.mView = mView;
        mIsTwoPane = isTwoPane;
    }

    @Provides
    MovieListContract.View provideMovieListContractView() {
        return mView;
    }

    @Provides
    @IsTwoPane
    boolean provideIsTwoPane() {
        return mIsTwoPane;
    }

    @Provides
    MovieListContract.Presenter provideMovieListContractPresenter(MovieListPresenter presenter) {
        return presenter;
    }
}
