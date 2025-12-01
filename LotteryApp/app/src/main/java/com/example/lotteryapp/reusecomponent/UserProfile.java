package com.example.lotteryapp.reusecomponent;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * Represents a model class for user profiles. Firebase storable.
 * Purpose: To store profile details and retrieve profile details from firebase
 *
 * Issues: None so far
 * @author wjlee1
 */
public class UserProfile {
    public Timestamp acc_created;

    /**
     * Enum representing the type of entrant.
     */
    public enum Type {
        admin, organizer, entrant;
    }
    public Type acc_type;
    public String email;
    public String ip;
    public String name;
    public String phone_num;
    public String uid;
    public ArrayList<String> registeredLotteries;

    public ArrayList<String> savedEvents;

    /**
     * Default constructor for firebase
     */
    public UserProfile() {
        savedEvents = new ArrayList<>();
        registeredLotteries = new ArrayList<>();
    }

    /**
     * Overridden equals constructor for comparison of displayed list for removal. Only compares uid since uid is expected to be unique per profile
     * @param o other object
     * @return if equals.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile other = (UserProfile) o;
        return uid.equals(other.uid);
    }
}
