package com.example.ianribas.mypopularmovies.data.source;

import com.example.ianribas.mypopularmovies.util.network.ConnectivityManagerDelegate;
import com.example.ianribas.mypopularmovies.data.Movie;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.mock.Calls;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import static org.mockito.Mockito.*;

public class MoviesRepositoryIntegrationTest {

    public static final String FAKE_TITLE = "fake title";
    public static final String OTHER_MOVIE = "other movie";
    public static final long FAKE_MOVIE_ID = -1L;
    public static final Movie FAKE_MOVIE = new Movie(FAKE_MOVIE_ID, FAKE_TITLE, null, null, null, 0L, 0.0);

    private MoviesRepository delegate;
    private ConnectivityManagerDelegate mockConnMgrDelegate;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        mockConnMgrDelegate = mock(ConnectivityManagerDelegate.class);
        when(mockConnMgrDelegate.isOnline()).thenReturn(true);
        delegate = MoviesRepository.create(mockConnMgrDelegate);
    }

    @Test
    public void testRetrievePopularMovies() throws IOException {

        List<Movie> movies = delegate.retrievePopularMovies();

        assertThat(movies.size(), greaterThan(0));
    }

    @Test
    public void testRetrieveTopRatedMovies() throws IOException {

        List<Movie> movies = delegate.retrieveTopRatedMovies();

        assertThat(movies.size(), greaterThan(0));
    }

    @Test
    public void testDetails() throws IOException {

        Movie movie = delegate.details(278);

        assertThat(movie.id, is(278L));
        assertThat(movie.title, is("The Shawshank Redemption"));
    }

    @Test
    public void testFailsFastWhenOffline() throws IOException {
        when(mockConnMgrDelegate.isOnline()).thenReturn(false); // No network.

        MoviesRepository.movieCache.invalidateAll();

        exception.expect(IOException.class);
        delegate.details(278);
    }

    @Test
    public void testCachePopularMovies() throws IOException {

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);

        List<Movie> fakeMovieList =  new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.popular()).thenReturn(Calls.response(new MoviesRepository.MovieListResult(fakeMovieList)));

        delegate.setService(mockAPI);

        List<Movie> movies = delegate.retrievePopularMovies();

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).popular();

        movies = delegate.retrievePopularMovies();

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).popular();
    }

    @Test
    public void testCacheTopRated() throws IOException {

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);

        List<Movie> fakeMovieList =  new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.topRated()).thenReturn(Calls.response(new MoviesRepository.MovieListResult(fakeMovieList)));

        delegate.setService(mockAPI);

        List<Movie> movies = delegate.retrieveTopRatedMovies();

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).topRated();

        movies = delegate.retrieveTopRatedMovies();

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).topRated();
    }

    @Test
    public void testCacheDetails() throws IOException {

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Calls.response(FAKE_MOVIE));

        delegate.setService(mockAPI);

        Movie movie = delegate.details(FAKE_MOVIE_ID);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);

        movie = delegate.details(FAKE_MOVIE_ID);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
    }

    @Test
    public void testCacheDetailsTwoMovies() throws IOException {

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieCache.invalidateAll();

        final long other_movie_id = 2L;
        Movie otherMovie = new Movie(other_movie_id, OTHER_MOVIE, null, null, null, 0L, 0.0);

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Calls.response(FAKE_MOVIE));
        when(mockAPI.details(other_movie_id)).thenReturn(Calls.response(otherMovie));

        delegate.setService(mockAPI);

        Movie movie = delegate.details(FAKE_MOVIE_ID);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(0)).details(other_movie_id);

        movie = delegate.details(FAKE_MOVIE_ID);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(0)).details(other_movie_id);

        movie = delegate.details(other_movie_id);

        assertThat(movie.id, is(other_movie_id));
        assertThat(movie.title, is(OTHER_MOVIE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);

        movie = delegate.details(FAKE_MOVIE_ID);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);

        movie = delegate.details(other_movie_id);

        assertThat(movie.id, is(other_movie_id));
        assertThat(movie.title, is(OTHER_MOVIE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);
    }
}