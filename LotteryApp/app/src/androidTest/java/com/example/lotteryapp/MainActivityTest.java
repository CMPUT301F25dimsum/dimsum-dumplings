package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.material.textfield.TextInputLayout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewAssertion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Instrumented tests for MainActivity (local only).
 *
 * Test-only Intent extras recognized by MainActivity (should be handled there):
 *  - TEST_BYPASS_SERVER = true → skip Firestore check and show form immediately
 *  - TEST_FORCE_LOCAL   = true → skip Firestore check and auto-route using local SharedPreferences
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // Auto-grant location permissions so system dialog doesn't block tests
    @Rule
    public GrantPermissionRule locationPermissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );

    // ---------------- helpers ----------------
    private void clearPrefs() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sp = ctx.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    private void writeSignedIn(String role) {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences sp = ctx.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean("hasAccount", true)
                .putString("UID", "TEST_UID_123")
                .putString("Role", role)
                .apply();
    }

    /** Assert a TextInputLayout shows a specific error string ("" means no error). */
    private static ViewAssertion hasTextInputLayoutError(String expected) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            if (!(view instanceof TextInputLayout)) {
                throw new AssertionError("View is not a TextInputLayout");
            }
            CharSequence err = ((TextInputLayout) view).getError();
            assertThat(err == null ? "" : err.toString(), is(expected));
        };
    }
    // ------------------------------------------


    /**
     * 4) clearsErrors_whenInputsFixed
     * After errors appear, fixing inputs should clear them.
     */
    @Test
    public void clearsErrors_whenInputsFixed() {
        clearPrefs();

        Intent it = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        it.putExtra("TEST_BYPASS_SERVER", true);

        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(it)) {
            // Trigger errors
            onView(withId(R.id.main_signup_button)).perform(click());

            // Fix all fields
            onView(withId(R.id.etEmail)).perform(replaceText("user@example.com"), closeSoftKeyboard());
            onView(withId(R.id.etName)).perform(replaceText("Alice"), closeSoftKeyboard());
            onView(withId(R.id.etPhone)).perform(replaceText("1234567890"), closeSoftKeyboard());
            onView(withId(R.id.main_role_dropdown)).perform(replaceText("Entrant"), closeSoftKeyboard());

            // Click again
            onView(withId(R.id.main_signup_button)).perform(click());

            // Errors should be cleared (empty)
            onView(withId(R.id.tilEmail)).check(hasTextInputLayoutError(""));
            onView(withId(R.id.tilName)).check(hasTextInputLayoutError(""));
            onView(withId(R.id.tilPhone)).check(hasTextInputLayoutError(""));
            onView(withId(R.id.tilRole)).check(hasTextInputLayoutError(""));
        }
    }


    /**
     * 1) phoneMask_formatsAsUserTypes
     * Types "1234567890111" and expects it to clamp/format to "1234567890".
     */
    @Test
    public void phoneMask_formatsAsUserTypes() {
        clearPrefs();

        Intent it = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        it.putExtra("TEST_BYPASS_SERVER", true);

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
