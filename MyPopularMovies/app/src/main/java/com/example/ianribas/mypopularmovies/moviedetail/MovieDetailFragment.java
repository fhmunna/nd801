package com.example.ianribas.mypopularmovies.moviedetail;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.ianribas.mypopularmovies.R;
import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.data.source.MoviesRepository;
import com.example.ianribas.mypopularmovies.databinding.MovieDetailBinding;
import com.example.ianribas.mypopularmovies.movielist.MovieListActivity;
import com.example.ianribas.mypopularmovies.util.network.NetworkStateListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
    private Target mBgTarget;

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

    public long getMovieId() {
        if (getArguments().containsKey(ARG_MOVIE_ID)) {
            return getArguments().getLong(ARG_MOVIE_ID, 0L);
        }
        return 0L;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_detail, container, false);

        final MoviesRepository dataSource = MoviesRepository.create((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        mPresenter = new MovieDetailPresenter(dataSource, this, getMovieId());

        return mBinding.getRoot();
    }

    @Override
    public void showMovie(Movie movie) {
        Log.d(TAG, "showMovie: " + movie + " f:" + this);
        if (getActivity() == null) {
            Log.i(TAG, "showMovie: no activity!!!");
            return;
        }

        mBinding.progressBar.setVisibility(View.GONE);

        mBinding.title.setText(movie.title);
        mBinding.year.setText(DateFormat.format("yyyy", movie.releaseDate));
        mBinding.duration.setText(this.getString(R.string.minutes, movie.runtime));
        mBinding.rating.setText(this.getString(R.string.rating, movie.voteAverage));
        mBinding.synopsis.setText(movie.overview);

        mBinding.detailContent.setVisibility(View.VISIBLE);
        mBinding.title.setVisibility(View.VISIBLE);

        Picasso.with(getContext()).load(mPresenter.posterPath()).into(mBinding.poster);

        mBgTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    final BitmapDrawable background = new BitmapDrawable(getContext().getResources(), bitmap);
                    background.setAlpha(50);
                    mBinding.detailContent.setBackground(background);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.e(TAG, "onBitmapFailed: error loading backdrop for movie detail background");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // NOOP
            }
        };

        ViewTreeObserver vto = mBinding.getRoot().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mBinding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mBinding.getRoot().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                Log.d(TAG, "showMovie: detailContent y: " + mBinding.detailContent.getY() + " h:" + mBinding.detailContent.getHeight());
                Log.d(TAG, "showMovie: binding root y: " + mBinding.getRoot().getY() + " h:" + mBinding.getRoot().getHeight());
                Log.d(TAG, "showMovie: parent y: " + ((View) mBinding.getRoot().getParent()).getY() + " h:" + ((View) mBinding.getRoot().getParent()).getHeight());

                int delta  = ((View) mBinding.getRoot().getParent()).getHeight() - mBinding.getRoot().getHeight();
                int height = mBinding.detailContent.getHeight();

                if (delta > 0) {
                    height = mBinding.detailContent.getMeasuredHeight() + delta;
                    mBinding.detailContent.setMinimumHeight(height);
                }

                Log.d(TAG, "onGlobalLayout: new height = " + height + " delta=" + delta);

                Picasso.with(getContext())
                        .load(mPresenter.backdropPath())
                        .resize(mBinding.detailContent.getWidth(), height)
                        .centerCrop()
                        .into(mBgTarget);
            }
        });

    }

    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading: id: " + getMovieId());
        if (mBinding != null) {
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mBinding.title.setVisibility(View.INVISIBLE);
            mBinding.detailContent.setVisibility(View.INVISIBLE);
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
            mBgTarget = null;
            mPresenter.unsubscribe();
        }
    }
}
