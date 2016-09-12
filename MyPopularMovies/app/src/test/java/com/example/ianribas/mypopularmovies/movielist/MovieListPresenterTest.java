package com.example.ianribas.mypopularmovies.movielist;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.preferences.AppPreferences;
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
import static org.junit.Assert.*;

/**
 */
public class MovieListPresenterTest {

    @Mock
    private MoviesDataSource mockDataSource;

    @Mock
    private MovieListContract.View mockView;

    @Mock
    private AppPreferences mockPreferences;

    private MovieListPresenter presenter;
    private PublishSubject<List<Movie>> mockPopularMoviesSubject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mockPopularMoviesSubject = PublishSubject.create();
        when(mockDataSource.retrievePopularMoviesRx()).thenReturn(mockPopularMoviesSubject);

        when(mockPreferences.getSortOrder()).thenReturn(AppPreferences.MOST_POPULAR);

        presenter = new MovieListPresenter(mockDataSource, mockView, mockPreferences);
    }

    @ClassRule
    public final static RxSchedulersOverrideRule mSchedulersOverrideRule = new RxSchedulersOverrideRule();

    @Test
    public void testStartShowsPopularMovies() throws Exception {

        presenter.start();
        verify(mockView).showLoading();
        verify(mockPreferences).getSortOrder();

        List<Movie> movies = new ArrayList<>();
        mockPopularMoviesSubject.onNext(movies);

        verify(mockView).showMovies(movies);
    }

    @Test
    public void testStartShowsErrorOnRepositoryError() {

        presenter.start();
        verify(mockView).showLoading();

        final IOException retrieveError = new IOException("Mock error");
        mockPopularMoviesSubject.onError(retrieveError);

        verify(mockView).showError(retrieveError);
    }

    @Test
    public void testClickOnMovieOpensMovieDetails() {

        long movieId = 111L;
        presenter.openMovieDetails(movieId);

        verify(mockView).showMovieDetailsUI(movieId);
    }

    @Test
    public void testGetSortOrder() {
        assertEquals(presenter.getSortOrder(), AppPreferences.MOST_POPULAR);

        when(mockPreferences.getSortOrder()).thenReturn(AppPreferences.TOP_RATED);
        assertEquals(presenter.getSortOrder(), AppPreferences.TOP_RATED);
    }

    @Test
    public void testSetSortOrder() {
        PublishSubject<List<Movie>> mockTopRatedMoviesSubject = PublishSubject.create();
        when(mockDataSource.retrieveTopRatedMoviesRx()).thenReturn(mockTopRatedMoviesSubject);

        presenter.setSortOrder(AppPreferences.TOP_RATED);

        verify(mockPreferences).setSortOrder(AppPreferences.TOP_RATED);

        verify(mockView).showLoading();

        List<Movie> movies = new ArrayList<>();
        mockTopRatedMoviesSubject.onNext(movies);

        verify(mockView).showMovies(movies);
    }
}