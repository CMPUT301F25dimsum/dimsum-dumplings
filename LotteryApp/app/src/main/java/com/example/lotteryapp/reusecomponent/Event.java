/*
* Main Class defining Events and their contents
*
* Created by: Ben Fisher on 28/10/2025
* Last Modified by: Ben Fisher on 28/10/2025
*
* */

package com.example.lotteryapp.reusecomponent;

import android.location.Address;

import java.util.ArrayList;
import java.util.Date;

public class Event {

    // Event meta-data
    private String organizer; // Might implement as a direct link to Organizer once setup
    private String title;

    // Event details
    private String description;
    private String eventLocation;
    private Date eventTime;
    private int maxCapacity;
    private String bannerURL;
    private ArrayList<String> extraImageURLs;
    private ArrayList<String> filters;

    // Event validation details
    private boolean validateLocation;
    private boolean open;

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
        if (eventTime == null)
            throw new IllegalStateException("Event Time cannot be empty");
        if (!eventTime.after(new Date()))
            throw new IllegalStateException("Cannot have an event in the past");
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
        extraImageURLs = new ArrayList<>();
        filters = new ArrayList<>();
        finalizedEntrants = new ArrayList<>();
    }

    public Event(String organizer){
        // On opening of Event creation page, everything is unknown except organizer
        this.organizer = organizer;
        maxCapacity = -1; // Default for error handling
        validateLocation = false; // Defaults off

        // Initialize members
        lottery = new Lottery();
        extraImageURLs = new ArrayList<>();
        filters = new ArrayList<>();
        finalizedEntrants = new ArrayList<>();
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public ArrayList<String> getExtraImageURLs() {
        return extraImageURLs;
    }

    public void setExtraImageURLs(ArrayList<String> extraImageURLs) {
        this.extraImageURLs = extraImageURLs;
    }

    public void setFilters(ArrayList<String> filters) {
        this.filters = filters;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
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

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public void setLotteryEndDate(Date date) {
        if (date.after(new Date(System.currentTimeMillis())))
            this.lottery.setRegistrationEnd(date);
    }

    public Date getLotteryEndDate(){
        return this.lottery.getRegistrationEnd();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setRegistrationLimit(int limit){ lottery.setMaxEntrants(limit); }
    public int getRegistrationLimit(){return lottery.getMaxEntrants(); };

    public boolean checkIsOpen(){
        return open;
    }

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String banner) {
        this.bannerURL = banner;
    }

    public void addExtraImageURL(String url){
        if (!extraImageURLs.contains(url)){
            extraImageURLs.add(url);
        }
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
