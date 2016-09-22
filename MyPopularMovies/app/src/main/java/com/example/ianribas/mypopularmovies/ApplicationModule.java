package com.example.ianribas.mypopularmovies;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.example.ianribas.mypopularmovies.util.network.ConnectivityManagerDelegate;

import dagger.Module;
import dagger.Provides;

/**
 * Used by dagger to provide the application context.
 */
@Module
public class ApplicationModule {
    private final Application application;

    public ApplicationModule(@NonNull Application application) {
        this.application = application;
    }

    @Provides
    public Context provideContext() {
        return application;
    }

    @Provides
    public ConnectivityManagerDelegate provideConnectivityManagerDelegate() {
        return new ConnectivityManagerDelegate((ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE));
    }
}
