package com.example.lotteryapp.admin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.example.lotteryapp.R;

/**
 * Purpose: A reusable filter bar component for the Admin Notification screen.
 *           Provides dropdown menus for filtering notifications by organizer
 *           and sorting them by time period.
 * <p>
 * Outstanding Issues: None
 * Author: Eric
 */

public class AdminNoticeFilterBar extends LinearLayout {
    private Spinner organizerSpinner, timeSpinner;

    /**
     * Constructs the AdminNoticeFilterBar view and inflates its layout.
     *
     * @param context The current context in which this view is running.
     * @param attrs   The set of attributes associated with the view's XML tag.
     */
    public AdminNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.fragment_admin_notice_filter_bar, this);
        organizerSpinner = findViewById(R.id.admin_filter_type);
        timeSpinner = findViewById(R.id.admin_filter_sort);
    }

    /**
     * Initializes the filter bar spinners and sets up the event listener
     * that triggers when the user changes filter options.
     *
     * @param listener Callback interface to notify when the selected
     *                 organizer or time filter changes.
     */
    public void initFilter(OnAdminFilter listener) {
        String[] organizers = {"All Organizers", "Organizer A", "Organizer B"};
        String[] times = {"All Time", "Today", "This Week"};
        ArrayAdapter<String> orgAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, organizers);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, times);

        organizerSpinner.setAdapter(orgAdapter);
        timeSpinner.setAdapter(timeAdapter);

        AdapterView.OnItemSelectedListener changeListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                listener.onFilterChanged(
                        organizerSpinner.getSelectedItem().toString(),
                        timeSpinner.getSelectedItem().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        organizerSpinner.setOnItemSelectedListener(changeListener);
        timeSpinner.setOnItemSelectedListener(changeListener);
    }

    /**
     * Interface definition for a callback to be invoked when
     * the admin filter selection changes.
     */
    public interface OnAdminFilter {
        void onFilterChanged(String organizer, String timeRange);
    }
}
