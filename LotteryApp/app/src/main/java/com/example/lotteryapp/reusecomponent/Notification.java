package com.example.lotteryapp.reusecomponent;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.List;

public class Notification {
    public String summary;
    public String title;
    public Timestamp time;
    public String sender;
    public String receiver;
    public String event;

    public enum Type {
        SUCCESS, FAILURE, CUSTOM;
    }

    public Type type; //apparently firebase can do this just fine

    public enum SenderRole {
        ORGANIZER, ADMIN
    }

    public SenderRole senderRole;

    public Notification() {
    }

    public static Notification constructCustomNotification(
            String summary,
            String title,
            String senderID,
            SenderRole senderRole
    ) {
        Notification ret = new Notification();
        ret.summary = summary;
        ret.title = title;
        ret.time = Timestamp.now();
        ret.sender = senderID;
        ret.receiver = null; //Set in send message
        ret.event = null;
        ret.type = Type.CUSTOM;
        ret.senderRole = senderRole;
        return ret;
    }

    public static Notification constructSuccessNotification(
            String title,
            String senderID,
            SenderRole senderRole,
            String eventID
    ) {
        Notification ret = Notification.constructCustomNotification(
                "You've Been Invited!", title, senderID, senderRole);
        ret.event = eventID;
        ret.type = Type.SUCCESS;
        return ret;
    }

    public static Notification constructFailureNotification(
            String title,
            String senderID,
            SenderRole senderRole,
            String eventID
    ) {
        Notification ret = Notification.constructCustomNotification(
                "You've Been Waitlisted.", title, senderID, senderRole);
        ret.event = eventID;
        ret.type = Type.FAILURE;
        return ret;
    }

    public String sendNotification(String receiverID) {
        this.receiver = receiverID;

        DocumentReference entry = FirebaseFirestore.getInstance()
                .collection("notifications").document(receiverID)
                .collection("userspecificnotifications").document();
        entry.set(this);

//        //KEEP FOR INSTRUMENTED TESTING ONLY
//        try {
//                Tasks.await(entry.set(this));
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//        }

        return entry.getId();
    }

    public void broadcastNotification(List<String> receivers) {
        for (String receiverID : receivers) this.sendNotification(receiverID);
    }
}
