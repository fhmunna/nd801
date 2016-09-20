package com.example.ianribas.mypopularmovies.data.source;

import com.example.ianribas.mypopularmovies.data.Movie;

import java.util.List;

import rx.Observable;

/**
 */
public interface MoviesDataSource {

    Observable<List<Movie>> retrievePopularMovies();

    Observable<List<Movie>> retrieveTopRatedMovies();

    Observable<Movie> details(long id);

    String imagePath(String imagePath);
}
