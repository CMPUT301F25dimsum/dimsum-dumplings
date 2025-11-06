package com.example.lotteryapp.admin;

import static org.junit.Assert.*;

import com.example.lotteryapp.reusecomponent.Notification;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Unit tests for AdminNoticeFragment's filtering logic.
 * <p>Tests organizer and time-based filtering using a fake fragment
 * that isolates the matchesFilter() logic without Android dependencies.</p>
 */
public class AdminNoticeFilterUnitTest {

    private FakeAdminFragment fragment;
    private Notification notifToday;
    private Notification notifThisWeek;
    private Notification notifOld;
    private Notification notifFromOtherOrganizer;

    /** A simplified copy of the real AdminNoticeFragment filtering logic. */
    static class FakeAdminFragment {
        String organizerFilter = "All Organizers";
        String timeFilter = "All Time";

        boolean matchesFilter(Notification n) {
            boolean organizerMatch = organizerFilter.equals("All Organizers")
                    || (n.sender != null && n.sender.equalsIgnoreCase(organizerFilter));
            boolean timeMatch = true;

            if (n.time != null) {
                Date notifDate = n.time.toDate();
                Calendar now = Calendar.getInstance();

                // Define start and end of today
                Calendar startOfDay = (Calendar) now.clone();
                startOfDay.set(Calendar.HOUR_OF_DAY, 0);
                startOfDay.set(Calendar.MINUTE, 0);
                startOfDay.set(Calendar.SECOND, 0);
                startOfDay.set(Calendar.MILLISECOND, 0);
                Calendar endOfDay = (Calendar) startOfDay.clone();
                endOfDay.add(Calendar.DAY_OF_MONTH, 1);

                // Define start and end of this week
                Calendar startOfWeek = (Calendar) now.clone();
                startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
                startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
                startOfWeek.set(Calendar.MINUTE, 0);
                startOfWeek.set(Calendar.SECOND, 0);
                startOfWeek.set(Calendar.MILLISECOND, 0);
                Calendar endOfWeek = (Calendar) startOfWeek.clone();
                endOfWeek.add(Calendar.WEEK_OF_YEAR, 1);

                switch (timeFilter) {
                    case "Today":
                        timeMatch = notifDate.after(startOfDay.getTime())
                                && notifDate.before(endOfDay.getTime());
                        break;
                    case "This Week":
                        timeMatch = notifDate.after(startOfWeek.getTime())
                                && notifDate.before(endOfWeek.getTime());
                        break;
                    default:
                        timeMatch = true;
                }
            }

            return organizerMatch && timeMatch;
        }
    }

    /** Prepare mock notifications before each test. */
    @Before
    public void setUp() {
        fragment = new FakeAdminFragment();

        // Today
        notifToday = new Notification();
        notifToday.sender = "Organizer A";
        notifToday.time = new Timestamp(new Date());

        // 3 days ago (still this week)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        notifThisWeek = new Notification();
        notifThisWeek.sender = "Organizer A";
        notifThisWeek.time = new Timestamp(cal.getTime());

        // 3 weeks ago
        cal.add(Calendar.WEEK_OF_YEAR, -3);
        notifOld = new Notification();
        notifOld.sender = "Organizer A";
        notifOld.time = new Timestamp(cal.getTime());

        // Different organizer
        notifFromOtherOrganizer = new Notification();
        notifFromOtherOrganizer.sender = "Organizer B";
        notifFromOtherOrganizer.time = new Timestamp(new Date());
    }

    /** Organizer filter should only show matching sender notifications. */
    @Test
    public void testFilterByOrganizer() {
        fragment.organizerFilter = "Organizer A";
        fragment.timeFilter = "All Time";

        assertTrue(fragment.matchesFilter(notifToday));
        assertFalse(fragment.matchesFilter(notifFromOtherOrganizer));
    }

    /** "Today" filter should include only today's notifications. */
    @Test
    public void testFilterByToday() {
        fragment.organizerFilter = "All Organizers";
        fragment.timeFilter = "Today";

        assertTrue(fragment.matchesFilter(notifToday));
        assertFalse(fragment.matchesFilter(notifThisWeek));
        assertFalse(fragment.matchesFilter(notifOld));
    }

    /** "This Week" filter should include all notifications from the current week. */
    @Test
    public void testFilterByThisWeek() {
        fragment.organizerFilter = "All Organizers";
        fragment.timeFilter = "This Week";

        assertTrue(fragment.matchesFilter(notifToday));
        assertTrue(fragment.matchesFilter(notifThisWeek));
        assertFalse(fragment.matchesFilter(notifOld));
    }

    /** Combined filter should only pass notifications matching both organizer and time. */
    @Test
    public void testFilterCombined() {
        fragment.organizerFilter = "Organizer A";
        fragment.timeFilter = "This Week";

        assertTrue(fragment.matchesFilter(notifToday));
        assertFalse(fragment.matchesFilter(notifFromOtherOrganizer));
        assertFalse(fragment.matchesFilter(notifOld));
    }
}
