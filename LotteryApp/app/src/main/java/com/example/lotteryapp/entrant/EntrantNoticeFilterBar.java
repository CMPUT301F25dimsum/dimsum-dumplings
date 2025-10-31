package com.example.lotteryapp.entrant;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.lotteryapp.R;

public class EntrantNoticeFilterBar extends LinearLayout {
    private Spinner spinner;
    public EntrantNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.filterbar_entrants, this);
        spinner = findViewById(R.id.entrant_filter_type);
    }
    public void initFilter(OnFilterChanged listener) {
        String[] options = {"All", "Success", "Failure", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                listener.onFilterChanged(options[pos]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public interface OnFilterChanged {
        void onFilterChanged(String filter);
    }
}
