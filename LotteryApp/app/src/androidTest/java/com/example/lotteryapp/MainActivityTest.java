package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

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
    // ------------------------------------------

    /**
     * 1) phoneMask_formatsAsUserTypes
     */
    @Test
    public void phoneMask_formatsAsUserTypes() {
        clearPrefs();
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.etPhone)).perform(typeText("1234567890"));
            onView(withId(R.id.etPhone)).check((v, e) ->
                    org.junit.Assert.assertEquals("123 456 7890",
                            ((android.widget.EditText) v).getText().toString()));
        }
    }


    /**
     * 3) tryLocalAutoSkip：
     * Assist with Chatgpt 2025/11/2
     */
    @Test
    public void tryLocalAutoSkip_routesOrShowsForm() {
        clearPrefs();
        writeSignedIn("organizer");
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {

            android.os.SystemClock.sleep(600);
            org.junit.Assert.assertEquals(Lifecycle.State.DESTROYED, sc.getState());
        }

        clearPrefs();
        try (ActivityScenario<MainActivity> sc = ActivityScenario.launch(MainActivity.class)) {
            android.os.SystemClock.sleep(200);
            org.junit.Assert.assertTrue(
                    sc.getState() == Lifecycle.State.RESUMED ||
                            sc.getState() == Lifecycle.State.STARTED
            );
        }
    }
}
