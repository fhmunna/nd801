package com.example.ianribas.mypopularmovies.data.source;

import com.example.ianribas.mypopularmovies.data.Movie;

import java.io.IOException;
import java.util.List;

import rx.Observable;

/**
 */
public interface MoviesDataSource {
    List<Movie> retrievePopularMovies() throws IOException;

    Observable<List<Movie>> retrievePopularMoviesRx();

    List<Movie> retrieveTopRatedMovies() throws IOException;

    Movie details(long id) throws IOException;

    String posterPath(Movie movie);
}
