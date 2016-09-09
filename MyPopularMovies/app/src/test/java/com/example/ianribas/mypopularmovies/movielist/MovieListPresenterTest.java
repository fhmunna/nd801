package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.util.RxSchedulersOverrideRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.subjects.PublishSubject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class MovieListPresenterTest {

    @Mock
    private MoviesDataSource mockDataSource;

    @Mock
    private MovieListContract.View mockView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @ClassRule
    public final static RxSchedulersOverrideRule mSchedulersOverrideRule = new RxSchedulersOverrideRule();

    @Test
    public void testStartShowsPopularMovies() throws Exception {
        List<Movie> movies = new ArrayList<>();
        PublishSubject<List<Movie>> mockMoviesSubj = PublishSubject.create();
        when(mockDataSource.retrievePopularMoviesRx()).thenReturn(mockMoviesSubj);

        MovieListPresenter presenter = new MovieListPresenter(mockDataSource, mockView);
        presenter.start();
        verify(mockView).showLoading();

        mockMoviesSubj.onNext(movies);

        verify(mockView).showMovies(movies);
    }

    @Test
    public void testStartShowsErrorOnRepositoryError() {

        PublishSubject<List<Movie>> mockMoviesSubj = PublishSubject.create();
        when(mockDataSource.retrievePopularMoviesRx()).thenReturn(mockMoviesSubj);

        MovieListPresenter presenter = new MovieListPresenter(mockDataSource, mockView);
        presenter.start();
        verify(mockView).showLoading();


        final IOException retrieveError = new IOException("Mock error");
        mockMoviesSubj.onError(retrieveError);

        verify(mockView).showError(retrieveError);
    }
}