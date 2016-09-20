package com.example.ianribas.mypopularmovies.movielist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ianribas.mypopularmovies.R;
import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.moviedetail.MovieDetailActivity;
import com.example.ianribas.mypopularmovies.moviedetail.MovieDetailFragment;
import com.example.ianribas.mypopularmovies.view.MoviePosterImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 */
public class MovieListFragment extends Fragment implements MovieListContract.View {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    public static final String MOVIE_DETAIL_FRAGMENT_TAG = "MOVIE_DETAIL_FRAGMENT_TAG";

    private RecyclerView mRecyclerView;
    private MovieListContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_list_fragment, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.movie_list);
        assert mRecyclerView != null;
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float widthDp = metrics.widthPixels / metrics.density;
        Log.i(TAG, "onCreate: dimensions: w=" + metrics.widthPixels + " d=" + metrics.density
                + " xdpi=" + metrics.xdpi + " ydpi=" + metrics.ydpi + " W(dp)=" + widthDp);

        if (widthDp >= 450.0) {
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(3);
        } else {
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).setSpanCount(2);
        }

        mPresenter.restoreState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.saveState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }

    @Override
    public void showMovies(List<Movie> movies) {
        Log.d(TAG, "showMovies: received movies " + movies.size());
        mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(movies));
        ((MovieListActivity) getActivity()).showData();

        final int selectedPosition = mPresenter.getSelectedPosition();
        if (selectedPosition != RecyclerView.NO_POSITION) {
//            mRecyclerView.smoothScrollToPosition(mSelectedPosition);
            ((GridLayoutManager) mRecyclerView.getLayoutManager())
                    .scrollToPositionWithOffset(selectedPosition, 0);
            Log.d(TAG, "showMovies: scrolled to position " + selectedPosition);
        }
    }

    @Override
    public void showLoading() {
        ((MovieListActivity) getActivity()).showLoading();
    }

    @Override
    public void showError(Throwable error) {
        Log.e(TAG, "showError: Error loading data", error);
        ((MovieListActivity) getActivity()).onNetworkUnavailable();
    }

    @Override
    public void showMovieDetailsUI(long movieId) {
        if (mPresenter.isTwoPane()) {
            showDetailsOnFragment(movieId);
        } else {
            showDetailsOnActivity(movieId);
        }
    }

    private void showDetailsOnFragment(long movieId) {
        final FragmentManager fragmentManager = getFragmentManager();
        MovieDetailFragment fragment = (MovieDetailFragment) fragmentManager
                .findFragmentByTag(MOVIE_DETAIL_FRAGMENT_TAG);

        // Don' recreate fragment if it is to show the same movie.
        if (fragment == null || fragment.getMovieId() != movieId) {
            fragment = MovieDetailFragment.create(movieId);

            fragmentManager.beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Log.i(TAG, "showDetailsOnFragment: Same movie, not showing again. id: " + movieId);
        }
    }

    private void showDetailsOnActivity(long movieId) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, movieId);

        startActivity(intent);
    }

    @Override
    public void setPresenter(MovieListContract.Presenter presenter) {
        mPresenter = presenter;
        presenter.start();
    }

    private class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;

        SimpleItemRecyclerViewAdapter(List<Movie> items) {
            mValues = items;
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
            Picasso.with(getContext()).load(mPresenter.posterPath(holder.mItem)).placeholder(R.drawable.comingsoon).into(holder.mImage);

            holder.mImage.setForeground(ContextCompat.getDrawable(getActivity(), R.drawable.touch_selector));

            if (holder.mItem.id == mPresenter.getSelectedMovieId()) {
                mPresenter.setSelectedPosition(holder.getAdapterPosition());
                holder.mImage.setActivated(true);
            } else {
                holder.mImage.setActivated(false);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.openMovieDetails(holder.mItem.id);
                    int selectedPosition = mPresenter.getSelectedPosition();
                    if (selectedPosition != RecyclerView.NO_POSITION) {

                        notifyItemChanged(selectedPosition);
                    }
                    Log.d(TAG, "onClick: " + holder + " last: " + selectedPosition + " pos: " + holder.getAdapterPosition());
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final MoviePosterImageView mImage;
            Movie mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                mImage = (MoviePosterImageView) view.findViewById(R.id.image);
            }
        }
    }
}
