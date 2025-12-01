package com.example.lotteryapp.reusecomponent;

/**
 * Purpose:
 * This model class represents an entrant in a lottery.
 * Each entrant has a status and can be mapped to a profile.
 * Expected that lottery entrants are unique.
 * Not meant to be serialized into Firebase.
 *
 * Issues:
 * None
 *
 * @author wjlee1
 */
public class LotteryEntrant {
    public String uid;
    public boolean bSelected;

    /**
     * Enum representing status, displayed on each lottery status
     */
    public enum Status {
        Registered, Invited, Waitlisted, Cancelled, Declined, Accepted
    }
    public Status status;
    //public boolean bValidUID; //Do not touch this

    /**
     * Constructor for the entrant, with extra parameters.
     * @param id UID
     * @param bSelected If the entrant is checked
     * @param status Status
     */
    public LotteryEntrant(String id, boolean bSelected, Status status) {
        this.uid = id;
        this.bSelected = bSelected;
        this.status = status;
        //this.bValidUID = true;
    }

    public String getUid() {
        return uid;
    }

    public String getStatus() {
        return status.toString();
    }

    /**
     * Simple constructor for the entrant with just the UID.
     * @param id UID
     */
    public LotteryEntrant(String id) {
        this.uid = id;
        bSelected = false;
        status = Status.Registered;
        //bValidUID = true;
    }

    /**
     * Overloaded equals operator for collection comparisons.
     * @param o Other object
     * @return if equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                         //ChatGPT caught this edge case
        if (!(o instanceof LotteryEntrant)) return false;
        return this.uid.equals(((LotteryEntrant) o).uid);
    }

    /**
     * Overloaded hashcode for set operations (Set not used)
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
