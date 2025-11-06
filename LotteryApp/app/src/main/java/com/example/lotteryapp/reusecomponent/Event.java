/*
* Main Class defining Events and their contents
*
* Created by: Ben Fisher on 28/10/2025
* Last Modified by: Ben Fisher on 28/10/2025
*
* */

package com.example.lotteryapp.reusecomponent;

import android.location.Address;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.example.lotteryapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class represents each event data type and all details. It is Firebase Store-able
 *
 * @author bsfisher
 */
public class Event {

    // Event meta-data
    public String id;
    private String organizer; // Might implement as a direct link to Organizer once setup
    private String title;

    // Event details
    private String description;
    private String eventLocation;
    private Date eventTime;
    private int maxCapacity;
    private String bannerURL;
    private ArrayList<String> filters;

    // Event validation details
    private boolean validateLocation;

    // Event lottery
    private Lottery lottery;
    private ArrayList<String> finalizedEntrants;

    // ----------------------------------------------------------------
    // Critical Functionality
    // ----------------------------------------------------------------

    /**
     * Throws an exception if the event properties are invalid
     * @throws IllegalStateException if parameters are invalid
     */
    public void isValid() throws IllegalStateException {
        // Validate information
        if (title == null || title.isEmpty())
            throw new IllegalStateException("Title cannot be empty");
        if (description == null || description.isEmpty())
            throw new IllegalStateException("Description cannot be empty");
        if (eventLocation == null || eventLocation.isEmpty())
            throw new IllegalStateException("Location cannot be empty");
        if (eventTime == null || !eventTime.after(new Date()))
            throw new IllegalStateException("Event Time cannot be empty or in the past");
        if (maxCapacity <= 0)
            throw new IllegalStateException("Event Capacity must be > 0");
        if (filters.isEmpty())
            throw new IllegalStateException("At least 1 filter required");
        lottery.isValid();
    }

    /**
     * Default constructor for Firebase
     */
    public Event(){
        // For FireStore
        organizer = "Default";
        maxCapacity = -1; // Default for error handling
        validateLocation = false; // Defaults off
        bannerURL = "Placeholder";

        // Initialize members
        lottery = new Lottery();
        filters = new ArrayList<>();
        finalizedEntrants = new ArrayList<>();
    }

    /**
     * Creation of event from specific organizer
     * @param organizer OrganizerID
     */
    public Event(String organizer){
        this();
        // On opening of Event creation page, everything is unknown except organizer
        this.organizer = organizer;
    }

    /**
     * Fine grain constructor
     * @param organizer organizer ID
     * @param bannerURL banner url
     * @param title title
     * @param desc description
     * @param location location
     * @param eventTime event time
     * @param lotteryStart lottery start time
     * @param lotteryEnd lottery end time
     * @param maxCapacity capacity
     * @param limitedWaiting bool limited waiting
     * @param filters filters
     * @param validateLocation bool enable geolocation
     */
    public Event(String organizer, String bannerURL, String title, String desc, String location,
                 Date eventTime, Date lotteryStart, Date lotteryEnd, int maxCapacity, int limitedWaiting,
                 ArrayList<String> filters, boolean validateLocation){
        this(organizer);
        this.bannerURL = bannerURL;
        this.title = title;
        this.description = desc;
        this.eventLocation = location;
        this.eventTime = eventTime;
        this.lottery.registrationStart = lotteryStart;
        this.lottery.registrationEnd = lotteryEnd;
        this.lottery.setMaxEntrants(limitedWaiting);
        this.filters = filters;
        this.validateLocation = validateLocation;
    }

    // ----------------------------------------------------------------
    // Comparators
    // ----------------------------------------------------------------

    /**
     * Equivalence comparator
     * @param o other
     * @return if equal
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event that = (Event) o;
        return (id == that.id && organizer.equals(that.organizer));
    }
    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    /**
     * Set organizer
     * @param organizer Organizer id
     */
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    /**
     * Set filters
     * @param filters filters
     */
    public void setFilters(ArrayList<String> filters) {
        this.filters = filters;
    }

    /**
     * Get openess
     * @return if open
     */
    public String isOpen() {
        if (lottery.isOpen())
            return "Open";
        return "Closed";
    }

    /**
     * Get lottery
     * @return lottery
     */
    public Lottery getLottery() {
        return lottery;
    }

    /**
     * Set lottery
     * @param lottery lottery
     */
    public void setLottery(Lottery lottery) {
        this.lottery = lottery;
    }

    /**
     * Get finalized entrants
     * @return finalized entrants
     */
    public ArrayList<String> getFinalizedEntrants() {
        return finalizedEntrants;
    }

    /**
     * Set finalized entrants
     * @param finalizedEntrants finalized entrants
     */
    public void setFinalizedEntrants(ArrayList<String> finalizedEntrants) {
        this.finalizedEntrants = finalizedEntrants;
    }

    /**
     * Get organizer
     * @return organizer id
     */
    public String getOrganizer() {
        return organizer;
    }

    /**
     * Get title
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get event location
     * @return event location
     */
    public String getEventLocation() {
        return eventLocation;
    }

    /**
     * Set event location
     * @param eventLocation event location
     */
    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    /**
     * Get event time
     * @return event time
     */
    public Date getEventTime() {
        return eventTime;
    }

    /**
     * Get N entrants
     * @return N entrants in lottery
     */
    public int getNentrants() { return lottery.getNEntrants(); }

    /**
     * Set event time
     * @param eventTime event time
     */
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Get Max capacity
     * @return max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Set max capacity
     * @param maxCapacity max capacity
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Set registration limit
     * @param limit limit
     */
    public void setRegistrationLimit(int limit){ lottery.setMaxEntrants(limit); }

    /**
     * Get registration limit
     * @return limit
     */
    public int getRegistrationLimit(){return lottery.getMaxEntrants(); };

    /**
     * Get banner url
     * @return url
     */
    public String getBannerURL() {
        return bannerURL;
    }

    /**
     * Set banner url
     * @param banner url
     */
    public void setBannerURL(String banner) {
        this.bannerURL = banner;
    }

    /**
     * Get filters
     * @return filters
     */
    public ArrayList<String> getFilters() {
        return filters;
    }

    /**
     * Add filters
     * @param filter filter
     */
    public void addFilter(String filter) {
        if (!this.filters.contains(filter))
            this.filters.add(filter);
    }

    /**
     * Remove filter
     * @param filter filter
     */
    public void removeFilter(String filter){
        this.filters.remove(filter);
    }

    /**
     * Get location toggle
     * @return location toggle
     */
    public boolean isValidateLocation() {
        return validateLocation;
    }

    /**
     * Set location toggle
     * @param validateLocation toggle
     */
    public void setValidateLocation(boolean validateLocation) {
        this.validateLocation = validateLocation;
    }

}
