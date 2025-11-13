package com.example.lotteryapp;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotteryapp.organizer.OrganizerActivity;
import com.example.lotteryapp.organizer.OrganizerManageEventFragment;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LotteryInstrumentedTest {

    /*
    Adapted techniques from ChatGPT to start a fragment in an activity
    by using a scenario. ChatGPT also advised me to use a latch or thread.sleep
    to make firebase accesses synchronous.

    Algorithms are original, some syntax was borrowed from ChatGPT, including allOf().
     */

    @Test
    public void CheckAll() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        ActivityScenario<OrganizerActivity> activityScenario =
                ActivityScenario.launch(OrganizerActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new OrganizerManageEventFragment(event.get()))
                    .commitNow();
        });

        Event e = event.get();

        Thread.sleep(3000);

        onView(withId(R.id.fragment_organizer_manage_lottery_check_all)).perform(click());
        Thread.sleep(500);
        onView(allOf(withId(R.id.fragment_organizer_manage_lottery_check), not(isChecked())))
                .check(doesNotExist());

        onView(withId(R.id.fragment_organizer_manage_lottery_check_all)).perform(click());
        Thread.sleep(500);
        onView(allOf(withId(R.id.fragment_organizer_manage_lottery_check), isChecked()))
                .check(doesNotExist());
    }

    @Test
    public void SendNotificationTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();
        OrganizerManageEventFragment fragment = new OrganizerManageEventFragment(e);

        ActivityScenario<OrganizerActivity> activityScenario =
                ActivityScenario.launch(OrganizerActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        Thread.sleep(3000);

        LotteryEntrant pixxi = fragment.getmValues().get(3);
        pixxi.bSelected = true;

        onView(withId(R.id.fragment_organizer_manage_lottery_notify)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("TestNotification"));
        onView(withText("Notify")).perform(click());

        Thread.sleep(3000);

        CountDownLatch latch2 = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("notifications").document("Pixxi")
                .collection("userspecificnotifications").get()
                .addOnSuccessListener(snapshot -> {
                    boolean flag = false;
                    for (DocumentSnapshot doc : snapshot) {
                        if ("TestNotification".equals(doc.get("summary"))) flag = true;
                    }
                    assertTrue("Did not find message", flag);
                    latch2.countDown();
                });

        completed = latch2.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);
    }

    @Test
    public void ForceCancelTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();
        OrganizerManageEventFragment fragment = new OrganizerManageEventFragment(e);

        ActivityScenario<OrganizerActivity> activityScenario =
                ActivityScenario.launch(OrganizerActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        Thread.sleep(3000);

        fragment.getmValues().forEach(le ->{
            le.status = LotteryEntrant.Status.Registered;
        });
        fragment.getmValues().get(3).bSelected = true; //Pixxi

        onView(withId(R.id.fragment_organizer_manage_lottery_cancel)).perform(click());

        Thread.sleep(3000);

        CountDownLatch latch2 = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    Event ev = snapshot.toObject(Event.class);
                    assertSame(LotteryEntrant.Status.Cancelled, ev.getLottery().entrantStatus.get(3));
                    latch2.countDown();
                });

        completed = latch2.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);
    }

    /**
     * Tests duplication, not-reinviting invited guests, and expected case
     * Cannot prove no duplication, just run the test multiple times.
     * @throws InterruptedException If firebase timed out
     */
    @Test
    public void Draw5Test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();
        e.setMaxCapacity(5);
        OrganizerManageEventFragment fragment = new OrganizerManageEventFragment(e);

        ActivityScenario<OrganizerActivity> activityScenario =
                ActivityScenario.launch(OrganizerActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        Thread.sleep(3000);

        fragment.getmValues().forEach(le ->{
            le.status = LotteryEntrant.Status.Registered;
        });

        onView(withId(R.id.fragment_organizer_manage_lottery_draw)).perform(click());

        Thread.sleep(3000);

        CountDownLatch latch2 = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    Event ev = snapshot.toObject(Event.class);
                    assertEquals(5, ev.getLottery().entrantStatus.stream().filter(stat -> stat == LotteryEntrant.Status.Invited).count());
                    latch2.countDown();
                });

        completed = latch2.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);
    }

    /**
     * Tests drawing above the amount available, tests not drawing cancelled entrants
     * @throws InterruptedException Firebase times out.
     */
    @Test
    public void Draw6Cancelled1Test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();
        e.setMaxCapacity(6);
        OrganizerManageEventFragment fragment = new OrganizerManageEventFragment(e);

        ActivityScenario<OrganizerActivity> activityScenario =
                ActivityScenario.launch(OrganizerActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commitNow();
        });

        Thread.sleep(3000);

        fragment.getmValues().forEach(le ->{
            le.status = LotteryEntrant.Status.Registered;
        });

        fragment.getmValues().get(3).bSelected = true; //Pixxi
        onView(withId(R.id.fragment_organizer_manage_lottery_cancel)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.fragment_organizer_manage_lottery_draw)).perform(click());

        Thread.sleep(3000);

        CountDownLatch latch2 = new CountDownLatch(1);

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("XPikdlyptK5UF0RUyw41")
                .get().addOnSuccessListener(snapshot -> {
                    Event ev = snapshot.toObject(Event.class);
                    assertEquals(5, ev.getLottery().entrantStatus.stream().filter(stat -> stat == LotteryEntrant.Status.Invited).count());
                    latch2.countDown();
                });

        completed = latch2.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);
    }
}
