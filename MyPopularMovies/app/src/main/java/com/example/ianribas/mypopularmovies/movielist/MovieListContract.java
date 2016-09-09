package com.example.ianribas.mypopularmovies.movielist;

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
    }

    interface Presenter extends BasePresenter {
    }
}
