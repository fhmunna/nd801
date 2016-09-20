package com.example.ianribas.mypopularmovies.movielist;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.ianribas.mypopularmovies.AbstractNetworkAwareActivity;
import com.example.ianribas.mypopularmovies.R;
import com.example.ianribas.mypopularmovies.data.source.MoviesRepository;
import com.example.ianribas.mypopularmovies.moviedetail.MovieDetailActivity;
import com.example.ianribas.mypopularmovies.moviedetail.MovieDetailFragment;
import com.example.ianribas.mypopularmovies.preferences.AppPreferences;
import com.example.ianribas.mypopularmovies.util.test.EspressoIdlingResource;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AbstractNetworkAwareActivity {

    private MovieListFragment mMovieListFragment;
    private View mProgressBar;
    private View mDetailContainer;

    private MovieListContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mOfflineView = findViewById(R.id.layout_offline);
        mProgressBar = findViewById(R.id.progress_bar);
        mMovieListFragment = ((MovieListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie_list));

        mDetailContainer = findViewById(R.id.movie_detail_container);

        mPresenter = new MovieListPresenter(MoviesRepository.create(mConnectivityManagerDelegate), mMovieListFragment,
                new AppPreferences(this), mDetailContainer != null);

        mMovieListFragment.setPresenter(mPresenter);

        Spinner spinner = (Spinner) findViewById(R.id.sort_order);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_orders_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(mPresenter.getSortOrder() == AppPreferences.MOST_POPULAR ? 0 : 1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.setSortOrder(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Default is first option (top rated)
                adapterView.setSelection(0);
            }
        });
    }

    private void setListVisibility(int visibility) {
        final View view = mMovieListFragment.getView();
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    @Override
    public void onNetworkUnavailable() {
        super.onNetworkUnavailable();

        mProgressBar.setVisibility(View.GONE);
        setListVisibility(View.GONE);
        if (mDetailContainer != null) {
            mDetailContainer.setVisibility(View.GONE);
        }
        mOfflineView.setVisibility(View.VISIBLE);
        findViewById(R.id.button_try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNetworkAvailable();
            }
        });

        if (mPresenter.isTwoPane()) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            MovieDetailFragment fragment = (MovieDetailFragment) fragmentManager
                    .findFragmentByTag(MovieListFragment.MOVIE_DETAIL_FRAGMENT_TAG);

            // Remove detail fragment, if present.
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
    }

    @Override
    public void onNetworkAvailable() {
        super.onNetworkAvailable();

        mPresenter.start();
    }

    public void showLoading() {
        mOfflineView.setVisibility(View.GONE);
        setListVisibility(View.GONE);
        if (mDetailContainer != null) {
            mDetailContainer.setVisibility(View.GONE);
        }
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void showData() {
        mProgressBar.setVisibility(View.GONE);
        setListVisibility(View.VISIBLE);
        if (mDetailContainer != null) {
            mDetailContainer.setVisibility(View.VISIBLE);
        }
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
