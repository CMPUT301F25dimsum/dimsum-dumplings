package com.example.lotteryapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.example.lotteryapp.admin.AdminActivity;
import com.example.lotteryapp.entrant.EntrantActivity;
import com.example.lotteryapp.organizer.OrganizerActivity;
import com.example.lotteryapp.reusecomponent.Event;
import com.example.lotteryapp.reusecomponent.EventDisplayFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

/**
 * MainActivity
 *
 * Responsibilities:
 *  - On launch, attempt server-validated auto-skip: if local prefs have {hasAccount, UID, Role},
 *    fetch Firestore doc user/{UID} with Source.SERVER. If it exists, route to role screen; if it
 *    does not exist (deleted in Firestore), clear local prefs and show sign-up form.
 *  - Render the sign-up UI and perform client-side validation with inline TextInputLayout errors:
 *      * Email must be valid.
 *      * Name is required.
 *      * Phone must be exactly 10 digits (masked live as "555 555 5555" while typing).
 *      * Role must be selected.
 *  - On valid input, call SignUpService to upsert Firestore + cache local prefs; then route.
 *
 *  Issues: None
 *  Author: Xindi Li
 */
public class MainActivity extends AppCompatActivity {

    // -------- Local cache keys (SharedPreferences "loginInfo") --------
    private static final String PREF_FILE   = "loginInfo";
    private static final String KEY_UID     = "UID";
    private static final String KEY_ROLE    = "Role";
    private static final String KEY_HAS_ACC = "hasAccount";

    // -------- UI: TextInputLayouts for inline error messages --------
    private TextInputLayout tilEmail, tilName, tilPhone, tilRole;

    // -------- UI: Inputs --------
    private EditText Email, Name, Phone;
    private MaterialAutoCompleteTextView roleDropdown;

    // -------- UI: Buttons --------
    private Button btnSignup, btnCancel;

    // -------- Data layer --------
    private FirebaseFirestore db;
    private SignUpService signUpService;

    public static String deepEventId = null;
    public static String deepOrganizerId = null;

