package com.example.lotteryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lotteryapp.admin.AdminActivity;
import com.example.lotteryapp.entrant.EntrantActivity;
import com.example.lotteryapp.organizer.OrganizerActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner s = findViewById(R.id.main_role_dropdown);

        String[] roles = {"Entrant", "Organizer", "Admin"};

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s.setAdapter(adapter);

        Button b = findViewById(R.id.main_signup_button);

        Uri data = getIntent().getData();
        if (data != null && "lotteryapp".equals(data.getScheme())) {
            String eventId = data.getQueryParameter("eid");
            Log.d("DeepLink", "Opened with event ID: " + eventId);
        }

        b.setOnClickListener(v -> {
            switch (s.getSelectedItem().toString()) {
                case "Entrant":
                    startActivity(new Intent(this, EntrantActivity.class));
                    finish();
                    break;
                case "Organizer":
                    startActivity(new Intent(this, OrganizerActivity.class));
                    finish();
                    break;
                case "Admin":
                    startActivity(new Intent(this, AdminActivity.class));
                    finish();
                    break;
            }
        });
    }
}