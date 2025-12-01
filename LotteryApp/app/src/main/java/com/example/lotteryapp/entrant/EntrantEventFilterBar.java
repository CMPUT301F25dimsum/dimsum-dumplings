package com.example.lotteryapp.entrant;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import com.example.lotteryapp.R;

/**
 * EntrantEventFilterBar
 *
 * Description:
 *  Fragment that allows entrants to select a drop-down menu
 *  and filter events based on hashtag interests and organizer ids.
 *  Author: Eric
 */

public class EntrantEventFilterBar extends LinearLayout {

    private Spinner tagSpinner;
    private Spinner organizerSpinner;
    private OnEventFilter listener;

    public EntrantEventFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.fragment_entrant_event_filter_bar, this);
        tagSpinner = findViewById(R.id.entrant_event_filter_tag);
        organizerSpinner = findViewById(R.id.entrant_event_filter_organizer);
    }

    /**
     * Initialize the filter bar with a callback.
     * The callback is invoked whenever either spinner selection changes.
     */
    public void initFilter(OnEventFilter listener) {
        this.listener = listener;

        AdapterView.OnItemSelectedListener changeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (EntrantEventFilterBar.this.listener != null) {
                    String selectedTag = tagSpinner.getSelectedItem() != null
                            ? tagSpinner.getSelectedItem().toString()
                            : "All Tags";
                    String selectedOrganizer = organizerSpinner.getSelectedItem() != null
                            ? organizerSpinner.getSelectedItem().toString()
                            : "All Organizers";
                    EntrantEventFilterBar.this.listener.onFilterChanged(selectedTag, selectedOrganizer);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        tagSpinner.setOnItemSelectedListener(changeListener);
        organizerSpinner.setOnItemSelectedListener(changeListener);
    }

    /**
     * Update tag options based on the current events.
     * The list should NOT include "All Tags"; it will be added here.
     */
    public void setTagOptions(List<String> tags) {
        List<String> options = new ArrayList<>();
        options.add("All Tags");
        if (tags != null) {
            options.addAll(tags);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(adapter);
    }

    /**
     * Update organizer options based on the current events.
     * The list should NOT include "All Organizers"; it will be added here.
     */
    public void setOrganizerOptions(List<String> organizers) {
        List<String> options = new ArrayList<>();
        options.add("All Organizers");
        if (organizers != null) {
            options.addAll(organizers);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        organizerSpinner.setAdapter(adapter);
    }

    /**
     * Callback interface for event filter changes.
     */
    public interface OnEventFilter {
        void onFilterChanged(String tag, String organizer);
    }
}