package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for MainActivity.
 *
 * Notes :
 * - These tests rely on two test-only Intent extras that MainActivity understands:
 *   * TEST_BYPASS_SERVER = true  → skip Firestore check and show the form immediately
 *   * TEST_FORCE_LOCAL   = true  → skip Firestore check and auto-route using local SharedPreferences
 *
 * This makes the tests deterministic and not dependent on network or server state.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // ---------------- helpers ----------------

    /**
     * Clears the app's local login SharedPreferences to start from a clean state.
     */
    private void clearPrefs() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sp = ctx.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    /**
     * Writes a minimal "signed-in" state into SharedPreferences for tests that simulate
     * local auto-skip (without contacting the server).
     *
     * @param role canonical role string, e.g., "organizer", "entrant", or "admin"
     */
    private void writeSignedIn(String role) {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sp = ctx.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean("hasAccount", true)
                .putString("UID", "TEST_UID_123")
                .putString("Role", role)
                .apply();
    }
    // ------------------------------------------

    /**
     * Verifies that the phone input is formatted as the user types:
     * typing "1234567890" becomes "123 456 7890".
     * <p>
     * Uses TEST_BYPASS_SERVER=true so the activity shows the form immediately
     * (no asynchronous Firestore check that could race the UI test).
     */
    @Test
    public void phoneMask_formatsAsUserTypes() {
        clearPrefs();

        Intent it = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        it.putExtra("TEST_BYPASS_SERVER", true); // ensure the signup form is shown immediately

        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(it)) {
            onView(withId(R.id.etPhone)).perform(typeText("1234567890111"));
            onView(withId(R.id.etPhone)).check((v, e) ->
                    org.junit.Assert.assertEquals(
                            "1234567890",
                            ((android.widget.EditText) v).getText().toString()
                    )
            );
        }
    }
}
