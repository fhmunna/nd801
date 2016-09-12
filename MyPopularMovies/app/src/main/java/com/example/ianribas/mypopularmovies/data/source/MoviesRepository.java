package com.example.ianribas.mypopularmovies.data.source;

import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.example.ianribas.mypopularmovies.BuildConfig;
import com.example.ianribas.mypopularmovies.util.network.ConnectivityManagerDelegate;
import com.example.ianribas.mypopularmovies.data.Movie;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import rx.Observable;
import rx.functions.Func1;

/**
 * API delegate, providing methods to retrieve movie data. The main class of the model.
 * This is a façade to abstract how the data is actually retrieved.
 */
public class MoviesRepository implements MoviesDataSource {

    private static final String DEVICE_OFFLINE = "Device is offline.";

    static String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String TMDB_POSTER_SIZE = "w780";

    public interface MoviesAPI {

        @Headers("Content-Type: application/json")
        @GET("movie/popular?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Call<MovieListResult> popular();

        @Headers("Content-Type: application/json")
        @GET("movie/top_rated?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Call<MovieListResult> topRated();

        @Headers("Content-Type: application/json")
        @GET("movie/popular?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Observable<MovieListResult> popularRx();

        @Headers("Content-Type: application/json")
        @GET("movie/top_rated?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Observable<MovieListResult> topRatedRx();

        @Headers("Content-Type: application/json")
        @GET("movie/{id}?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Call<Movie> details(@Path("id") long id);
    }

    private MoviesAPI mService;

    // Cache for the lists and the movies.
    private static Long POPULAR_KEY = 0L;
    private static Long TOP_RATED_KEY = 1L;

    static Cache<Long, List<Movie>> movieListCache;
    static Cache<Long, Movie> movieCache;

    static {
        movieCache = CacheBuilder.newBuilder()
                .weakValues()
                .expireAfterWrite(6, TimeUnit.HOURS)
                .maximumSize(20)
                .build();

        movieListCache = CacheBuilder.newBuilder()
                .weakValues()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .maximumSize(2)
                .build();
    }

    private final Callable<List<Movie>> mPopularMoviesLoader = new Callable<List<Movie>>() {
        @Override
        public List<Movie> call() throws Exception {
            return localRetrievePopularMovies();
        }
    };

    private final Callable<List<Movie>> mTopRatedLoader = new Callable<List<Movie>>() {
        @Override
        public List<Movie> call() throws Exception {
            return localRetrieveTopRatedMovies();
        }
    };


    private final ConnectivityManagerDelegate mConnectivityManagerDelegate;

    /**
     * Factory methods.
     */
    public static MoviesRepository create(ConnectivityManager cm) {
        return new MoviesRepository(new ConnectivityManagerDelegate(cm));
    }

    public static MoviesRepository create(ConnectivityManagerDelegate cmd) {
        return new MoviesRepository(cmd);
    }

    public MoviesRepository(ConnectivityManagerDelegate cmd) {
        mConnectivityManagerDelegate = cmd;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_API_BASE_URL)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder()
                                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                        .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mService = retrofit.create(MoviesAPI.class);
    }

    @Override
    public Observable<List<Movie>> retrievePopularMoviesRx() {
        return retrieveMovieList(POPULAR_KEY);
    }

    @Override
    public Observable<List<Movie>> retrieveTopRatedMoviesRx() {
        return retrieveMovieList(TOP_RATED_KEY);
    }

    @NonNull
    private Observable<List<Movie>> retrieveMovieList(final long sortOrder) {
        List<Movie> movies = movieListCache.getIfPresent(sortOrder);

        if (movies != null) {
            return Observable.just(movies);
        } else if (!mConnectivityManagerDelegate.isOnline()) {
            return Observable.error(new IOException(DEVICE_OFFLINE));
        } else {
            Observable<MovieListResult> observable;

            if (sortOrder == POPULAR_KEY) {
                observable = mService.popularRx();
            } else {
                observable = mService.topRatedRx();
            }

            return observable.map(new Func1<MovieListResult, List<Movie>>() {
                @Override
                public List<Movie> call(MovieListResult movieListResult) {
                    movieListCache.put(sortOrder, movieListResult.results);
                    return movieListResult.results;
                }
            });
        }
    }

    @Override
    public List<Movie> retrievePopularMovies() throws IOException {
        try {
            return movieListCache.get(POPULAR_KEY, mPopularMoviesLoader);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                // Should not really happen ...
                throw new IOException(e);
            }
        }
    }

    private List<Movie> localRetrievePopularMovies() throws IOException {
        if (!mConnectivityManagerDelegate.isOnline()) {
            throw new IOException(DEVICE_OFFLINE);
        }

        final Response<MovieListResult> response = mService.popular().execute();

        return response.body().results;
    }

    public List<Movie> retrieveTopRatedMovies() throws IOException {
        try {
            return movieListCache.get(TOP_RATED_KEY, mTopRatedLoader);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                // Should not really happen ...
                throw new IOException(e);
            }
        }
    }

    private List<Movie> localRetrieveTopRatedMovies() throws IOException {
        if (!mConnectivityManagerDelegate.isOnline()) {
            throw new IOException(DEVICE_OFFLINE);
        }

        final Response<MovieListResult> response = mService.topRated().execute();

        return response.body().results;
    }

    @Override
    public Movie details(final long id) throws IOException {
        try {
            return movieCache.get(id, new Callable<Movie>() {
                @Override
                public Movie call() throws Exception {
                    return localDetails(id);
                }
            });
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                // Should not really happen ...
                throw new IOException(e);
            }
        }
    }

    private Movie localDetails(long id) throws IOException {
        if (!mConnectivityManagerDelegate.isOnline()) {
            throw new IOException(DEVICE_OFFLINE);
        }

        final Response<Movie> response = mService.details(id).execute();

        return response.body();
    }

    @Override
    public String posterPath(Movie movie) {
        return TMDB_IMAGE_BASE_URL + TMDB_POSTER_SIZE + movie.posterPath;
    }

    // Used only for testing.
    void setService(MoviesAPI service) {
        mService = service;
    }

    // Retrofit auxiliary classes.
    static class MovieListResult {
        List<Movie> results;

        public MovieListResult(List<Movie> results) {
            this.results = results;
        }
    }
}
