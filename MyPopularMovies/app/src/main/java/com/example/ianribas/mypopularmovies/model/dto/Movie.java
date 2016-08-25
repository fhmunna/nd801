package com.example.ianribas.mypopularmovies.model.dto;

import java.util.Date;

public class Movie {
    public long id;
    public String title;
    public String overview;
    public String posterPath;
    public Date releaseDate;
    public long runtime;
    public double voteAverage;

    public Movie(long id, String title, String overview, String posterPath, Date releaseDate, long runtime, double voteAverage) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.voteAverage = voteAverage;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", releaseDate=" + releaseDate +
                ", runtime=" + runtime +
                ", voteAverage=" + voteAverage +
                '}';
    }
}
