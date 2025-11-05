/*
 * Main Class defining Lotteries and their operations
 *
 * Used per Event to draw participants
 *
 * Created by: Ben Fisher on 28/10/2025
 * Last Modified by: Ben Fisher on 28/10/2025
 * */


package com.example.lotteryapp.reusecomponent;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class Lottery {
    public Date registrationEnd;
    public Date registrationStart;

    private int maxEntrants; // If <=0 -> no limit
    private ArrayList<String> entrants;

    // ----------------------------------------------------------------
    // Critical Functionality
    // ----------------------------------------------------------------

    public Lottery() {
        // Created initially as blank
        entrants = new ArrayList<>();
        maxEntrants = -1;
    }

    public void isValid() throws IllegalStateException{
        if (registrationStart == null)
            throw new IllegalStateException("Registration must have a valid start date");
        if (registrationEnd == null || registrationEnd.before(new Date()))
            throw new IllegalStateException("Registration must have a valid end date");
        if (registrationStart.after(registrationEnd))
            throw new IllegalStateException("Registration start must be before end");
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------
    public ArrayList<String> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }

    public boolean addEntrant(String entrant) { // May want to make throwable for debugging
        if (!this.entrants.contains(entrant)){
            if (this.entrants.size() < maxEntrants || this.maxEntrants == -1){
                this.entrants.add(entrant);
                return true;
            }
        }
        return false;
    }

    public boolean isOpen(){
        Date current = new Date();
        if (registrationStart == null && registrationEnd == null)
            return true;
        if (registrationStart == null)
            return current.before(registrationEnd);
        if (registrationEnd == null)
            return current.after(registrationStart);
        return (current.before(registrationEnd) && current.after(registrationStart));
    }

    public void removeEntrant(String entrant){ // May want to make throwable for debugging
        this.entrants.remove(entrant);
    }

    public boolean containsEntrant(String entrant) {
        return this.entrants.contains(entrant);
    }

    public boolean isFull(){
        return this.entrants.size() == maxEntrants;
    }

    public int getNEntrants(){
        return this.entrants.size();
    }

    public Integer getMaxEntrants() {
        return maxEntrants;
    }

    public void setMaxEntrants(Integer maxEntrants) {
        if (maxEntrants <= 0){
            return;
        }
        this.maxEntrants = maxEntrants;
    }
}
