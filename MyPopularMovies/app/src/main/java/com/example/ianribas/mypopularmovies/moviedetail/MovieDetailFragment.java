package com.example.ianribas.mypopularmovies.moviedetail;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ianribas.mypopularmovies.R;
import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesRepository;
import com.example.ianribas.mypopularmovies.databinding.MovieDetailBinding;
import com.example.ianribas.mypopularmovies.movielist.MovieListActivity;
import com.example.ianribas.mypopularmovies.util.network.NetworkStateListener;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements MovieDetailContract.View {
    private final String TAG = MovieDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the movie ID that this fragment
     * represents.
     */
    public static final String ARG_MOVIE_ID = "item_id";

    private MovieDetailBinding mBinding;

    private MovieDetailContract.Presenter mPresenter;

    public static MovieDetailFragment create(long movieId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_MOVIE_ID, movieId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    private long getMovieId() {
        if (getArguments().containsKey(ARG_MOVIE_ID)) {
            return getArguments().getLong(ARG_MOVIE_ID, 0L);
        }
        return 0L;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_detail, container, false);

        // populateView();
        final MoviesRepository dataSource = MoviesRepository.create((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        mPresenter = new MovieDetailPresenter(dataSource, this, getMovieId());
        setPresenter(mPresenter);

        return mBinding.getRoot();
    }

    @Override
    public void showMovie(Movie movie) {
        if (getActivity() == null) {
            return;
        }

        mBinding.progressBar.setVisibility(View.GONE);

        mBinding.title.setText(movie.title);
        mBinding.year.setText(DateFormat.format("yyyy", movie.releaseDate));
        mBinding.duration.setText(this.getString(R.string.minutes, movie.runtime));
        mBinding.rating.setText(this.getString(R.string.rating, movie.voteAverage));
        mBinding.synopsis.setText(movie.overview);

        mBinding.synopsis.setVisibility(View.VISIBLE);
        mBinding.middle.setVisibility(View.VISIBLE);
        mBinding.title.setVisibility(View.VISIBLE);

        Picasso.with(getContext()).load(mPresenter.posterPath()).into(mBinding.poster);
    }

    @Override
    public void showLoading() {
        if (mBinding != null) {
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mBinding.title.setVisibility(View.INVISIBLE);
            mBinding.middle.setVisibility(View.INVISIBLE);
            mBinding.synopsis.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void showError(Throwable error) {
        Log.e(TAG, "showError: Error loading movie", error);
        ((NetworkStateListener) getActivity()).onNetworkUnavailable();
    }

    @Override
    public void setPresenter(MovieDetailContract.Presenter presenter) {
        mPresenter = presenter;
        mPresenter.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }
}
