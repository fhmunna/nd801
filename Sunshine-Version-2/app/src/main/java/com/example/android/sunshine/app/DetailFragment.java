package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {
    private static final String TAG = DetailFragment.class.getSimpleName();

    public static final String DETAIL_URI = "weather_detail_uri";
    private static int FORECAST_DETAIL_LOADER_ID = 0;

    private static final String[] FORECAST_DETAIL_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_DEGREES = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;
    static final int COL_LOCATION_SETTING = 10;

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private TextView mWeekdayText;
    private ShareActionProvider mShareActionProvider;
    private TextView mDateText;
    private TextView mHighTempText;
    private TextView mLowTempText;
    private TextView mForecastText;
    private TextView mHumidityText;
    private TextView mWindText;
    private TextView mPressureText;
    private ImageView mDetailIcon;
    private Uri mUri;
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), mUri,
                    FORECAST_DETAIL_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                boolean isMetric = Utility.isMetric(getActivity());

                final long weatherDate = data.getLong(COL_WEATHER_DATE);
                final String highTemp = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
                final String lowTemp = Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
                final String forecastDesc = data.getString(COL_WEATHER_DESC);

                mWeekdayText.setText(Utility.getDayName(getContext(), weatherDate));
                mDateText.setText(Utility.getFormattedMonthDay(getContext(), weatherDate));
                mHighTempText.setText(highTemp);
                mLowTempText.setText(lowTemp);
                mForecastText.setText(forecastDesc);

                mHumidityText.setText(getString(R.string.format_humidity,
                        data.getFloat(COL_WEATHER_HUMIDITY)));

                mWindText.setText(Utility.getFormattedWind(getContext(),
                        data.getFloat(COL_WEATHER_WIND_SPEED),
                        data.getFloat(COL_WEATHER_DEGREES)));

                mPressureText.setText(getString(R.string.format_pressure,
                        data.getDouble(COL_WEATHER_PRESSURE)));

                int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
                mDetailIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                mDetailIcon.setContentDescription(forecastDesc);

                if (mShareActionProvider != null) {
                    final String forecast = Utility.formatDate(weatherDate) +
                            " - " + forecastDesc +
                            " - " + (highTemp + "/" + lowTemp) +
                            " - " + data.getString(COL_LOCATION_SETTING);

                    mShareActionProvider.setShareIntent(createShareForecastIntent(forecast));
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mWeekdayText = (TextView) rootView.findViewById(R.id.detail_weekday_textview);
        mDateText = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mHighTempText = (TextView) rootView.findViewById(R.id.detail_high_temp_textview);
        mLowTempText = (TextView) rootView.findViewById(R.id.detail_low_temp_textview);
        mForecastText = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHumidityText = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindText = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureText = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        mDetailIcon = (ImageView) rootView.findViewById(R.id.detail_icon);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: args=" + getArguments());
        if (getArguments() != null) {
            mUri = (Uri) getArguments().get(DETAIL_URI);
            Log.d(TAG, "onActivityCreated: uri=" + mUri);
            if (mUri != null) {
                Log.d(TAG, "onActivityCreated: creating loader");
                getLoaderManager().initLoader(FORECAST_DETAIL_LOADER_ID, null, mLoaderCallbacks);
            }
        }


        super.onActivityCreated(savedInstanceState);
    }

    private Intent createShareForecastIntent(String forecast) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mWeekdayText.getText() != null && !"".equals(mWeekdayText.getText())) {
            mShareActionProvider.setShareIntent(createShareForecastIntent(mWeekdayText.getText().toString()));
        }
    }

    public void onLocationChanged(String location) {
        Log.d(TAG, "onLocationChanged: ");
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
            getLoaderManager().restartLoader(FORECAST_DETAIL_LOADER_ID, null, mLoaderCallbacks);
        }

    }
}
