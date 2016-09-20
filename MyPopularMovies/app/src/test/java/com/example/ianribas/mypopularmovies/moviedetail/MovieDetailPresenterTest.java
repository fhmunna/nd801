package com.example.ianribas.mypopularmovies.moviedetail;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.util.RxSchedulersOverrideRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Date;

import rx.subjects.PublishSubject;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class MovieDetailPresenterTest {

    private static final long FAKE_MOVIE_ID = 465L;
    private static final Movie FAKE_MOVIE = new Movie(
            FAKE_MOVIE_ID,
            "Some title",
            "Longer overview of plot ...",
            "/POSTERPATHxNDcMjHVUZhpoCNC.jpg",
            "/BACKDROPPATHxNDcYqUZhpoCNC.jpg",
            new Date(),
            120L,
            5.1
    );


    @Mock
    private MoviesDataSource mockDataSource;

    @Mock
    private MovieDetailContract.View mockView;

    private MovieDetailPresenter presenter;
    private PublishSubject<Movie> mockMovieDetailSubject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMovieDetailSubject = PublishSubject.create();
        when(mockDataSource.details(FAKE_MOVIE_ID)).thenReturn(mockMovieDetailSubject);

        presenter = new MovieDetailPresenter(mockDataSource, mockView, FAKE_MOVIE_ID);
    }

    @ClassRule
    public final static RxSchedulersOverrideRule mSchedulersOverrideRule = new RxSchedulersOverrideRule();

    @Test
    public void testStartShowsMovieDetails() {
        presenter.start();
        verify(mockView).showLoading();

        mockMovieDetailSubject.onNext(FAKE_MOVIE);

        verify(mockView).showMovie(FAKE_MOVIE);
    }

    @Test
    public void testStartShowsErrorOnRepositoryError() {
        presenter.start();
        verify(mockView).showLoading();

        final IOException retrieveError = new IOException("Mock error");
        mockMovieDetailSubject.onError(retrieveError);

        verify(mockView).showError(retrieveError);
    }

    @Test
    public void testCanUnsubscribeFromDataRetrieval() {
        presenter.start();
        verify(mockView).showLoading();

        presenter.unsubscribe();

        mockMovieDetailSubject.onNext(FAKE_MOVIE);

        verify(mockView, never()).showMovie(FAKE_MOVIE);
    }

    @Test
    public void testPosterPath() {
        when(mockDataSource.imagePath(FAKE_MOVIE.posterPath)).thenReturn("full fake path");
        presenter.start();
        mockMovieDetailSubject.onNext(FAKE_MOVIE);

        assertNotNull(presenter.posterPath());
    }

    @Test
    public void testBackdropPath() {
        when(mockDataSource.imagePath(FAKE_MOVIE.backdropPath)).thenReturn("full fake bg path");

        presenter.start();
        mockMovieDetailSubject.onNext(FAKE_MOVIE);

        assertNotNull(presenter.backdropPath());
    }
}