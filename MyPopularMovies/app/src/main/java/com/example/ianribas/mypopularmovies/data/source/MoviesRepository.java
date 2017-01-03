package com.example.ianribas.mypopularmovies.data.source;

import android.support.annotation.NonNull;

import com.example.ianribas.mypopularmovies.BuildConfig;
import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.util.network.ConnectivityManagerDelegate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * API delegate, providing methods to retrieve movie data. The main class of the model.
 * This is a façade to abstract how the data is actually retrieved.
 */
public class MoviesRepository implements MoviesDataSource {

    private static final String DEVICE_OFFLINE = "Device is offline.";

    private static final String TMDB_API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String TMDB_POSTER_SIZE = "w185";

    interface MoviesAPI {

        @Headers("Content-Type: application/json")
        @GET("movie/popular?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Observable<MovieListResult> popular();

        @Headers("Content-Type: application/json")
        @GET("movie/top_rated?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Observable<MovieListResult> topRated();

        @Headers("Content-Type: application/json")
        @GET("movie/{id}?api_key=" + BuildConfig.THE_MOVIE_DB_API_KEY)
        Observable<Movie> details(@Path("id") long id);
    }

    private MoviesAPI mService;

    // Cache for the lists and the movies.
    private static final Long POPULAR_KEY = 0L;
    private static final Long TOP_RATED_KEY = 1L;

    static final Cache<Long, List<Movie>> movieListCache;
    static final Cache<Long, Movie> movieCache;

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

    private final ConnectivityManagerDelegate mConnectivityManagerDelegate;

    private final Action1<Movie> mCacheMovieAction  = new Action1<Movie>() {
        @Override
        public void call(Movie movie) {
            movieCache.put(movie.id, movie);
        }
    };

    @Inject
    MoviesRepository(ConnectivityManagerDelegate cmd) {
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
    public Observable<List<Movie>> retrievePopularMovies() {
        return retrieveMovieList(POPULAR_KEY);
    }

    @Override
    public Observable<List<Movie>> retrieveTopRatedMovies() {
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
                observable = mService.popular();
            } else {
                observable = mService.topRated();
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
    public Observable<Movie> details(final long id) {
        Movie movie = movieCache.getIfPresent(id);

        if (movie != null) {
            return Observable.just(movie);
        } else if (!mConnectivityManagerDelegate.isOnline()) {
            return Observable.error(new IOException(DEVICE_OFFLINE));
        } else {
            return mService.details(id).doOnNext(mCacheMovieAction);
        }
    }

    @Override
    public String imagePath(String imagePath) {
        return TMDB_IMAGE_BASE_URL + TMDB_POSTER_SIZE + imagePath;
    }

    // Used only for testing.
    void setService(MoviesAPI service) {
        mService = service;
    }

    // Retrofit auxiliary classes.
    static class MovieListResult {
        List<Movie> results;

        MovieListResult(List<Movie> results) {
            this.results = results;
        }
    }
}
