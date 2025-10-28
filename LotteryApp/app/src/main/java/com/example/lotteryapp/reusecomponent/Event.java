package com.example.lotteryapp.reusecomponent;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    public String title;
    public String organizer_name;
    public ArrayList<String> filters;
    public String location;
    public int lottery_size;
    public Date deadline;
    public boolean lim_waiting;
    public int lim_waiting_size;
    public boolean require_location;
    public String banner_uri;
    public String image1_uri;
    public String image2_uri;
    public String image3_uri;

    public Event() {}
}
