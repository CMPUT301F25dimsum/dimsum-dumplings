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

/**
 * Describes a class used store the applicants and winners of each event lottery.
 *
 * @author justin1
 */
public class Lottery {
    public Date registrationEnd;
    public Date registrationStart;

    private int maxEntrants; // If <=0 -> no limit
    private ArrayList<String> entrants;

    // ----------------------------------------------------------------
    // Critical Functionality
    // ----------------------------------------------------------------

    /**
     * Default constructor, initializing variables
     */
    public Lottery() {
        // Created initially as blank
        entrants = new ArrayList<>();
        maxEntrants = -1;
    }

    /**
     * Validates the lottery state, throwing an exception if invalid
     * @throws IllegalStateException If parameters are incorrect
     */
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

    /**
     * Get entrants
     * @return entrants
     */
    public ArrayList<String> getEntrants() {
        return entrants;
    }

    /**
     * Set entrants
     * @param entrants entrants
     */
    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }

    /**
     * Add entrants
     * @param entrant entrant
     * @return success
     */
    public boolean addEntrant(String entrant) { // May want to make throwable for debugging
        if (!this.entrants.contains(entrant)){
            if (this.entrants.size() < maxEntrants || this.maxEntrants == -1){
                this.entrants.add(entrant);
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the lottery is open relative to right now
     * @return open
     */
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

    /**
     * Remove the entrant from lottery
     * @param entrant entrant id
     */
    public void removeEntrant(String entrant){ // May want to make throwable for debugging
        this.entrants.remove(entrant);
    }

    /**
     * Check if entrant in lottery
     * @param entrant entrant
     * @return if in lottery
     */
    public boolean containsEntrant(String entrant) {
        return this.entrants.contains(entrant);
    }

    /**
     * Check if lottery full
     * @return fullness
     */
    public boolean isFull(){
        return this.entrants.size() == maxEntrants;
    }

    /**
     * Get how many entrants
     * @return entrant size
     */
    public int getNEntrants(){
        return this.entrants.size();
    }

    /**
     * Get max entrants
     * @return max entrants
     */
    public Integer getMaxEntrants() {
        return maxEntrants;
    }

    /**
     * Set max entrants
     * @param maxEntrants max entrants
     */
    public void setMaxEntrants(Integer maxEntrants) {
        if (maxEntrants <= 0){
            return;
        }
        this.maxEntrants = maxEntrants;
    }
}
