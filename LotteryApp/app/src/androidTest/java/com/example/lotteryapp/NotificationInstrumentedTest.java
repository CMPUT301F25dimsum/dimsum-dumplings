package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.lotteryapp.admin.AdminActivity;
import com.example.lotteryapp.admin.AdminNoticeFragment;
import com.example.lotteryapp.organizer.OrganizerActivity;
import com.example.lotteryapp.organizer.OrganizerManageEventFragment;
import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Test;

public class NotificationInstrumentedTest {
    /**
     * Delete 'Burnice' 'Doe' 'John' notifications before you do this test
     */

    @Test
    public void sendSuccessMessageTest() {
        Notification successNotification = Notification.constructSuccessNotification(
                "Success Title", "Mark", Notification.SenderRole.ORGANIZER, "MarkEvent"
        );
        successNotification.maskCorrespondence("MarkOrganizerName");

        String successNotificationID = successNotification.sendNotification("Doe");

        FirebaseFirestore.getInstance()
                .collection("notifications").document("Doe")
                .collection("userspecificnotifications").document(successNotificationID)
                .get().addOnSuccessListener(snapshot -> {
                    Notification actualNotification = snapshot.toObject(Notification.class);
                    Assert.assertNotNull(actualNotification);
                    assertEquals("You've Been Invited!", actualNotification.summary);
                    assertEquals("Success Title", actualNotification.title);
                    assertEquals("Mark", actualNotification.sender);
                    assertEquals("Doe", actualNotification.receiver);
                    assertEquals("MarkEvent", actualNotification.event);
                    assertEquals(Notification.Type.SUCCESS, actualNotification.type);
                    assertEquals(Notification.SenderRole.ORGANIZER, actualNotification.senderRole);
                    assertEquals("MarkOrganizerName", actualNotification.correspondenceMask);
                });
    }

    @Test
    public void sendFailureMessageTest() {
        Notification failureNotification = Notification.constructFailureNotification(
                "Failure Title", "Jane", Notification.SenderRole.ORGANIZER, "JaneEvent"
        );
        failureNotification.maskCorrespondence("JaneOrganizerName");

        String failureNotificationID = failureNotification.sendNotification("Burnice");

        FirebaseFirestore.getInstance()
                .collection("notifications").document("Burnice")
                .collection("userspecificnotifications").document(failureNotificationID)
                .get().addOnSuccessListener(snapshot -> {
                    Notification actualNotification = snapshot.toObject(Notification.class);
                    Assert.assertNotNull(actualNotification);
                    assertEquals("You've Been Waitlisted.", actualNotification.summary);
                    assertEquals("Failure Title", actualNotification.title);
                    assertEquals("Jane", actualNotification.sender);
                    assertEquals("Burnice", actualNotification.receiver);
                    assertEquals("JaneEvent", actualNotification.event);
                    assertEquals(Notification.Type.FAILURE, actualNotification.type);
                    assertEquals(Notification.SenderRole.ORGANIZER, actualNotification.senderRole);
                    assertEquals("JaneOrganizerName", actualNotification.correspondenceMask);
                });
    }

    @Test
    public void sendCustomMessageTest() {
        Notification customNotification = Notification.constructCustomNotification(
                "Custom Message", "Custom Title", "Bob", Notification.SenderRole.ADMIN
        );

        String customNotificationID = customNotification.sendNotification("John");

        FirebaseFirestore.getInstance()
                .collection("notifications").document("John")
                .collection("userspecificnotifications").document(customNotificationID)
                .get().addOnSuccessListener(snapshot -> {
                    Notification actualNotification = snapshot.toObject(Notification.class);
                    Assert.assertNotNull(actualNotification);
                    assertEquals("Custom Message", actualNotification.summary);
                    assertEquals("Custom Title", actualNotification.title);
                    assertEquals("Bob", actualNotification.sender);
                    assertEquals("John", actualNotification.receiver);
                    assertNull(actualNotification.event);
                    assertEquals(Notification.Type.CUSTOM, actualNotification.type);
                    assertEquals(Notification.SenderRole.ADMIN, actualNotification.senderRole);
                });
    }

    /**
     * Tests to see if notifications in the database appear on the app.
     * Requires exactly 1 initiation notice in the database :/ otherwise ambiguous views
     * @throws InterruptedException if firebase times out
     */
    @Test
    public void seeNotificationTest() throws InterruptedException {
        ActivityScenario<AdminActivity> activityScenario =
                ActivityScenario.launch(AdminActivity.class);

        activityScenario.onActivity(activity -> {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new AdminNoticeFragment())
                    .commitNow();
        });

        Thread.sleep(5000);

        onView(withId(R.id.reuse_notification_button)).check(matches(isDisplayed()));
        onView(withId(R.id.reuse_notification_button)).perform(click());
        //Button actions open new fragment, not possible in this test
    }
}
