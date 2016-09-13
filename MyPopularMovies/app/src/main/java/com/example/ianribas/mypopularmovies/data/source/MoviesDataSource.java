package com.example.ianribas.mypopularmovies.data.source;

import com.example.ianribas.mypopularmovies.data.Movie;

import java.io.IOException;
import java.util.List;

import rx.Observable;

/**
 */
public interface MoviesDataSource {

    Observable<List<Movie>> retrievePopularMoviesRx();

    Observable<List<Movie>> retrieveTopRatedMoviesRx();

    Movie details(long id) throws IOException;

    String posterPath(Movie movie);
}
