package com.example.ianribas.mypopularmovies;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ianribas.mypopularmovies.databinding.MovieDetailBinding;
import com.example.ianribas.mypopularmovies.model.MoviesDBDelegate;
import com.example.ianribas.mypopularmovies.model.NetworkStateListener;
import com.example.ianribas.mypopularmovies.model.dto.Movie;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    private final String TAG = MovieDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the movie ID that this fragment
     * represents.
     */
    public static final String ARG_MOVIE_ID = "item_id";

    /**
     * The movie this fragment is presenting.
     */
    private Movie mMovie;
    private MovieDetailBinding mBinding;

    private MoviesDBDelegate moviesDBDelegate;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        moviesDBDelegate = MoviesDBDelegate.create(
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE));
        if (getArguments().containsKey(ARG_MOVIE_ID)) {

            // Load the movie details specified by the id in the fragment arguments.
            final long id = getArguments().getLong(ARG_MOVIE_ID);
            if (id > 0 && (mMovie == null)) {
                new FetchMovieTask().execute(id);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_detail, container, false);

        populateView();

        return mBinding.getRoot();
    }

    private void populateView() {
        if (getActivity() == null) {
            return;
        }

        if (mMovie == null && mBinding != null) {
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mBinding.title.setVisibility(View.INVISIBLE);
            mBinding.middle.setVisibility(View.INVISIBLE);
            mBinding.synopsis.setVisibility(View.INVISIBLE);
        }

        if (mMovie == null || mBinding == null) {
            return;
        }

        mBinding.progressBar.setVisibility(View.GONE);

        mBinding.title.setText(mMovie.title);
        mBinding.year.setText(DateFormat.format("yyyy", mMovie.releaseDate));
        mBinding.duration.setText(this.getString(R.string.minutes, mMovie.runtime));
        mBinding.rating.setText(this.getString(R.string.rating, mMovie.voteAverage));
        mBinding.synopsis.setText(mMovie.overview);

        mBinding.synopsis.setVisibility(View.VISIBLE);
        mBinding.middle.setVisibility(View.VISIBLE);
        mBinding.title.setVisibility(View.VISIBLE);

        Picasso.with(getContext()).load(moviesDBDelegate.posterPath(mMovie)).into(mBinding.poster);
    }

    private class FetchMovieTask extends AsyncTask<Long, Void, Movie> {


        @Override
        protected Movie doInBackground(Long... ids) {
            Movie movie = null;

            if (ids != null && ids.length > 0) {
                try {
                    movie = moviesDBDelegate.details(ids[0]);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: error retrieveing movies",e);
                }
            }

            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie != null) {
                mMovie = movie;
                populateView();
            } else {
                if (getActivity() instanceof NetworkStateListener) {
                    ((NetworkStateListener) getActivity()).onNetworkUnavailable();
                } else {
                    Toast.makeText(getContext(), R.string.error_loading_movies, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
