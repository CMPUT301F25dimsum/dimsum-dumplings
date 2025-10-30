package com.example.lotteryapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Test;

public class NotificationInstrumentedTest {
    @Test
    public void sendMessageTest() {
        Notification customNotification = Notification.constructCustomNotification(
                "Custom Message", "Custom Title", "Bob", Notification.SenderRole.ADMIN
        );

        Notification successNotification = Notification.constructSuccessNotification(
                "Success Title", "Mark", Notification.SenderRole.ORGANIZER, "MarkEvent"
        );

        Notification failureNotification = Notification.constructFailureNotification(
                "Failure Title", "Jane", Notification.SenderRole.ORGANIZER, "JaneEvent"
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
                });

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
                });
    }
}
