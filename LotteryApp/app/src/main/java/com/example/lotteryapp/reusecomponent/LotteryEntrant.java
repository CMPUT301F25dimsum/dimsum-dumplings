package com.example.lotteryapp.reusecomponent;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LotteryEntrant {
    public String uid;
    public boolean bSelected;
    public enum Status {
        Registered, Invited, Waitlisted, Cancelled, Declined, Accepted
    }
    public Status status;
    //public boolean bValidUID; //Do not touch this
    public LotteryEntrant(String id, boolean bSelected, Status status) {
        this.uid = id;
        this.bSelected = bSelected;
        this.status = status;
        //this.bValidUID = true;
    }

    public LotteryEntrant(String id) {
        this.uid = id;
        bSelected = false;
        status = Status.Registered;
        //bValidUID = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LotteryEntrant)) return false;
        return this.uid.equals(((LotteryEntrant) o).uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
