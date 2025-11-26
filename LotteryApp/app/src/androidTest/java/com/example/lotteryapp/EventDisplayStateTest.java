package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotteryapp.entrant.EntrantActivity;
import com.example.lotteryapp.organizer.OrganizerActivity;
import com.example.lotteryapp.organizer.OrganizerManageEventFragment;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventDisplayFragment;
import com.example.lotteryapp.reusecomponent.LotteryEntrant;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDisplayStateTest {

    @Test
    public void registerTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();
        e.getLottery().entrantStatus.clear();
        e.getLottery().getEntrants().clear();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Register")).check(matches(isDisplayed()));
    }

    @Test
    public void registeredTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Awaiting Results...")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    @Test
    public void waitlistedTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Waitlisted")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    @Test
    public void invitedTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Accept")).check(matches(isDisplayed()));
        onView(withText("Decline")).check(matches(isDisplayed()));
    }

    @Test
    public void acceptTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Accepted")).check(matches(isDisplayed()));
    }

    @Test
    public void declineTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Declined")).check(matches(isDisplayed()));
    }

    @Test
    public void cancelTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Event> event = new AtomicReference<>(new Event("b52331ac7535030e"));

        FirebaseFirestore.getInstance()
                .collection("events").document("b52331ac7535030e")
                .collection("organizer_events").document("Iao3JhrwKvV2Z9NyVKFh")
                .get().addOnSuccessListener(snapshot -> {
                    event.set(snapshot.toObject(Event.class));
                    latch.countDown();
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase timed out", completed);

        Event e = event.get();

        ActivityScenario<EntrantActivity> activityScenario =
                ActivityScenario.launch(EntrantActivity.class);

        activityScenario.onActivity(activity -> {
            EventDisplayFragment fragment = new EventDisplayFragment(e);
            fragment.show(activity.getSupportFragmentManager(), "eventDisplay");
        });

        Thread.sleep(3000);

        onView(withText("Cancelled")).check(matches(isDisplayed()));
    }
}
