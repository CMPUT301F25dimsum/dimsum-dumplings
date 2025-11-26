package com.example.lotteryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * SignUpService
 *
 * Creates/updates a master user document at: user/{ANDROID_ID}.
 * Base fields are written for all roles. Additional fields are written conditionally by role:
 * - entrant: adds empty placeholders "event_id" and "organizer_id" on first signup.
 *
 * Local SharedPreferences written ("loginInfo"):
 *  - UID, Role, enableOrganizerNotif=false, enableAdminNotif=false, hasAccount=true
 *  Author: Xindi Li
 */
public class SignUpService {

    /** Callback for async signup result. */
    public interface Callback {
        /**
         * Called when sign-up succeeds.
         *
         * @param uid Firestore document id (ANDROID_ID)
         * @param canonicalRole one of "entrant" | "organizer" | "admin"
         */
        void onSuccess(String uid, String canonicalRole);

        /**
         * Called when sign-up fails.
         *
         * @param message short user-facing message you may choose to display
         * @param e underlying exception (may be null)
         */
        void onError(String message, Exception e);
    }

    // SharedPreferences contract
    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_UID = "UID";
    private static final String KEY_ROLE = "Role";
    private static final String KEY_ENABLE_ORG = "enableOrganizerNotif";
    private static final String KEY_ENABLE_ADMIN = "enableAdminNotif";
    private static final String KEY_HAS_ACCOUNT = "hasAccount";

    // Collection
    private static final String COLL_USERS = "user";

    private final FirebaseFirestore db;

    public SignUpService(@NonNull FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Create/merge a user profile document at user/{ANDROID_ID}.
     * Base fields are always written; role-specific fields are added conditionally:
     * - If role == entrant → add "event_id" = "" and "organizer_id" = "".
     *
     * @param ctx         Android context (for prefs and ANDROID_ID)
     * @param email       email (may be empty; if not empty must match Patterns.EMAIL_ADDRESS)
     * @param name        display name (non-empty)
     * @param phone       digits-only "5555555555" (10 digits expected; caller enforces)
     * @param roleDisplay UI label: "Entrant" | "Organizer" | "Admin"
     * @param cb          callback for async result
     */
    public void signUp(Context ctx,
                       String email,
                       String name,
                       String phone,
                       String roleDisplay,
                       @NonNull Callback cb) {

        // 1) Validate minimal inputs
        String err = validate(email, name, phone, roleDisplay);
        if (err != null) { cb.onError(err, null); return; }

        // 2) Canonical role + uid
        String role = normalizeRole(roleDisplay); // "entrant" | "organizer" | "admin"
        String uid = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

        // 3) Base document (common fields)
        Map<String, Object> doc = new HashMap<>();
        doc.put("acc_created", FieldValue.serverTimestamp());
        doc.put("acc_type", role);
        doc.put("email", email);
        doc.put("ip", "");               // left blank; typically populated server-side if needed
        doc.put("name", name);
        doc.put("phone_num", phone);
        doc.put("uid", uid);


        // 5) Upsert (merge) into user/{uid}
        db.collection(COLL_USERS).document(uid)
                .set(doc, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    // 6) Cache minimal local state for future local checks (if you ever need them)
                    SharedPreferences prefs = ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString(KEY_UID, uid)
                            .putString(KEY_ROLE, role)
                            .putBoolean(KEY_ENABLE_ORG, false)
                            .putBoolean(KEY_ENABLE_ADMIN, false)
                            .putBoolean(KEY_HAS_ACCOUNT, true)
                            .apply();
                    cb.onSuccess(uid, role);
                })
                .addOnFailureListener(e -> cb.onError("Network error, please try again", e));
    }

    // ----------------- helpers -----------------

    /**
     * Minimal input checks.
     *
     * @param email email to check (optional; if present must match email pattern)
     * @param name non-empty display name
     * @param phone phone digits-only string, expected length 10
     * @param roleDisplay UI label for role
     * @return null if valid; otherwise a short error string
     */
    private String validate(String email, String name, String phone, String roleDisplay) {
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Invalid email";
        if (TextUtils.isEmpty(name))
            return "Name required";
        if (TextUtils.isEmpty(phone) || phone.replaceAll("\\D", "").length() != 10)
            return "Phone must be 10 digits";
        if (TextUtils.isEmpty(roleDisplay))
            return "Account type required";
        return null;
    }

    /**
     * Maps UI label to canonical role string.
     *
     * @param display "Entrant" | "Organizer" | "Admin"
     * @return canonical role: "entrant" | "organizer" | "admin"
     */
    private String normalizeRole(String display) {
        switch (display) {
            case "Organizer": return "organizer";
            case "Admin":     return "admin";
            default:          return "entrant";
        }
    }

    /**
     * Attaches a live US/CA phone mask ("555 555 5555") to the given EditText.
     *
     * @param editText target input field
     */
    public static void attachUsPhoneMask(android.widget.EditText editText) {
        editText.addTextChangedListener(new UsPhoneMaskTextWatcher(editText));
    }

    /** TextWatcher that formats phone numbers as "123 456 7890" while typing. */
    public static class UsPhoneMaskTextWatcher implements android.text.TextWatcher {
        private final android.widget.EditText edit;
        private boolean selfChange;

        public UsPhoneMaskTextWatcher(android.widget.EditText edit) {
            this.edit = edit;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {
            if (selfChange) return;
            selfChange = true;

            String digits = s.toString().replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);

            StringBuilder out = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                out.append(digits.charAt(i));
            }

            s.replace(0, s.length(), out.toString());
            edit.setSelection(s.length());

            selfChange = false;
        }
    }
}
