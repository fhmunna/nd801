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

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import static org.mockito.Mockito.*;

public class MoviesRepositoryIntegrationTest {

    public static final String FAKE_TITLE = "fake title";
    public static final String OTHER_MOVIE = "other movie";
    public static final long FAKE_MOVIE_ID = 776L;
    public static final Movie FAKE_MOVIE = new Movie(FAKE_MOVIE_ID, FAKE_TITLE, null, null, null, 0L, 0.0);

    private MoviesRepository repository;
    private ConnectivityManagerDelegate mockConnMgrDelegate;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        mockConnMgrDelegate = mock(ConnectivityManagerDelegate.class);
        when(mockConnMgrDelegate.isOnline()).thenReturn(true);
        repository = MoviesRepository.create(mockConnMgrDelegate);
    }

    @Test
    public void testRetrievePopularMovies() throws IOException {
//        server = new MockWebServer();
//        server.start();
//
//        String str = "{\"page\": 1,\"results\": [{\"poster_path\": \"/5N20rQURev5CNDcMjHVUZhpoCNC.jpg\",\"adult\": false," +
//                "\"overview\": \"Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.\"," +
//                "\"release_date\": \"2016-04-27\",\"genre_ids\": [28,53,878],\"id\": 271110,\"original_title\": \"Captain America: Civil War\",\"original_language\": \"en\",\"title\": \"Captain America: Civil War\",\"backdrop_path\": \"/rqAHkvXldb9tHlnbQDwOzRi0yVD.jpg\",\"popularity\": 47.318703,\"vote_count\": 2796,\"video\": false,\"vote_average\": 6.93}," +
//                "{\"poster_path\": \"/e1mjopzAS2KNsvpbpahQ1a6SkSn.jpg\",\"adult\": false," +
//                "\"overview\": \"From DC Comics comes the Suicide Squad, an antihero team of incarcerated supervillains who act as deniable assets for the United States government, undertaking high-risk black ops missions in exchange for commuted prison sentences.\"," +
//                "\"release_date\": \"2016-08-03\",\"genre_ids\": [28,80,14],\"id\": 297761,\"original_title\": \"Suicide Squad\",\"original_language\": \"en\",\"title\": \"Suicide Squad\",\"backdrop_path\": \"/ndlQ2Cuc3cjTL7lTynw6I4boP4S.jpg\",\"popularity\": 31.917737,\"vote_count\": 1792,\"video\": false,\"vote_average\": 5.9}]}";
//        server.enqueue(new MockResponse().setBody(str));
//
//        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();
//
//        String oldBaseUrl =  MoviesRepository.TMDB_API_BASE_URL;
//        MoviesRepository.TMDB_API_BASE_URL = server.url("/").toString();
//
//        try {
//            repository.retrievePopularMovies().subscribe(testSubscriber);
//        } finally {
//            MoviesRepository.TMDB_API_BASE_URL = oldBaseUrl;
//            server.shutdown();
//        }

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        List<Movie> fakeMovieList = new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.popular()).thenReturn(Observable.just(new MoviesRepository.MovieListResult(fakeMovieList)));

        repository.setService(mockAPI);

        repository.retrievePopularMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        List<Movie> movies = testSubscriber.getOnNextEvents().get(0);

        assertThat(movies.get(0).id, is(FAKE_MOVIE_ID));
    }

    @Test
    public void testCachePopularMovies() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        List<Movie> fakeMovieList = new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.popular()).thenReturn(Observable.just(new MoviesRepository.MovieListResult(fakeMovieList)));

        repository.setService(mockAPI);

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        repository.retrievePopularMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        List<Movie> movies = testSubscriber.getOnNextEvents().get(0);
        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).popular();

        repository.retrievePopularMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        movies = testSubscriber.getOnNextEvents().get(0);

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).popular();
    }

    @Test
    public void testPopularMoviesFailsFastWhenOffline() throws IOException {
        when(mockConnMgrDelegate.isOnline()).thenReturn(false); // No network.

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        when(mockAPI.popular()).thenReturn(Observable.<MoviesRepository.MovieListResult>just(null));

        repository.setService(mockAPI);

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        repository.retrievePopularMovies().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertTerminalEvent();
        testSubscriber.assertError(IOException.class);

        verify(mockAPI, never()).popular();
    }

    @Test
    public void testRetrieveTopRated() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        List<Movie> fakeMovieList = new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.topRated()).thenReturn(Observable.just(new MoviesRepository.MovieListResult(fakeMovieList)));

        repository.setService(mockAPI);

        repository.retrieveTopRatedMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        List<Movie> movies = testSubscriber.getOnNextEvents().get(0);

        assertThat(movies.get(0).id, is(FAKE_MOVIE_ID));
    }

    @Test
    public void testCacheTopRated() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        List<Movie> fakeMovieList = new ArrayList<>();
        fakeMovieList.add(FAKE_MOVIE);

        when(mockAPI.topRated()).thenReturn(Observable.just(new MoviesRepository.MovieListResult(fakeMovieList)));

        repository.setService(mockAPI);

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        repository.retrieveTopRatedMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        List<Movie> movies = testSubscriber.getOnNextEvents().get(0);
        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).topRated();

        repository.retrieveTopRatedMovies().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        movies = testSubscriber.getOnNextEvents().get(0);

        assertThat(movies.size(), is(1));
        verify(mockAPI, times(1)).topRated();
    }

    @Test
    public void testTopRatedFailsFastWhenOffline() throws IOException {
        when(mockConnMgrDelegate.isOnline()).thenReturn(false); // No network.

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieListCache.invalidateAll();

        when(mockAPI.topRated()).thenReturn(Observable.<MoviesRepository.MovieListResult>just(null));

        repository.setService(mockAPI);

        TestSubscriber<List<Movie>> testSubscriber = new TestSubscriber<>();

        repository.retrieveTopRatedMovies().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertTerminalEvent();
        testSubscriber.assertError(IOException.class);

        verify(mockAPI, never()).topRated();
    }

    @Test
    public void testDetails() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieCache.invalidateAll();

        TestSubscriber<Movie> testSubscriber = new TestSubscriber<>();

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Observable.just(FAKE_MOVIE));

        repository.setService(mockAPI);

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        Movie movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie, is(FAKE_MOVIE));
    }

    @Test
    public void testFailsFastWhenOffline() throws IOException {
        when(mockConnMgrDelegate.isOnline()).thenReturn(false); // No network.

        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieCache.invalidateAll();

        TestSubscriber<Movie> testSubscriber = new TestSubscriber<>();

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Observable.just(FAKE_MOVIE));

        repository.setService(mockAPI);

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertTerminalEvent();
        testSubscriber.assertError(IOException.class);

        verify(mockAPI, never()).details(anyLong());
    }

    @Test
    public void testCacheDetails() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieCache.invalidateAll();

        TestSubscriber<Movie> testSubscriber = new TestSubscriber<>();

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Observable.just(FAKE_MOVIE));

        repository.setService(mockAPI);

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        Movie movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);

        testSubscriber = new TestSubscriber<>();

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
    }

    @Test
    public void testCacheDetailsTwoMovies() throws IOException {
        MoviesRepository.MoviesAPI mockAPI = mock(MoviesRepository.MoviesAPI.class);
        MoviesRepository.movieCache.invalidateAll();

        TestSubscriber<Movie> testSubscriber = new TestSubscriber<>();

        when(mockAPI.details(FAKE_MOVIE_ID)).thenReturn(Observable.just(FAKE_MOVIE));

        final long other_movie_id = 2L;
        Movie otherMovie = new Movie(other_movie_id, OTHER_MOVIE, null, null, null, 0L, 0.0);

        when(mockAPI.details(other_movie_id)).thenReturn(Observable.just(otherMovie));

        repository.setService(mockAPI);

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertTerminalEvent();

        Movie movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(0)).details(other_movie_id);

        testSubscriber = new TestSubscriber<>();

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);
        movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(0)).details(other_movie_id);

        testSubscriber = new TestSubscriber<>();

        repository.details(other_movie_id).subscribe(testSubscriber);
        movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(other_movie_id));
        assertThat(movie.title, is(OTHER_MOVIE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);

        testSubscriber = new TestSubscriber<>();

        repository.details(FAKE_MOVIE_ID).subscribe(testSubscriber);
        movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(FAKE_MOVIE_ID));
        assertThat(movie.title, is(FAKE_TITLE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);

        testSubscriber = new TestSubscriber<>();

        repository.details(other_movie_id).subscribe(testSubscriber);
        movie = testSubscriber.getOnNextEvents().get(0);

        assertThat(movie.id, is(other_movie_id));
        assertThat(movie.title, is(OTHER_MOVIE));
        verify(mockAPI, times(1)).details(FAKE_MOVIE_ID);
        verify(mockAPI, times(1)).details(other_movie_id);
    }
}