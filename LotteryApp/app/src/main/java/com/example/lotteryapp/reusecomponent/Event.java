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

    public Event(String organizer){
        this();
        // On opening of Event creation page, everything is unknown except organizer
        this.organizer = organizer;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event that = (Event) o;
        return (id == that.id && organizer.equals(that.organizer));
    }
    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public void setFilters(ArrayList<String> filters) {
        this.filters = filters;
    }

    public String isOpen() {
        if (lottery.isOpen())
            return "Open";
        return "Closed";
    }


    public Lottery getLottery() {
        return lottery;
    }

    public void setLottery(Lottery lottery) {
        this.lottery = lottery;
    }

    public ArrayList<String> getFinalizedEntrants() {
        return finalizedEntrants;
    }

    public void setFinalizedEntrants(ArrayList<String> finalizedEntrants) {
        this.finalizedEntrants = finalizedEntrants;
    }


    public String getOrganizer() {
        return organizer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public int getNentrants() { return lottery.getNEntrants(); }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setRegistrationLimit(int limit){ lottery.setMaxEntrants(limit); }
    public int getRegistrationLimit(){return lottery.getMaxEntrants(); };

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String banner) {
        this.bannerURL = banner;
    }

    public ArrayList<String> getFilters() {
        return filters;
    }

    public void addFilter(String filter) {
        if (!this.filters.contains(filter))
            this.filters.add(filter);
    }

    public void removeFilter(String filter){
        this.filters.remove(filter);
    }

    public boolean isValidateLocation() {
        return validateLocation;
    }

    public void setValidateLocation(boolean validateLocation) {
        this.validateLocation = validateLocation;
    }

}
