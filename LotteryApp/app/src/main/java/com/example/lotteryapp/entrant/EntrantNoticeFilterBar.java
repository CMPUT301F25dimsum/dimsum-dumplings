package com.example.lotteryapp.entrant;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.lotteryapp.R;

public class EntrantNoticeFilterBar extends LinearLayout {
    private final Spinner spinner;
    public EntrantNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.fragment_entrant_notice_filter_bar, this);
        spinner = findViewById(R.id.fragment_entrant_notice_filter_bar_type);
    }
    public void initFilter() {
        String[] options = {"All", "Success", "Failure", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
