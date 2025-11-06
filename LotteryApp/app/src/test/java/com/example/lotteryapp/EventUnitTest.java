package com.example.lotteryapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.example.lotteryapp.reusecomponent.Event;

import org.junit.Test;

import java.util.Date;

public class EventUnitTest {
    public Event mockEmptyEvent(){
        return new Event("unit tester");
    }
    public Event completeEvent(){
        Event event = mockEmptyEvent();
        event.setTitle("Test Event");
        event.setDescription("This is a test event for unit testing validation");
        event.setEventLocation("Event location is imaginary");
        event.setEventTime(new Date(System.currentTimeMillis() + 64800000));
        event.setMaxCapacity(100);
        event.addFilter("Test");
        event.getLottery().registrationEnd =(new Date(System.currentTimeMillis() + 64800000));
        event.getLottery().registrationStart =(new Date(System.currentTimeMillis()));
        return event;
    }

    @Test
    public void testEventValidation() {
        Event event = mockEmptyEvent();

        // Add one member at a time and ensure event is invalid throughout the process
        assertThrows(IllegalStateException.class, event::isValid);
        event.setTitle("Test Event");
        assertThrows(IllegalStateException.class, event::isValid);
        event.setDescription("This is a test event for unit testing validation");
        assertThrows(IllegalStateException.class, event::isValid);
        event.setEventLocation("Event location is imaginary");
        assertThrows(IllegalStateException.class, event::isValid);
        event.setEventTime(new Date(System.currentTimeMillis() + 64800000));
        assertThrows(IllegalStateException.class, event::isValid);
        event.setMaxCapacity(100);
        assertThrows(IllegalStateException.class, event::isValid);
        event.addFilter("Test");
        assertThrows(IllegalStateException.class, event::isValid);
        event.getLottery().registrationEnd =(new Date(System.currentTimeMillis() + 64800000));
        event.getLottery().registrationStart =(new Date(System.currentTimeMillis()));
        // Event should now be valid (raises exception if not)
        event.isValid();
        // Verify Exception if event date is invalid
        event.setEventTime(new Date(System.currentTimeMillis() - 64800000));
        assertThrows(IllegalStateException.class, event::isValid);
    }
}
