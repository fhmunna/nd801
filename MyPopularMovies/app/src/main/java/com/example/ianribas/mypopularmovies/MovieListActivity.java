package com.example.ianribas.mypopularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;


import com.example.ianribas.mypopularmovies.model.MoviesDBDelegate;
import com.example.ianribas.mypopularmovies.model.dto.Movie;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements IShowOffline {
    private static String TAG = MovieListActivity.class.getSimpleName();

    public static final int MOST_POPULAR = 0;
    public static final int TOP_RATED = 1;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * The last selected movie. It is more intenresting in two pane mode.
     */
    private long mSelectedMovieId = SELECTED_MOVIE_ID_DEFAULT;
    public static final String SELECTED_MOVIE_ID_KEY = "selected_movie_id_key";
    public static final long SELECTED_MOVIE_ID_DEFAULT = -1;

    /**
     * The border size of the selected movie on the grid, calculated taking into account
     * the device pixel density.
     */
    private int mSelectedPaddingOffset = 0;

    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private View mDetailContainer;

    private final MoviesDBDelegate moviesDBDelegate = MoviesDBDelegate.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mRecyclerView = (RecyclerView) findViewById(R.id.movie_list);
        assert mRecyclerView != null;

        if (savedInstanceState != null) {
            mSelectedMovieId = savedInstanceState.getLong(SELECTED_MOVIE_ID_KEY, SELECTED_MOVIE_ID_DEFAULT);
        }

        mProgressBar = findViewById(R.id.progress_bar);

        mDetailContainer = findViewById(R.id.movie_detail_container);
        if (mDetailContainer != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float widthDp = metrics.widthPixels / metrics.density;
        Log.i(TAG, "onCreate: dimensions: w=" + metrics.widthPixels + " d=" + metrics.density
                + " xdpi=" + metrics.xdpi + " ydpi=" + metrics.ydpi + " W(dp)=" + widthDp);

        if (widthDp >= 450.0) {
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(3);
        } else {
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(2);
        }

        mSelectedPaddingOffset = (int) (10.0 * metrics.density);

        Spinner spinner = (Spinner) findViewById(R.id.sort_order);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_orders_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(getSortOrder() == MOST_POPULAR ? 0 : 1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSortOrder(i);
                updateMovies();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default is first option (top rated)
                adapterView.setSelection(0);
            }
        });

        updateMovies();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSelectedMovieId != SELECTED_MOVIE_ID_DEFAULT) {
            outState.putLong(SELECTED_MOVIE_ID_KEY, mSelectedMovieId);
        }

        super.onSaveInstanceState(outState);
    }

    private int getSortOrder() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getInt(getString(R.string.pref_sort_order_key), MOST_POPULAR);
    }

    private void setSortOrder(int sortOrder) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(getString(R.string.pref_sort_order_key),
                        sortOrder != MOST_POPULAR ? TOP_RATED : MOST_POPULAR)
                .commit();
    }

    private void updateMovies() {
        new FetchMoviesTask().execute();
    }

    private void showDetailsOnFragment(long movieId) {
        Bundle arguments = new Bundle();
        arguments.putLong(MovieDetailFragment.ARG_MOVIE_ID, movieId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();
    }

    private void showDetailsOnActivity(long movieId) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, movieId);

        this.startActivity(intent);
    }

    public void showOffline() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        if (mTwoPane) {
            mDetailContainer.setVisibility(View.GONE);
        }
        findViewById(R.id.layout_offline).setVisibility(View.VISIBLE);
        findViewById(R.id.button_try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.layout_offline).setVisibility(View.GONE);
                updateMovies();
            }
        });
    }

    private class FetchMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            mRecyclerView.setVisibility(View.GONE);
            if (mTwoPane) {
                mDetailContainer.setVisibility(View.GONE);
            }
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                if (getSortOrder() == MOST_POPULAR) {
                    return moviesDBDelegate.retrievePopularMovies();
                } else {
                    return moviesDBDelegate.retrieveTopRatedMovies();
                }
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: error retrieving movies", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                if (mTwoPane) {
                    mDetailContainer.setVisibility(View.VISIBLE);
                }

                mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(movies));
                if (mTwoPane && movies.size() > 0) {
                    Log.i(TAG, "onPostExecute: pos = " + (mSelectedMovieId != SELECTED_MOVIE_ID_DEFAULT ? mSelectedMovieId : movies.get(0).id));
                    showDetailsOnFragment(mSelectedMovieId != SELECTED_MOVIE_ID_DEFAULT ? mSelectedMovieId : movies.get(0).id);
                }
            } else {
                showOffline();
            }
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;
        private final int imagePadding;
        private int lastSelectedPosition = RecyclerView.NO_POSITION;

        public SimpleItemRecyclerViewAdapter(List<Movie> items) {
            mValues = items;
            imagePadding = (int) getResources().getDimension(R.dimen.movie_poster_padding);
            Log.i(TAG, "SimpleItemRecyclerViewAdapter: ");
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            Picasso.with(MovieListActivity.this).load(moviesDBDelegate.posterPath(holder.mItem)).into(holder.mImage);

            int selectedImagePadding = imagePadding + mSelectedPaddingOffset;
            if (holder.mItem.id == mSelectedMovieId) {
                lastSelectedPosition = position;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.mImage.setCropToPadding(true);
                    holder.mImage.setPadding(imagePadding, selectedImagePadding, imagePadding, selectedImagePadding);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.mImage.setCropToPadding(false);
                    holder.mImage.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
                }
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedMovieId = holder.mItem.id;
                    decorateSelectedPosition(holder.getAdapterPosition());
                    if (mTwoPane) {
                        showDetailsOnFragment(holder.mItem.id);
                    } else {
                        showDetailsOnActivity(holder.mItem.id);
                    }
                }
            });
        }

        private void decorateSelectedPosition(int position) {
            if (lastSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(lastSelectedPosition);
            }
            lastSelectedPosition = position;
            if (position != RecyclerView.NO_POSITION) {
                notifyItemChanged(position);
            }
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImage;
            public Movie mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImage = (ImageView) view.findViewById(R.id.image);
            }
        }
    }
}
