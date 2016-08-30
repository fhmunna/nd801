package com.example.ianribas.mypopularmovies.model;

/**
 * Interface for activities that allows other to signal they should show an offline message.
 */
public interface NetworkStateListener {
    void onNetworkUnavailable();
    void onNetworkAvailable();
}
