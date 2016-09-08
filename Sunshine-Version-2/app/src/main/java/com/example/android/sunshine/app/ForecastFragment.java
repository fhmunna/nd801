package com.example.android.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Fragment containing the weekly forecast.
 */
public class ForecastFragment extends Fragment {
    public static final int FORECAST_LOADER_ID = 0;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private static final String SELECTED_FORECAST_POSITION = "selected_forecast_position";

    private ForecastAdapter mForecastAdapter;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks;
    private ListView mListView;
    private int mSelectedPosition = ListView.INVALID_POSITION;
    private boolean mUseSpecialTodayLayout = true;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("For now", "menu clicked " + item.getTitle());
        if (item.getItemId() == R.id.action_refresh) {

            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        new FetchWeatherTask(getActivity()).execute(Utility.getPreferredLocation(getActivity()));
    }

    public void setUseSpecialTodayLayout(boolean useSpecialTodayLayout) {
        mUseSpecialTodayLayout = useSpecialTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseSpectialTodayLayout(mUseSpecialTodayLayout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("loader", "onCreateView: ");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_FORECAST_POSITION)){
            mSelectedPosition = savedInstanceState.getInt(SELECTED_FORECAST_POSITION);
        }

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseSpectialTodayLayout(mUseSpecialTodayLayout);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null && getActivity() instanceof Callback) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    final Uri itemUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE));

                    ((Callback) getActivity()).onItemSelected(itemUri);
                }
                mSelectedPosition = position;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("loader", "onActivityCreated: ");

        mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                // Get data from the database.
                String locationSetting = Utility.getPreferredLocation(getActivity());

                // Sort order:  Ascending, by date.
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                        locationSetting, System.currentTimeMillis());

                Log.d("loader", "onCreateLoader: ");
                return new CursorLoader(getActivity(), weatherForLocationUri,
                        FORECAST_COLUMNS, null, null, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Log.d("loader", "onLoadFinished: " + data.getCount());
                mForecastAdapter.swapCursor(data);

                if (mSelectedPosition != ListView.INVALID_POSITION) {
                    Log.d("loader", "onLoadFinished: scrolled to " + mSelectedPosition);
                    mListView.smoothScrollToPosition(mSelectedPosition);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                Log.d("loader", "onLoaderReset: ");
                mForecastAdapter.swapCursor(null);
            }
        };

        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, mLoaderCallbacks);

        super.onActivityCreated(savedInstanceState);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, mLoaderCallbacks);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_FORECAST_POSITION, mSelectedPosition);
        }
        Log.d("loader", "onSaveInstanceState: saved " + mSelectedPosition);
        super.onSaveInstanceState(outState);
    }
}
