package com.example.lotteryapp.entrant;

import com.example.lotteryapp.reusecomponent.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Local unit tests for event filtering logic (tag + organizer).
 */
public class EventFilterTest {
    private List<Event> filterEvents(List<Event> all,
                                     String tagFilter,
                                     String organizerFilter) {
        List<Event> result = new ArrayList<>();
        for (Event e : all) {
            // tag filtering
            if (!"All Tags".equals(tagFilter)) {
                if (e.getFilters() == null || !e.getFilters().contains(tagFilter)) {
                    continue;
                }
            }
            // organizer filtering
            if (!"All Organizers".equals(organizerFilter)) {
                if (e.getOrganizer() == null ||
                        !e.getOrganizer().equalsIgnoreCase(organizerFilter)) {
                    continue;
                }
            }
            result.add(e);
        }
        return result;
    }

    private Event makeEvent(String id,
                            String organizer,
                            String... tags) {
        Event e = new Event();
        e.id = id;
        e.setOrganizer(organizer);
        e.setFilters(new ArrayList<>(Arrays.asList(tags)));
        return e;
    }

    @Test
    public void allFiltersAll_returnsAllEvents() {
        List<Event> all = Arrays.asList(
                makeEvent("e1", "orgA", "Sports", "Kids"),
                makeEvent("e2", "orgB", "Music"),
                makeEvent("e3", "orgA", "Sports")
        );

        List<Event> filtered = filterEvents(all, "All Tags", "All Organizers");

        assertEquals(3, filtered.size());
    }

    @Test
    public void filterByTag_onlyEventsWithThatTag() {
        List<Event> all = Arrays.asList(
                makeEvent("e1", "orgA", "Sports", "Kids"),
                makeEvent("e2", "orgB", "Music"),
                makeEvent("e3", "orgA", "Sports")
        );

        List<Event> filtered = filterEvents(all, "Sports", "All Organizers");

        // e1, e3 have Sports，e2 dont
        assertEquals(2, filtered.size());
        List<String> ids = Arrays.asList(filtered.get(0).id, filtered.get(1).id);
        assertEquals(true, ids.contains("e1"));
        assertEquals(true, ids.contains("e3"));
    }

    @Test
    public void filterByOrganizer_onlyEventsFromThatOrganizer() {
        List<Event> all = Arrays.asList(
                makeEvent("e1", "orgA", "Sports", "Kids"),
                makeEvent("e2", "orgB", "Music"),
                makeEvent("e3", "orgA", "Sports")
        );

        List<Event> filtered = filterEvents(all, "All Tags", "orgA");

        // orgA only have e1, e3
        assertEquals(2, filtered.size());
        List<String> ids = Arrays.asList(filtered.get(0).id, filtered.get(1).id);
        assertEquals(true, ids.contains("e1"));
        assertEquals(true, ids.contains("e3"));
    }

    @Test
    public void filterByTagAndOrganizer_intersectionOnly() {
        List<Event> all = Arrays.asList(
                makeEvent("e1", "orgA", "Sports", "Kids"),
                makeEvent("e2", "orgB", "Sports"),
                makeEvent("e3", "orgA", "Music")
        );

        List<Event> filtered = filterEvents(all, "Sports", "orgA");

        // only e1 have sports and organizer = orgA
        assertEquals(1, filtered.size());
        assertEquals("e1", filtered.get(0).id);
    }
}
