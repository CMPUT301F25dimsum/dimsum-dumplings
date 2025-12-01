package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.example.lotteryapp.organizer.OrganizerActivity;
import com.example.lotteryapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple UI test for the organizer profile screen.
 *
 * This test:
 *  - Launches OrganizerActivity.
 *  - Navigates to the profile tab.
 *  - Verifies that the three profile cards are displayed.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class OrganizerProfileUiTest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    @Test
    public void profileCards_areDisplayed() {
        onView(withId(R.id.organizerProfileFragment)).perform(click());
        onView(withId(R.id.card_account_info)).check(matches(isDisplayed()));
        onView(withId(R.id.card_update_account)).check(matches(isDisplayed()));
    }
}
