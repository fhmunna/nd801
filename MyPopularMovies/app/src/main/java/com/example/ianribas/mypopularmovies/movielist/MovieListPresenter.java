package com.example.ianribas.mypopularmovies.movielist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.di.named.IsTwoPane;
import com.example.ianribas.mypopularmovies.preferences.AppPreferences;
import com.example.ianribas.mypopularmovies.util.test.EspressoIdlingResource;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 */
public class MovieListPresenter implements MovieListContract.Presenter {
    private static final String SELECTED_MOVIE_ID_KEY = "selected_movie_id_key";
    private static final String SELECTED_POSITION_KEY = "selected_position_key";

    private final MoviesDataSource mDataSource;
    private final MovieListContract.View mMovieListView;
    private final AppPreferences mAppPreferences;
    private Subscription mSubscription;
    private final boolean mTwoPane;

    /**
     * The last selected movie. It is more interesting in two pane mode.
     */
    private long mSelectedMovieId = SELECTED_MOVIE_ID_DEFAULT;
    private int mSelectedPosition = RecyclerView.NO_POSITION;

    @Inject
    public MovieListPresenter(@NonNull MoviesDataSource dataSource,
                              @NonNull MovieListContract.View view,
                              @NonNull AppPreferences preferences,
                              @IsTwoPane boolean twoPane) {
        mDataSource = checkNotNull(dataSource);
        mAppPreferences = checkNotNull(preferences);
        mMovieListView = checkNotNull(view);
        mTwoPane = twoPane;

        mMovieListView.setPresenter(this);
    }

    @Override
    public void start() {
        loadMovies(mAppPreferences.getSortOrder());
    }

    private void loadMovies(int sortOrder) {
        EspressoIdlingResource.increment();
        mMovieListView.showLoading();

        Observable<List<Movie>> movies;
        if (sortOrder == AppPreferences.MOST_POPULAR) {
            movies = mDataSource.retrievePopularMovies();
        } else {
            movies = mDataSource.retrieveTopRatedMovies();
        }

        unsubscribe();
        mSubscription = movies
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Movie>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        mMovieListView.showError(e);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onNext(List<Movie> movies) {
                        mMovieListView.showMovies(movies);
                        if (mTwoPane && movies.size() > 0) {
                            if (getSelectedMovieId() == SELECTED_MOVIE_ID_DEFAULT) {
                                setSelectedMovieId(movies.get(0).id);
                            }
                            openMovieDetails(mSelectedMovieId);
                        }
                        EspressoIdlingResource.decrement();
                    }
                });
    }

    @Override
    public void openMovieDetails(long movieId) {
        setSelectedMovieId(movieId);
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

            setSelectedMovieId(SELECTED_MOVIE_ID_DEFAULT);
            setSelectedPosition(RecyclerView.NO_POSITION);

            loadMovies(sortOrder);
        }
    }

    @Override
    public void unsubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public String posterPath(Movie movie) {
        return mDataSource.imagePath(movie.posterPath);
    }

    @Override
    public void saveState(Bundle state) {
        if (mSelectedMovieId != SELECTED_MOVIE_ID_DEFAULT) {
            state.putLong(SELECTED_MOVIE_ID_KEY, mSelectedMovieId);
        }

        if (mSelectedPosition != RecyclerView.NO_POSITION) {
            state.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
        }

    }

    @Override
    public void restoreState(Bundle state) {
        if (state != null && state.containsKey(SELECTED_MOVIE_ID_KEY)) {
            mSelectedMovieId = state.getLong(SELECTED_MOVIE_ID_KEY, SELECTED_MOVIE_ID_DEFAULT);
        }

        if (state != null && state.containsKey(SELECTED_POSITION_KEY)) {
            mSelectedPosition = state.getInt(SELECTED_POSITION_KEY, RecyclerView.NO_POSITION);
        }
    }

    @Override
    public long getSelectedMovieId() {
        return mSelectedMovieId;
    }

    @Override
    public void setSelectedMovieId(long mSelectedMovieId) {
        this.mSelectedMovieId = mSelectedMovieId;
    }

    @Override
    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    @Override
    public void setSelectedPosition(int mSelectedPosition) {
        this.mSelectedPosition = mSelectedPosition;
    }
}
