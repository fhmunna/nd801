package com.example.ianribas.mypopularmovies.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 */
public class MoviePosterImageView extends ImageView {
    public MoviePosterImageView(Context context) {
        super(context);
    }

    public MoviePosterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoviePosterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Maintain 1:1.5 aspect ratio of posters.
        setMeasuredDimension(getMeasuredWidth(), (3 * getMeasuredWidth()) / 2);
    }
}
