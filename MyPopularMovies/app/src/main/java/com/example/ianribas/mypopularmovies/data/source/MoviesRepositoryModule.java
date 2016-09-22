package com.example.ianribas.mypopularmovies.data.source;

import dagger.Module;
import dagger.Provides;

/**
 * This is used by Dagger to inject the required arguments into the {@link MoviesRepository}.
 */
@Module
public class MoviesRepositoryModule {
    @Provides
    static MoviesDataSource provideMoviesDataSource(MoviesRepository repository) {
        return repository;
    }
}
