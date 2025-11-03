package com.example.lotteryapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * LotteryApp - MainActivity
 * - On launch: if a saved UID exists (already signed up), jump to role screen.
 * - Otherwise: show the sign-up screen (no deep links, no server validation).
 * - Validates phone as 10 digits; on error, show a Toast (no setError/focus).
 */
public class MainActivity extends AppCompatActivity {

    // -------- SharedPreferences keys (local auto-skip) --------
    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_UID = "UID";
    private static final String KEY_ROLE = "Role";
    private static final String KEY_HAS_ACCOUNT = "hasAccount";

    // -------- UI --------
    private EditText Email, Name, Phone;
    private MaterialAutoCompleteTextView roleDropdown;
    private Button btnSignup, btnCancel;

    // -------- Firebase + service --------
    private FirebaseFirestore db;
    private SignUpService signUpService;

    /**
     * Set up UI. If a saved UID exists, route to the saved role; else bind the sign-up form.
     *
     * @param savedInstanceState state when re-created; may be null
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Avoid drawing under system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        signUpService = new SignUpService(db);

        // Local-only auto-skip: if UID exists, jump to role screen
        if (!tryLocalAutoSkip()) {
            bindViewsAndWireUp();
        }
    }

    /**
     * If a local account (UID) exists, go to its role screen and finish.
     *
     * @return true if routed (auto-skipped), false if no local account and should show form
     */
    private boolean tryLocalAutoSkip() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        boolean hasAccount = prefs.getBoolean(KEY_HAS_ACCOUNT, false);
        String uid = prefs.getString(KEY_UID, null);
        String savedRole = prefs.getString(KEY_ROLE, "entrant");

        if (hasAccount && uid != null && !uid.isEmpty()) {
            routeToRole(savedRole);
            finish();
            return true;
        }
        return false;
    }

    /** Find views, set phone mask & role dropdown, and wire button clicks. */
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

    /** Read inputs, check 10-digit phone (Toast on error), call SignUpService, then route on success. */
    private void onSignUp() {
        String email = safeText(Email);
        String name  = safeText(Name);
        String phoneFormatted = safeText(Phone);                   // e.g., "555 555 5555"
        String phoneDigits = phoneFormatted.replaceAll("\\D", ""); // "5555555555"
        String roleDisplay = roleDropdown.getText().toString().trim();

        // Missing role -> use Toast for consistency (optional; can keep setError if you prefer)
        if (TextUtils.isEmpty(roleDisplay)) {
            Toast.makeText(this, "Please select account type.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Phone must be exactly 10 digits; on error, show Toast (no setError/requestFocus)
        if (phoneDigits.length() != 10) {
            Toast.makeText(this, "Please enter a 10-digit phone number (e.g., 555 555 5555).", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpService.signUp(
                this, email, name, phoneDigits, roleDisplay,
                new SignUpService.Callback() {
                    /**
                     * Called when sign-up is successful.
                     *
                     * @param uid Firestore user document id
                     * @param canonicalRole normalized role ("entrant" | "organizer" | "admin")
                     */
                    @Override
                    public void onSuccess(String uid, String canonicalRole) {
                        // Persist minimal local state for future auto-skip
                        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean(KEY_HAS_ACCOUNT, true)
                                .putString(KEY_UID, uid)
                                .putString(KEY_ROLE, canonicalRole)
                                .apply();

                        Toast.makeText(MainActivity.this, "Sign up success", Toast.LENGTH_SHORT).show();
                        routeToRole(canonicalRole);
                        finish();
                    }

                    /**
                     * Called when sign-up fails.
                     *
                     * @param message short error message to show to the user
                     * @param e the exception; may be null
                     */
                    @Override
                    public void onError(String message, Exception e) {
                        Log.e("MainActivity", "Sign up failed", e);
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Start the Activity for the given role (no deep link extras).
     *
     * @param canonicalRole "entrant" | "organizer" | "admin"
     */
    private void routeToRole(String canonicalRole) {
        Intent intent;
        switch (canonicalRole) {
            case "organizer":
                intent = new Intent(this, OrganizerActivity.class); break;
            case "admin":
                intent = new Intent(this, AdminActivity.class); break;
            default:
                intent = new Intent(this, EntrantActivity.class);
        }
        startActivity(intent);
    }

    /** Close app safely. Assist with Chatgpt 2025/11/2 */
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

    /**
     * Trim EditText text; never returns null.
     *
     * @param et the input field
     * @return trimmed text (empty string if null)
     */
    private static String safeText(EditText et) {
        CharSequence cs = et.getText();
        return cs == null ? "" : cs.toString().trim();
    }
}
