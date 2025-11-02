package com.example.lotteryapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Test;

public class NotificationInstrumentedTest {
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

    @Test
    public void popUpEventTest() {
        Notification successNotification = Notification.constructSuccessNotification(
                "Success Title", "Doe", Notification.SenderRole.ORGANIZER, "cPYfSo2rncMtgWnv0XJ8"
        );
        successNotification.maskCorrespondence("DoeCoolEvents");

        String successNotificationID = successNotification.sendNotification("Burnice");
    }

    //Intent tests for integration
//    @Test
//    public void adminToOrganizerTest() {
//        //Do sign in as admin
//        SharedPreferences current = InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//
//        Notification customNotification = Notification.constructCustomNotification(
//                "Custom Message", "Custom Title", current.getString("UID", "Bob"), Notification.SenderRole.ADMIN
//        );
//
//        String customNotificationID = customNotification.sendNotification("John");
//
//        //Check if John received
//    }
//
//    @Test
//    public void organizerToEntrantSuccessTest() {
//        //Do sign in as organizer
//        SharedPreferences current = InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//
//        Notification successNotification = Notification.constructSuccessNotification(
//                "Success Title", current.getString("UID", "Mark"), Notification.SenderRole.ORGANIZER, "MarkEvent"
//        );
//        successNotification.maskCorrespondence("MarkOrganizerName");
//
//        String successNotificationID = successNotification.sendNotification("Doe");
//
//        //Sign in as Doe, make sure it matches
//        //Sign in as admin, make sure its there
//    }
//
//    @Test
//    public void organizerToEntrantFailureTest() {
//        //Do sign in as organizer
//        SharedPreferences current = InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
//
//        Notification failureNotification = Notification.constructFailureNotification(
//                "Failure Title", current.getString("UID", "Jane"), Notification.SenderRole.ORGANIZER, "JaneEvent"
//        );
//        failureNotification.maskCorrespondence("JaneOrganizerName");
//
//        String failureNotificationID = failureNotification.sendNotification("Burnice");
//
//        //Sign in as Burnice, make sure it matches
//        //Sign in as admin, make sure its there
//    }
}
