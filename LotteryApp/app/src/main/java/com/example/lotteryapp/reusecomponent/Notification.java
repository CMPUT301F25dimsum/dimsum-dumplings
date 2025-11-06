package com.example.lotteryapp.reusecomponent;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.List;

/**
 * This class represents the notification data that populates a user's notification tab.
 * Notifications are displayed differently by entrant/organizer/admin adapters.
 * The class is Firebase-store able.
 *
 * @author wjlee1
 * @version 1.2
 */
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

    public String correspondenceMask;

    /**
     * Public constructor
     * @since 1.0
     */
    public Notification() {
    }

    /**
     * Constructs a generic custom notification. Typically used by admins or organizers
     * for notifications that are not event specific. You may edit any of Notification's
     * public attributes even after using this method. Unless Notification.type is overwritten,
     * this type of message does not have a button that is linked to an event.
     * Example usage: Notification n = Notification.constructCustomNotification(...);
     * @param summary Summary for the notice
     * @param title Title of the notice
     * @param senderID ID of the sender (current userID from SharedPreference)
     * @param senderRole Role of the sender (current role from SharedPreference)
     * @return Notification of constructed object.
     */
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
        ret.correspondenceMask = senderID;
        return ret;
    }

    /**
     * Construct a success Notification (You've Been Invited! message) typically used by
     * organizers to construct success Notification for winning entrants. Summary is set
     * to "You've Been Invited!" By default, this notification has a button which links
     * to the event specified by the eventID.
     * @param title Title of the notification
     * @param senderID ID of the sender (current userID from SharedPreference)
     * @param senderRole Role of the sender (current role from SharedPreference)
     * @param eventID ID of the linked event
     * @return Notification of constructed object.
     */
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

    /**
     * Construct a failure Notification (You've Been Waitlisted. message) typically used by
     * organizers to construct fail Notifications for losing entrants. Summary is set
     * to "You've Been Waitlisted." By default, this notification has a button which links
     * to the event specified by the eventID.
     * @param title Title of the notification
     * @param senderID ID of the sender (current userID from SharedPreference)
     * @param senderRole Role of the sender (current role from SharedPreference)
     * @param eventID ID of the linked event
     * @return Notification of constructed object.
     */
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

    /**
     * This hides the sender information from the recipient with the mask provided.
     * Typically useful for organizers hiding their ID with their name or their organizations'.
     * If not called, the default is the senderID. Does not hide information from admins.
     * Example usage:
     * Notification n = Notification.constructFailure(...)
     * n.maskCorrespondence("Cool Name") -> Entrants will see "Cool Name" as the author of the message
     *
     * @param mask The applied
     */
    public void maskCorrespondence(String mask) { //Does not affect administrators
        this.correspondenceMask = mask;
    }

    /**
     * Commits the message to the database, which will be picked up by the user in real time.
     * Example usage:
     * Notification n = Notification.constructSuccessNotification(...)
     * n.maskCorrespondence("author")
     * n.sendNotification(ID)
     * @param receiverID Receiver ID
     * @return The ID of the notification document in Firebase for that user
     */
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

    /**
     * Commits the message to the database, which will be picked up by all users specified
     * in real time.
     * Example usage:
     * Notification n = Notification.constructSuccessNotification(...)
     * n.maskCorrespondence("author")
     * n.broadcastNotification(List)
     * @param receivers IDs of receivers
     */
    public void broadcastNotification(List<String> receivers) {
        for (String receiverID : receivers) this.sendNotification(receiverID);
    }
}
