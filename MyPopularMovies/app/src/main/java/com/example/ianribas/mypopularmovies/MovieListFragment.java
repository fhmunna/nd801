package com.example.ianribas.mypopularmovies;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ianribas.mypopularmovies.data.Movie;
import com.example.ianribas.mypopularmovies.movielist.MovieListContract;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 */
public class MovieListFragment extends Fragment implements MovieListContract.View {
    public static final String TAG = MovieListFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private int mSelectedPaddingOffset;
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

        mSelectedPaddingOffset = (int) (10.0 * metrics.density);

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
    }

    @Override
    public void showLoading() {
        ((NetworkAwareActivity) getActivity()).showLoading();

    }

    @Override
    public void showError(Throwable error) {
        Log.e(TAG, "showError: Error loading data", error);
        ((NetworkAwareActivity) getActivity()).onNetworkUnavailable();
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
        Bundle arguments = new Bundle();
        arguments.putLong(MovieDetailFragment.ARG_MOVIE_ID, movieId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment)
                .commit();
    }

    private void showDetailsOnActivity(long movieId) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, movieId);

        this.startActivity(intent);
    }

    @Override
    public void setPresenter(MovieListContract.Presenter presenter) {
        mPresenter = presenter;
        presenter.start();
        Log.d(TAG, "setPresenter: started");
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Movie> mValues;
        private final int imagePadding;
        private ViewHolder lastSelected = null;

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
            Picasso.with(getContext()).load(mPresenter.posterPath(holder.mItem)).into(holder.mImage);

            int selectedImagePadding = imagePadding + mSelectedPaddingOffset;
            if (holder.mItem.id == mPresenter.getSelectedMovieId()) {
                lastSelected = holder;
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
                    notifyItemChangedIfFound(lastSelected);
                    notifyItemChangedIfFound(holder);
                    lastSelected = holder;
                    mPresenter.openMovieDetails(holder.mItem.id);
                }
            });
        }

        private void notifyItemChangedIfFound(final ViewHolder holder) {
            if (holder != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }
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
