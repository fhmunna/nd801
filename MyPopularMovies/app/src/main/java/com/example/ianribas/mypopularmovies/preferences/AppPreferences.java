package com.example.ianribas.mypopularmovies.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Gives access to the application preferences.
 * For now, just the movie sort order: most popular or top rated.
 */
public class AppPreferences {

    public static final int MOST_POPULAR = 0;
    public static final int TOP_RATED = 1;

    private static final String SORT_ORDER_KEY = "sort_order_key";
    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getSortOrder() {
        return prefs.getInt(SORT_ORDER_KEY, MOST_POPULAR);
    }

    public void setSortOrder(int sortOrder) {
        prefs.edit()
                .putInt(SORT_ORDER_KEY, sortOrder == MOST_POPULAR ? MOST_POPULAR : TOP_RATED)
                .apply();
    }


}