    // -------- Location --------
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Get the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        // Avoid drawing under system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Extract QR code deep link parameters (occurs when launched from QR code)
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            deepEventId = data.getQueryParameter("eid");
            deepOrganizerId = data.getQueryParameter("oid");
        }

        db = FirebaseFirestore.getInstance();
        signUpService = new SignUpService(db);

        // ★ Server-validated auto-skip (checks Firestore before routing)
        tryServerValidatedAutoSkipOrShowForm();
    }

    // check permissions, if not granted, request them
    // else get location
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted, get the location
            getLocation();
        }
    }

    // handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                getLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // check if permission granted and get location
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Logic to handle location object
                            SharedPreferences sharedPref = getSharedPreferences("userLocation", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putFloat("latitude", (float) location.getLatitude());
                            editor.putFloat("longitude", (float) location.getLongitude());
                            editor.apply();
                        }
                    });
        }
    }

    /**
     * Server-validated auto-skip:
     * If local prefs indicate an account, verify user/{UID} exists using Source.SERVER.
     *  - Exists  → route to saved role and finish.
     *  - Missing → clear prefs and show the sign-up form.
     * If no local account, show the sign-up form.
     */
    private void tryServerValidatedAutoSkipOrShowForm() {
        SharedPreferences p = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        boolean has = p.getBoolean(KEY_HAS_ACC, false);
        String uid  = p.getString(KEY_UID,  "");
        String role = p.getString(KEY_ROLE, "");

        if (has && !TextUtils.isEmpty(uid) && !TextUtils.isEmpty(role)) {
            db.collection("user").document(uid)
                    .get(Source.SERVER)
                    .addOnSuccessListener(snap -> {
                        if (snap.exists()) {
                            routeToRole(role);
                            finish();
                        } else {
                            // Cloud doc was deleted → local cache invalid
                            p.edit().clear().apply();
                            bindViewsAndWireUp();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Network error → conservative choice: show the form (you may choose to retry/notify)
                        bindViewsAndWireUp();
                    });
        } else {
            bindViewsAndWireUp();
        }
    }

    /**
     * Loads an event from firebase based on a passed organizer + event id
     * then displays it as a dialog fragment
     */
    public static void load_event(String eventId, String organizerId, FragmentManager manager){
        // Fetch event
        FirebaseFirestore.getInstance().collection("events")
                .document(organizerId)
                .collection("organizer_events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        new EventDisplayFragment(event)
                                .show((manager), "event_display");
                    }
                    deepOrganizerId = null;
                    deepEventId = null;
                });
    }

    /**
     * Bind views, attach phone mask & error-clearing, set dropdown and button listeners.
     */
    private void bindViewsAndWireUp() {
        // TextInputLayouts (for inline setError)
        tilEmail = findViewById(R.id.tilEmail);
        tilName  = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        tilRole  = findViewById(R.id.tilRole);

        // Inputs
        Email = findViewById(R.id.etEmail);
        Name  = findViewById(R.id.etName);
        Phone = findViewById(R.id.etPhone);

        // Phone mask "555 555 5555" as user types
        SignUpService.attachUsPhoneMask(Phone);

        // Clear error as user edits
        addErrorClearingWatcher(Email, tilEmail);
        addErrorClearingWatcher(Name,  tilName);
        addErrorClearingWatcher(Phone, tilPhone);

        // Role dropdown
        roleDropdown = findViewById(R.id.main_role_dropdown);
        String[] roles = {"Entrant", "Organizer", "Admin"};
        roleDropdown.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles));
        roleDropdown.setOnItemClickListener((p, v, pos, id) -> tilRole.setError(null));
        roleDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tilRole.setError(null);
        });

        // Buttons
        btnSignup = findViewById(R.id.main_signup_button);
        btnCancel = findViewById(R.id.main_cancel_button);

        btnSignup.setOnClickListener(v -> onSignUp());
        btnCancel.setOnClickListener(v -> closeApp());
    }

    /**
     * Validates inputs; on success calls SignUpService, on failure shows inline errors.
     */
    private void onSignUp() {
        if (!validateForm()) return;

        String email       = safeText(Email);
        String name        = safeText(Name);
        String phoneDigits = safeText(Phone).replaceAll("\\D", ""); // store digits-only
        String roleDisplay = roleDropdown.getText().toString().trim();

        signUpService.signUp(
                this, email, name, phoneDigits, roleDisplay,
                new SignUpService.Callback() {
                    @Override public void onSuccess(String uid, String canonicalRole) {
                        // Cache minimal local state for future auto-skip
                        SharedPreferences p = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                        p.edit()
                                .putBoolean(KEY_HAS_ACC, true)
                                .putString(KEY_UID, uid)
                                .putString(KEY_ROLE, canonicalRole)
                                .apply();

                        routeToRole(canonicalRole);
                        finish();
                    }

                    @Override public void onError(String message, Exception e) {
                        Toast.makeText(MainActivity.this,
                                TextUtils.isEmpty(message) ? "Network error. Try again." : message,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Checks each field and sets an inline error on failure.
     *
     * @return true if all inputs pass validation; false otherwise
     */
    private boolean validateForm() {
        boolean ok = true;

        String email       = safeText(Email);
        String name        = safeText(Name);
        String phoneDigits = safeText(Phone).replaceAll("\\D", "");
        String roleDisplay = roleDropdown.getText().toString().trim();

        // Email
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            ok = false;
        } else {
            tilEmail.setError(null);
        }

        // Name
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            ok = false;
        } else {
            tilName.setError(null);
        }

        // Phone: exactly 10 digits
        if (phoneDigits.length() != 10) {
            tilPhone.setError("Enter a 10-digit phone (e.g., 555 555 5555)");
            ok = false;
        } else {
            tilPhone.setError(null);
        }

        // Role
        if (TextUtils.isEmpty(roleDisplay)) {
            tilRole.setError("Select an account type");
            ok = false;
        } else {
            tilRole.setError(null);
        }

        return ok;
    }

    /**
     * Clears TextInputLayout error as the user edits the corresponding EditText.
     *
     * @param et  input field to observe
     * @param til associated TextInputLayout to clear errors on change
     */
    private void addErrorClearingWatcher(EditText et, TextInputLayout til) {
        et.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { til.setError(null); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /**
     * Launches the role-specific Activity.
     *
     * @param role canonical role: "entrant" | "organizer" | "admin"
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

    /** Closes the app/task safely. */
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
     * Returns trimmed text from an EditText, never null.
     *
     * @param et EditText to read
     * @return trimmed string (empty if null)
     */
    private static String safeText(EditText et) {
        CharSequence cs = et.getText();
        return cs == null ? "" : cs.toString().trim();
    }
}
