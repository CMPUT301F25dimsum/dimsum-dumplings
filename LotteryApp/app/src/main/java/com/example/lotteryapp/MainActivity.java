package com.example.lotteryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

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
 * Main entry screen for LotteryApp.
 *   On launch, checks Firestore (SERVER) for user/{ANDROID_ID}. If the doc exists,
 *       reads "acc_type" and routes to the role screen. Otherwise, shows the signup form.</li>
 *   On sign up, performs minimal silent validation (no toasts), creates/merges the user via
 *       {@link SignUpService}, caches minimal local state, and routes.</li>
 *
 *
 * Testing(assist with Chatgpt 2025/11/4:
 *   {@code TEST_BYPASS_SERVER=true}: skip server check and always show the form.</li>
 *  {@code TEST_FORCE_LOCAL=true}: skip server check and auto-route using local SharedPreferences.</li>
 *
 */
public class MainActivity extends AppCompatActivity {

    // Optional local cache (not trusted for auto-skip; server is source of truth)
    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_UID = "UID";
    private static final String KEY_ROLE = "Role";
    private static final String KEY_HAS_ACCOUNT = "hasAccount";

    // Test-only intent extras (do not set these in production)
    private static final String EXTRA_TEST_BYPASS_SERVER = "TEST_BYPASS_SERVER";
    private static final String EXTRA_TEST_FORCE_LOCAL = "TEST_FORCE_LOCAL";

    private EditText Email, Name, Phone;
    private MaterialAutoCompleteTextView roleDropdown;
    private Button btnSignup, btnCancel;

    private FirebaseFirestore db;
    private SignUpService signUpService;

    /**
     * Android activity entry point.
     *
     * @param savedInstanceState previous state bundle if recreating, otherwise {@code null}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        signUpService = new SignUpService(db);

        // ---- Test hooks (no effect in production unless extras are explicitly set) ----
        Intent it = getIntent();
        boolean TEST_BYPASS_SERVER = it.getBooleanExtra(EXTRA_TEST_BYPASS_SERVER, false);
        boolean TEST_FORCE_LOCAL   = it.getBooleanExtra(EXTRA_TEST_FORCE_LOCAL, false);
        if (TEST_BYPASS_SERVER) {            // For UI tests that need form immediately
            bindViewsAndWireUp();
            return;
        }
        if (TEST_FORCE_LOCAL && tryLocalAutoSkipOnce()) { // For tests simulating local auto-skip
            finish();
            return;
        }
        // -----------------------------------------------------------------------------

        // Production behavior: server check first → route if exists; otherwise show form
        tryAutoLoginByServerOrShowForm();
    }

    /** If Firestore has user/{ANDROID_ID}, route to its role; else show the signup form (silent). */
    private void tryAutoLoginByServerOrShowForm() {
        String uid = getAndroidId();
        db.collection("user").document(uid)
                .get(Source.SERVER) // force server to avoid stale local cache
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        String role = snap.getString("acc_type");
                        if (TextUtils.isEmpty(role)) role = "entrant";
                        // Cache locally for convenience (not required for server auto-skip)
                        SharedPreferences p = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                        p.edit()
                                .putBoolean(KEY_HAS_ACCOUNT, true)
                                .putString(KEY_UID, uid)
                                .putString(KEY_ROLE, role)
                                .apply();
                        routeToRole(role);
                        finish();
                    } else {
                        bindViewsAndWireUp();
                    }
                })
                .addOnFailureListener(e -> {
                    // Silent fallback: show form if network fails
                    bindViewsAndWireUp();
                });
    }

    /**
     * Test-only helper: attempt local auto-skip using SharedPreferences (no server).
     *
     * @return {@code true} if local state is present and routing occurred; {@code false} otherwise
     */
    private boolean tryLocalAutoSkipOnce() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        boolean has = prefs.getBoolean(KEY_HAS_ACCOUNT, false);
        String uid  = prefs.getString(KEY_UID, null);
        String role = prefs.getString(KEY_ROLE, "entrant");
        if (has && uid != null && !uid.isEmpty()) {
            routeToRole(role);
            return true;
        }
        return false;
    }

    /** Bind the signup form views, set phone mask and dropdown, and wire click handlers (silent). */
    private void bindViewsAndWireUp() {
        Email = findViewById(R.id.etEmail);
        Name  = findViewById(R.id.etName);
        Phone = findViewById(R.id.etPhone);
        roleDropdown = findViewById(R.id.main_role_dropdown);
        btnSignup = findViewById(R.id.main_signup_button);
        btnCancel = findViewById(R.id.main_cancel_button);

        // Phone mask: formats as "555 555 5555" while typing
        SignUpService.attachUsPhoneMask(Phone);

        // Role dropdown
        String[] roles = {"Entrant", "Organizer", "Admin"};
        roleDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles));

        btnSignup.setOnClickListener(v -> onSignUp());
        btnCancel.setOnClickListener(v -> closeApp());
    }

    /** Reads inputs, performs minimal silent validation, invokes SignUpService, and routes on success. */
    private void onSignUp() {
        String email = safeText(Email);
        String name  = safeText(Name);
        String phoneDigits = safeText(Phone).replaceAll("\\D", "");
        String roleDisplay = roleDropdown.getText().toString().trim();

        // Minimal silent validation: if invalid, simply return (no error UI)
        if (TextUtils.isEmpty(roleDisplay)) return;
        if (phoneDigits.length() != 10) return;

        signUpService.signUp(
                this, email, name, phoneDigits, roleDisplay,
                new SignUpService.Callback() {
                    @Override public void onSuccess(String uid, String role) {
                        SharedPreferences p = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                        p.edit()
                                .putBoolean(KEY_HAS_ACCOUNT, true)
                                .putString(KEY_UID, uid)
                                .putString(KEY_ROLE, role)
                                .apply();
                        routeToRole(role);
                        finish();
                    }
                    @Override public void onError(String msg, Exception e) {
                        // Silent: no UI message
                    }
                }
        );
    }

    /**
     * Routes to the appropriate Activity based on canonical role.
     *
     * @param role one of: "entrant", "organizer", or "admin"
     */
    private void routeToRole(String role) {
        Intent intent;
        switch (role) {
            case "organizer": intent = new Intent(this, OrganizerActivity.class); break;
            case "admin":     intent = new Intent(this, AdminActivity.class);     break;
            default:          intent = new Intent(this, EntrantActivity.class);
        }
        startActivity(intent);
    }

    /**
     * Returns this installation's ANDROID_ID used as the Firestore document id.
     *
     * @return the ANDROID_ID string for this device/installation
     */
    private String getAndroidId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /** Closes the app/task with best-effort behavior across Android versions (silent). */
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
     * Safely returns trimmed text from an EditText.
     *
     * @param et the EditText to read from
     * @return the trimmed string content of the EditText (never {@code null})
     */
    private static String safeText(EditText et) {
        CharSequence cs = et.getText();
        return cs == null ? "" : cs.toString().trim();
        // Note: EditText#getText() may return null before first layout/attach; we normalize to "".
    }
}
