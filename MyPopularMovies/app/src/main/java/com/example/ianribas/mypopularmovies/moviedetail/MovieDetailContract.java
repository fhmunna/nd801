package com.example.ianribas.mypopularmovies.moviedetail;

import com.example.ianribas.mypopularmovies.BasePresenter;
import com.example.ianribas.mypopularmovies.BaseView;
import com.example.ianribas.mypopularmovies.data.Movie;

/**
 */
public interface MovieDetailContract {
    interface View extends BaseView<Presenter> {

        void showMovie(Movie movie);

        void showLoading();

        void showError(Throwable error);
    }

    interface Presenter extends BasePresenter {
        void unsubscribe();

        String posterPath();
    }
}
