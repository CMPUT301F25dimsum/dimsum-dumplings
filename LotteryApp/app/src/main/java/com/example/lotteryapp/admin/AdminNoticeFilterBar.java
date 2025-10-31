package com.example.lotteryapp.admin;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.example.lotteryapp.R;

public class AdminNoticeFilterBar extends LinearLayout {
    private Spinner organizerSpinner, timeSpinner;
    public AdminNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.filterbar_admin, this);
        organizerSpinner = findViewById(R.id.admin_filter_type);
        timeSpinner = findViewById(R.id.admin_filter_sort);
    }

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

    public interface OnAdminFilter {
        void onFilterChanged(String organizer, String timeRange);
    }
}
