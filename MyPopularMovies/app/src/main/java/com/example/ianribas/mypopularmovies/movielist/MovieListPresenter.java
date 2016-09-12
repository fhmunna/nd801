package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.preferences.AppPreferences;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class MovieListPresenter implements MovieListContract.Presenter {
    private final MoviesDataSource mDataSource;
    private final MovieListContract.View mMovieListView;
    private final AppPreferences mAppPreferences;

    public MovieListPresenter(@NotNull MoviesDataSource dataSource,
                              @NotNull MovieListContract.View view,
                              @NotNull AppPreferences preferences) {
        mDataSource = checkNotNull(dataSource);
        mAppPreferences = checkNotNull(preferences);
        mMovieListView = checkNotNull(view);

        mMovieListView.setPresenter(this);
    }

    @Override
    public void start() {
        loadMovies(mAppPreferences.getSortOrder());
    }

    private void loadMovies(int sortOrder) {
        mMovieListView.showLoading();

        Observable<List<Movie>> movies;
        if (sortOrder == AppPreferences.MOST_POPULAR) {
            movies = mDataSource.retrievePopularMoviesRx();
        } else {
            movies = mDataSource.retrieveTopRatedMoviesRx();
        }

        movies
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Movie>>() {
                    @Override
                    public void onCompleted() {
                        // Do nothing ...
                    }

                    @Override
                    public void onError(Throwable e) {
                        mMovieListView.showError(e);
                    }

                    @Override
                    public void onNext(List<Movie> movies) {
                        mMovieListView.showMovies(movies);
                    }
                });
    }

    @Override
    public void openMovieDetails(long movieId) {
        mMovieListView.showMovieDetailsUI(movieId);
    }

    @Override
    public int getSortOrder() {
        return mAppPreferences.getSortOrder();
    }

    @Override
    public void setSortOrder(int sortOrder) {
        if (sortOrder != mAppPreferences.getSortOrder()) {
            mAppPreferences.setSortOrder(sortOrder);

            loadMovies(sortOrder);
        }
    }
}
