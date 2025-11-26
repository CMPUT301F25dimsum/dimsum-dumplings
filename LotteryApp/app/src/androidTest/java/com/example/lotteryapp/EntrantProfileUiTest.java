package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.example.lotteryapp.entrant.EntrantActivity;
import com.example.lotteryapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple UI test for Entrant profile screen, without using FragmentScenario.
 *
 * This test:
 *  - Launches EntrantActivity.
 *  - Navigates to the profile tab.
 *  - Verifies that the three profile cards are displayed.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class EntrantProfileUiTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Test
    public void profileCards_areDisplayed() {
        onView(withId(R.id.entrantProfileFragment)).perform(click());
        onView(withId(R.id.card_account_info)).check(matches(isDisplayed()));
        onView(withId(R.id.card_update_account)).check(matches(isDisplayed()));
        onView(withId(R.id.card_notification)).check(matches(isDisplayed()));
    }
}
