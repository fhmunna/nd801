package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class MovieListPresenter implements MovieListContract.Presenter {
    private final MoviesDataSource mDataSource;
    private final MovieListContract.View mMovieListView;

    public MovieListPresenter(@NotNull MoviesDataSource dataSource,
                              @NotNull MovieListContract.View view) {
        mDataSource = checkNotNull(dataSource);
        mMovieListView = checkNotNull(view);

        mMovieListView.setPresenter(this);
    }

    @Override
    public void start() {
        mMovieListView.showLoading();
        mDataSource
                .retrievePopularMoviesRx()
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
}
