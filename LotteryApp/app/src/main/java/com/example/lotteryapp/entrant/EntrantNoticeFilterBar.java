package com.example.lotteryapp.entrant;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.lotteryapp.R;

/**
 * Purpose: A reusable filter bar for entrant notifications.
 *           Provides a dropdown spinner to filter notifications
 *           by their type (Success, Failure, or Custom).
 * <p>
 * Outstanding Issues: None
 * Author: Eric
 */
public class EntrantNoticeFilterBar extends LinearLayout {
    private final Spinner spinner;

    /**
     * Constructs the EntrantNoticeFilterBar and inflates its layout.
     *
     * @param context The context in which this view is running.
     * @param attrs   The set of attributes associated with the XML tag.
     */
    public EntrantNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.fragment_entrant_notice_filter_bar, this);
        spinner = findViewById(R.id.fragment_entrant_notice_filter_bar_type);
    }

    /**
     * Initializes the filter spinner with predefined filter options.
     * Options include "All", "Success", "Failure", and "Custom".
     */
    public void initFilter() {
        String[] options = {"All", "Success", "Failure", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
