package com.example.ianribas.mypopularmovies;

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.example.ianribas.mypopularmovies.movielist.MovieListActivity;
import com.squareup.picasso.PicassoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    @Rule
    public final IntentsTestRule<MovieListActivity> mRule = new IntentsTestRule<>(MovieListActivity.class);
    private PicassoIdlingResource mPicassoIdlingResource;

    // Upon launch, present the user with an grid arrangement of movie posters.
    @Test
    public void presentGridArrangementOfMoviePosters() {
        onView(withId(R.id.fragment_movie_list))
                .check(matches(allOf(isDisplayed(), hasDescendant(isAssignableFrom(ImageView.class)))));
    }

    //    Allow your user to change sort order via a setting: The sort order can be by most popular, or by top rated
    @Test
    public void allowUserToChangeSortOrder() {
        onView(withId(R.id.sort_order)).perform(click());

        String topRated = InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.sort_orders_array)[1];

        onData(allOf(is(instanceOf(String.class)), is(topRated))).perform(click());

        onView(withId(R.id.fragment_movie_list))
                .check(matches(hasDescendant(isAssignableFrom(ImageView.class))));

        String mostPopular = InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.sort_orders_array)[0];
        onView(withId(R.id.sort_order)).perform(click());

        onData(allOf(is(instanceOf(String.class)), is(mostPopular))).perform(click());

        onView(withId(R.id.fragment_movie_list))
                .check(matches(hasDescendant(isAssignableFrom(ImageView.class))));

    }

    //    Allow the user to tap on a movie poster and transition to a details screen with additional information such as:
//    original title
//    movie poster image thumbnail
//    A plot synopsis (called overview in the api)
//    user rating (called vote_average in the api)
//    release date
    @Test
    public void showDetailsScreen() {
        mPicassoIdlingResource = new PicassoIdlingResource(mRule.getActivity());
        Espresso.registerIdlingResources(mPicassoIdlingResource);
        SystemClock.sleep(200);

        onView(withId(R.id.fragment_movie_list))
                .check(matches(allOf(isDisplayed(), hasDescendant(isAssignableFrom(ImageView.class)))));

        onView(withId(R.id.fragment_movie_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));

        onView(withId(R.id.title))
                .check(matches(withText(not(isEmptyOrNullString()))));
        onView(withId(R.id.poster))
                .check(matches(isAssignableFrom(ImageView.class)));
        onView(withId(R.id.synopsis))
                .check(matches(withText(not(isEmptyOrNullString()))));
        onView(withId(R.id.rating))
                .check(matches(withText(not(isEmptyOrNullString()))));
        onView(withId(R.id.year))
                .check(matches(withText(not(isEmptyOrNullString()))));
        // Additional, following layout.
        onView(withId(R.id.year))
                .check(matches(withText(not(isEmptyOrNullString()))));
    }

    @Before
    public void setup() {
        Espresso.registerIdlingResources(mRule.getActivity().getCountingIdlingResource());
    }

    @After
    public void teardown() {
        Espresso.unregisterIdlingResources(mRule.getActivity().getCountingIdlingResource());
        if (mPicassoIdlingResource != null) {
            Espresso.unregisterIdlingResources(mPicassoIdlingResource);
        }
    }
}