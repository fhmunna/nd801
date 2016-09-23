package com.example.ianribas.mypopularmovies.movielist;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesDataSource;
import com.example.ianribas.mypopularmovies.preferences.AppPreferences;
import com.example.ianribas.mypopularmovies.util.RxSchedulersOverrideRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.subjects.PublishSubject;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
//@RunWith(RobolectricTestRunner.class)
//@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.M)
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
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockPopularMoviesSubject = PublishSubject.create();
        when(mockDataSource.retrievePopularMovies()).thenReturn(mockPopularMoviesSubject);

        when(mockPreferences.getSortOrder()).thenReturn(AppPreferences.MOST_POPULAR);

        presenter = new MovieListPresenter(mockDataSource, mockView, mockPreferences, false);
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
        assertThat(presenter.getSelectedMovieId(), is(movieId));
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
        when(mockDataSource.retrieveTopRatedMovies()).thenReturn(mockTopRatedMoviesSubject);

        presenter.setSortOrder(AppPreferences.TOP_RATED);

        verify(mockPreferences).setSortOrder(AppPreferences.TOP_RATED);

        verify(mockView).showLoading();

        List<Movie> movies = new ArrayList<>();
        mockTopRatedMoviesSubject.onNext(movies);

        verify(mockView).showMovies(movies);
    }

    @Test
    public void testSetSortOrderResetsSelectedAndPosition() {
        PublishSubject<List<Movie>> mockTopRatedMoviesSubject = PublishSubject.create();
        when(mockDataSource.retrieveTopRatedMovies()).thenReturn(mockTopRatedMoviesSubject);

        presenter.setSelectedPosition(3);
        presenter.setSelectedMovieId(666);

        assertThat(presenter.getSelectedPosition(), is(3));
        assertThat(presenter.getSelectedMovieId(), is(666L));

        presenter.setSortOrder(AppPreferences.TOP_RATED);

        assertThat(presenter.getSelectedPosition(), is(RecyclerView.NO_POSITION));
        assertThat(presenter.getSelectedMovieId(), is(MovieListPresenter.SELECTED_MOVIE_ID_DEFAULT));

        verify(mockPreferences).setSortOrder(AppPreferences.TOP_RATED);
    }

    @Test
    public void testCanUnsubscribeFromDataRetrieval() {
        presenter.start();
        verify(mockView).showLoading();
        verify(mockPreferences).getSortOrder();

        presenter.unsubscribe();

        List<Movie> movies = new ArrayList<>();
        mockPopularMoviesSubject.onNext(movies);

        verify(mockView, never()).showMovies(movies);
    }

    @Test
    public void testTwoPane() {
        assertThat(presenter.isTwoPane(), is(false));

        assertThat((new MovieListPresenter(mockDataSource, mockView, mockPreferences, true)).isTwoPane(), is(true));
    }

    @Test
    public void testSelectedMovieId() {
        assertThat(presenter.getSelectedMovieId(), is(MovieListContract.Presenter.SELECTED_MOVIE_ID_DEFAULT));

        presenter.setSelectedMovieId(11L);

        assertThat(presenter.getSelectedMovieId(), is(11L));
    }

    @Test
    public void testPosterPath() {
        final Movie fakeMovie = new Movie(-1, "fake title", null, "fake_path", "fake_bg_path", null, 0, 0.0);
        when(mockDataSource.imagePath(fakeMovie.posterPath)).thenReturn("full fake path");
        assertNotNull(presenter.posterPath(fakeMovie));
    }

    @Test
    public void testSaveState() {
        Bundle state = mock(Bundle.class);
        long movieId = 123L;

        presenter.setSelectedMovieId(movieId);
        presenter.setSelectedPosition(3);
        presenter.saveState(state);

        verify(state).putLong(MovieListPresenter.SELECTED_MOVIE_ID_KEY, movieId);
        verify(state).putInt(MovieListPresenter.SELECTED_POSITION_KEY, 3);
    }

    @Test
    public void testRestoreState() {
        Bundle state = mock(Bundle.class);
        long movieId = 123L;

        when(state.containsKey(any(String.class))).thenReturn(true);
        when(state.getInt(MovieListPresenter.SELECTED_POSITION_KEY, RecyclerView.NO_POSITION))
                .thenReturn(3);
        when(state.getLong(MovieListPresenter.SELECTED_MOVIE_ID_KEY,
                MovieListContract.Presenter.SELECTED_MOVIE_ID_DEFAULT))
                .thenReturn(movieId);

        assertThat(presenter.getSelectedMovieId(), is(MovieListContract.Presenter.SELECTED_MOVIE_ID_DEFAULT));
        assertThat(presenter.getSelectedPosition(), is(RecyclerView.NO_POSITION));

        presenter.restoreState(state);

        assertThat(presenter.getSelectedMovieId(), is(movieId));
        assertThat(presenter.getSelectedPosition(), is(3));
    }

    @Test
    public void testState() {
        Bundle state = fakeBundle();
        long movieId = 123L;

        presenter.setSelectedMovieId(movieId);
        presenter.setSelectedPosition(3);
        presenter.saveState(state);

        assertThat(state.containsKey(MovieListPresenter.SELECTED_MOVIE_ID_KEY), is(true));
        assertThat(state.getLong(MovieListPresenter.SELECTED_MOVIE_ID_KEY), is(movieId));

        assertThat(state.containsKey(MovieListPresenter.SELECTED_POSITION_KEY), is(true));
        assertThat(state.getInt(MovieListPresenter.SELECTED_POSITION_KEY), is(3));

        presenter.setSelectedMovieId(MovieListContract.Presenter.SELECTED_MOVIE_ID_DEFAULT);
        assertThat(presenter.getSelectedMovieId(), is(MovieListContract.Presenter.SELECTED_MOVIE_ID_DEFAULT));

        presenter.setSelectedPosition(RecyclerView.NO_POSITION);
        assertThat(presenter.getSelectedPosition(), is(RecyclerView.NO_POSITION));

        presenter.restoreState(state);

        assertThat(presenter.getSelectedMovieId(), is(movieId));
        assertThat(presenter.getSelectedPosition(), is(3));
    }

    @Test
    public void testFakeBundle() {
        Bundle state = fakeBundle();

        state.putInt("aaa", 4);
        assertThat(state.containsKey("aaa"), is(true));
        assertThat(state.containsKey("bbb"), is(false));
        assertThat(state.getInt("aaa"), is(4));
        assertThat(state.getInt("aaa", -1), is(4));
        assertThat(state.getInt("bbb", -1), is(-1));
    }

    private static Bundle fakeBundle() {
        return (Bundle) Enhancer.create(Bundle.class, new BundleInvocationHandler());
    }

    private static class BundleInvocationHandler implements MethodInterceptor {

        Map<Object, Object> map = new HashMap<>();

        /**
         * @param obj    "this", the enhanced object
         * @param method intercepted Method
         * @param args   argument array; primitive types are wrapped
         * @param proxy  used to invoke super (non-intercepted method); may be called
         *               as many times as needed
         */
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

            if (method.getName().startsWith("put")) {
                map.put(args[0], args[1]);
                return null;
            } else if (method.getName().startsWith("get")) {
                Object key = args[0];
                Object ret = map.get(key);
                if (ret == null && args.length > 1) {
                    ret = args[1];
                }
                return ret;
            } else if (method.getName().startsWith("containsKey")) {
                return map.containsKey(args[0]);
            }
            throw new IllegalAccessError();
        }
    }


}