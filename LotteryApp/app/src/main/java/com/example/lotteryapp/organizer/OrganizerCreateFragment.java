package com.example.lotteryapp.organizer;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.EditableImage;

public class OrganizerCreateFragment extends Fragment {

    private EditableImage banner;
    private EditableImage image1;
    private EditableImage image2;
    private EditableImage image3;

    public OrganizerCreateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_organizer_create, container, false);

        banner = ret.findViewById(R.id.fragment_organizer_create_banner);
        image1 = ret.findViewById(R.id.fragment_organizer_create_image_1);
        image2 = ret.findViewById(R.id.fragment_organizer_create_image_2);
        image3 = ret.findViewById(R.id.fragment_organizer_create_image_3);

        SwitchCompat limSizeSwitch = ret.findViewById(R.id.fragment_organizer_create_limited_waiting);
        TextView limitedSizeText = ret.findViewById(R.id.fragment_organizer_create_lim_waiting_size_text);
        TextView limitedSizeAmount = ret.findViewById(R.id.fragment_organizer_create_lim_waiting_size);
        limSizeSwitch.setOnCheckedChangeListener((bv, isChecked) -> {
            if (isChecked) {
                limitedSizeText.setVisibility(VISIBLE);
                limitedSizeAmount.setVisibility(VISIBLE);
            } else {
                limitedSizeText.setVisibility(GONE);
                limitedSizeAmount.setVisibility(GONE);
            }
        });

        return ret;
    }

    /// ChatGPT generated lines 63-70 for swappable images
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        banner.registerLauncher(this);
        image1.registerLauncher(this);
        image2.registerLauncher(this);
        image3.registerLauncher(this);
    }
}