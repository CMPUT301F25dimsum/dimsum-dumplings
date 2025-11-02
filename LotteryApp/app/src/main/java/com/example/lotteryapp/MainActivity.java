package com.example.lotteryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lotteryapp.admin.AdminActivity;
import com.example.lotteryapp.entrant.EntrantActivity;
import com.example.lotteryapp.organizer.OrganizerActivity;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

/**
 * MainActivity
 *
 * Purpose:
 * - Entry screen for sign-up and deep-link handling.
 * - Server-validated auto-skip: if local loginInfo exists, verify user doc on Firestore (SERVER)
 *   before routing to the role home.
 * - Applies a phone input mask "555 555 5555" while typing and enforces 10 digits on submit.
 */
public class MainActivity extends AppCompatActivity {

    // -------- SharedPreferences contract (must match project spec) --------
    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_UID = "UID";
    private static final String KEY_ROLE = "Role";
    private static final String KEY_ENABLE_ORG = "enableOrganizerNotif";
    private static final String KEY_ENABLE_ADMIN = "enableAdminNotif";
    private static final String KEY_HAS_ACCOUNT = "hasAccount";

    // -------- UI --------
    private EditText Email, Name, Phone;
    private MaterialAutoCompleteTextView roleDropdown;
    private Button btnSignup, btnCancel;

    // -------- Firebase + service --------
    private FirebaseFirestore db;
    private SignUpService signUpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Avoid content under status/nav bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        signUpService = new SignUpService(db);

        // Try server-validated auto-skip; if it shows the form, we bind in the callback below
        tryServerValidatedAutoSkipOrBindForm();
    }

    /**
     * If local prefs indicate a linked account, verify on server (Firestore, SERVER source).
     * - If the doc exists -> route & finish.
     * - If missing -> clear prefs and show sign-up form.
     * If no local link -> show sign-up form.
     */
    private void tryServerValidatedAutoSkipOrBindForm() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        boolean hasAccount = prefs.getBoolean(KEY_HAS_ACCOUNT, false);
        String uid = prefs.getString(KEY_UID, null);
        String savedRole = prefs.getString(KEY_ROLE, "entrant");

        if (hasAccount && uid != null) {
            db.collection("user").document(uid)
                    .get(Source.SERVER)
                    .addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            routeToRole(savedRole, getDeepLinkEventId());
                            finish();
                        } else {
                            prefs.edit().clear().apply();
                            Toast.makeText(this, "Profile not found. Please sign up again.", Toast.LENGTH_SHORT).show();
                            bindViewsAndWireUp();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Server validation failed", e);
                        Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                        bindViewsAndWireUp();
                    });
        } else {
            bindViewsAndWireUp();
        }
    }

    /**
     * Bind views, attach phone mask, set role dropdown and button listeners.
     */
    private void bindViewsAndWireUp() {
        Email = findViewById(R.id.etEmail);
        Name  = findViewById(R.id.etName);
        Phone = findViewById(R.id.etPhone);
        roleDropdown = findViewById(R.id.main_role_dropdown);
        btnSignup = findViewById(R.id.main_signup_button);
        btnCancel = findViewById(R.id.main_cancel_button);

        // Phone mask "555 555 5555" while typing
        SignUpService.attachUsPhoneMask(Phone);

        // Role dropdown
        String[] roles = {"Entrant", "Organizer", "Admin"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles);
        roleDropdown.setAdapter(adapter);

        btnSignup.setOnClickListener(v -> onSignUp());
        btnCancel.setOnClickListener(v -> closeApp());
    }

    /**
     * Collect inputs, enforce 10-digit phone, and call SignUpService.
     * Stores digits-only phone number to Firestore.
     */
    private void onSignUp() {
        String email = safeText(Email);
        String name  = safeText(Name);
        String phoneFormatted = safeText(Phone);                 // e.g., "555 555 5555"
        String phoneDigits = phoneFormatted.replaceAll("\\D", ""); // "5555555555"
        String roleDisplay = roleDropdown.getText().toString().trim();
        String eid = getDeepLinkEventId();

        if (TextUtils.isEmpty(roleDisplay)) {
            roleDropdown.setError("Select account type");
            roleDropdown.requestFocus();
            return;
        }

        // Enforce exactly 10 digits for US/CA style numbers
        if (phoneDigits.length() != 10) {
            Phone.setError("Enter a 10-digit phone number (e.g., 555 555 5555)");
            Phone.requestFocus();
            return;
        }

        signUpService.signUp(
                this, email, name, phoneDigits, roleDisplay,
                new SignUpService.Callback() {
                    @Override
                    public void onSuccess(String uid, String canonicalRole) {
                        Toast.makeText(MainActivity.this, "Sign up success", Toast.LENGTH_SHORT).show();
                        routeToRole(canonicalRole, eid);
                        finish();
                    }
                    @Override
                    public void onError(String message, Exception e) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /** Extracts lotteryapp://.../?eid=XXXX if the app was opened via deep link. */
    @Nullable
    private String getDeepLinkEventId() {
        Uri data = getIntent().getData();
        if (data != null && "lotteryapp".equals(data.getScheme())) {
            String eid = data.getQueryParameter("eid");
            Log.d("DeepLink", "Opened with event ID: " + eid);
            return eid;
        }
        return null;
    }

    /** Route to the correct Activity based on canonical role; forward eid if present. */
    private void routeToRole(String canonicalRole, @Nullable String eid) {
        Intent intent;
        switch (canonicalRole) {
            case "organizer":
                intent = new Intent(this, OrganizerActivity.class); break;
            case "admin":
                intent = new Intent(this, AdminActivity.class); break;
            default:
                intent = new Intent(this, EntrantActivity.class);
        }
        if (eid != null) intent.putExtra("eid", eid);
        startActivity(intent);
    }

    /** App close (used by the Cancel button). Assist with Chatgpt 2025.11.1 */
    private void closeApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
            moveTaskToBack(true);
        } else {
            moveTaskToBack(true);
            finish();
        }
    }
    /** Safely get trimmed text from an EditText (never returns null). */
    private static String safeText(EditText et) {
        CharSequence cs = et.getText();
        return cs == null ? "" : cs.toString().trim();
    }
}
