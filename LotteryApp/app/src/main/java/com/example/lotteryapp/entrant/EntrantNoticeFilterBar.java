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
    public EntrantNoticeFilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.filterbar_entrants, this);
    }
}
