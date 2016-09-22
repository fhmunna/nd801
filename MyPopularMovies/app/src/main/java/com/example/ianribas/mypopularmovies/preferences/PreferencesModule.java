package com.example.ianribas.mypopularmovies.preferences;

import android.content.Context;

import com.example.ianribas.mypopularmovies.ApplicationModule;

import dagger.Module;
import dagger.Provides;

/**
 * Used by dagger to provide the application preferences.
 */
@Module(includes = ApplicationModule.class)
public class PreferencesModule {
    @Provides
    static AppPreferences getAppPreferences(Context context) {
        return new AppPreferences(context);
    }
}
