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
    private Date registrationEnd;

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
        if (registrationEnd == null)
                throw new IllegalStateException("Registration must have an end date");
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------
    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public ArrayList<String> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }

    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public void addEntrant(String entrant) { // May want to make throwable for debugging
        if (!this.entrants.contains(entrant)){
            this.entrants.add(entrant);
        }
    }

    public void removeEntrant(String entrant){ // May want to make throwable for debugging
        this.entrants.remove(entrant);
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
