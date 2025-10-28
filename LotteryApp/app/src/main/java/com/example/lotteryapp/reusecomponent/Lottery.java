/*
 * Main Class defining Lotteries and their operations
 *
 * Used per Event to draw participants
 *
 * Created by: Ben Fisher on 28/10/2025
 * Last Modified by: Ben Fisher on 28/10/2025
 * */


package com.example.lotteryapp.reusecomponent;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Lottery {
    private LocalDateTime registrationEnd;
    private Integer maxEntrants; // If Null -> no limit
    private ArrayList<Entrant> entrants;

    // ----------------------------------------------------------------
    // Critical Functionality
    // ----------------------------------------------------------------

    public Lottery() {
        // Created initially as blank
    }

    public void isValid() throws IllegalStateException{
        if (registrationEnd == null)
                throw new IllegalStateException("Registration must have an end date");
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------
    public LocalDateTime getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(LocalDateTime registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public void addEntrant(Entrant entrant) { // May want to make throwable for debugging
        if (!this.entrants.contains(entrant)){
            this.entrants.add(entrant);
        }
    }

    public void removeEntrant(Entrant entrant){ // May want to make throwable for debugging
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
