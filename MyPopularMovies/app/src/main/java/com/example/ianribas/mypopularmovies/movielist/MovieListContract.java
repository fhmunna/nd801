package com.example.ianribas.mypopularmovies.movielist;

import android.os.Bundle;

import com.example.ianribas.mypopularmovies.BasePresenter;
import com.example.ianribas.mypopularmovies.BaseView;
import com.example.ianribas.mypopularmovies.data.Movie;

import java.util.List;

/**
 */
public interface MovieListContract {
    interface View extends BaseView<Presenter> {

        void showMovies(List<Movie> movies);

        void showLoading();

        void showError(Throwable error);

        void showMovieDetailsUI(long movieId);
    }

    interface Presenter extends BasePresenter {

        long SELECTED_MOVIE_ID_DEFAULT = -1;

        void openMovieDetails(long movieId);

        int getSortOrder();

        void setSortOrder(int sortOrder);

        void unsubscribe();

        boolean isTwoPane();

        String posterPath(Movie movie);

        void saveState(Bundle state);

        void restoreState(Bundle state);

        long getSelectedMovieId();

        void setSelectedMovieId(long mSelectedMovieId);

        int getSelectedPosition();

        void setSelectedPosition(int mSelectedPosition);
    }
}
