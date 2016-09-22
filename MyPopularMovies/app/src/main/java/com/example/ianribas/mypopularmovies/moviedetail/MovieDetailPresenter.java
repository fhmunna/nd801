package com.example.ianribas.mypopularmovies.moviedetail;

import android.support.annotation.NonNull;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.util.test.EspressoIdlingResource;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class MovieDetailPresenter implements MovieDetailContract.Presenter {

    private final MoviesDataSource mDataSource;
    private final MovieDetailContract.View mMovieDetailView;
    private final long mMovieId;
    private Movie mMovie;
    private Subscription mSubscription;

    @Inject
    public MovieDetailPresenter(@NonNull MoviesDataSource dataSource,
                                @NonNull MovieDetailContract.View view,
                                long movieId) {
        mDataSource = checkNotNull(dataSource);
        mMovieDetailView = checkNotNull(view);
        mMovieId = movieId;
    }

    @Override
    public void start() {
        EspressoIdlingResource.increment();
        mMovieDetailView.showLoading();

        mSubscription = mDataSource
                .details(mMovieId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Movie>() {
            @Override
            public void onCompleted() {
                // Do nothing.
            }

            @Override
            public void onError(Throwable e) {
                mMovieDetailView.showError(e);
                EspressoIdlingResource.decrement();
            }

            @Override
            public void onNext(Movie movie) {
                mMovie = movie;
                mMovieDetailView.showMovie(movie);
                EspressoIdlingResource.decrement();
            }
        });
    }

    @Override
    public void unsubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public String posterPath() {
        if (mMovie != null) {
            return mDataSource.imagePath(mMovie.posterPath);
        }
        return null;
    }

    @Override
    public String backdropPath() {
        if (mMovie != null) {
            return mDataSource.imagePath(mMovie.backdropPath);
        }
        return null;
    }
}
