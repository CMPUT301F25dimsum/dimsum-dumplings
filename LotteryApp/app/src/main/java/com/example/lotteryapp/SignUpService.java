package com.example.lotteryapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * SignUpService
 * Purpose:
 *  - Validate inputs, upsert Firestore user at path user/{uid}, and cache local login state.
 *  - Uses ANDROID_ID only as the Firestore document ID and local UID cache.
 * Firestore fields written: acc_created, acc_type, email, ip, name, phone_num
 * Local prefs written: UID, Role, enableOrganizerNotif=false, enableAdminNotif=false, hasAccount=true
 */
public class SignUpService {

    /**
     * Callback for async result of {@link #signUp(Context, String, String, String, String, Callback)}.
     */
    public interface Callback {
        /**
         * Sign-up success.
         *
         * @param uid the Firestore document id (ANDROID_ID)
         * @param canonicalRole normalized role string: "entrant" | "organizer" | "admin"
         */
        void onSuccess(String uid, String canonicalRole);

        /**
         * Sign-up failed.
         *
         * @param message short user-facing error message
         * @param e the underlying exception (may be null)
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

    private final FirebaseFirestore db;

    /**
     * Create a SignUpService bound to a specific {@link FirebaseFirestore} instance.
     *
     * @param db Firestore instance to use
     */
    public SignUpService(@NonNull FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Create/merge a user profile bound to this installation.
     *
     * @param ctx         context used for prefs and to read ANDROID_ID
     * @param email       user email (must match {@link Patterns#EMAIL_ADDRESS})
     * @param name        display name (non-empty)
     * @param phone       phone string; 10 digits expected (digits-only recommended)
     * @param roleDisplay role as shown to user: "Entrant" | "Organizer" | "Admin"
     * @param cb          callback for async result
     */
    public void signUp(Context ctx,
                       String email,
                       String name,
                       String phone,
                       String roleDisplay,
                       @NonNull Callback cb) {

        String err = validate(email, name, phone, roleDisplay);
        if (err != null) { cb.onError(err, null); return; }

        // Use ANDROID_ID as per-install ID (only as document ID, not stored as a field)
        String uid = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        String role = normalizeRole(roleDisplay); // "entrant" | "organizer" | "admin"

        // Minimal Firestore doc
        Map<String, Object> doc = new HashMap<>();
        doc.put("acc_created", FieldValue.serverTimestamp());
        doc.put("acc_type", role);
        doc.put("email", email);
        doc.put("ip", "");
        doc.put("name", name);
        doc.put("phone_num", phone);

        db.collection("user").document(uid)
                .set(doc, SetOptions.merge())
                .addOnSuccessListener(unused -> {
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

    // ---------- helpers ----------

    /**
     * Validate user inputs for sign-up.
     *
     * @param email email to check
     * @param name display name to check
     * @param phone phone input (may include spaces or symbols)
     * @param roleDisplay role string as displayed to user
     * @return null if valid; otherwise a short error message
     */
    private String validate(String email, String name, String phone, String roleDisplay) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Invalid email";
        if (TextUtils.isEmpty(name))
            return "Name required";
        // here we expect digits-only "5555555555"; 10 digits enforced by UI before calling signUp
        if (!TextUtils.isEmpty(phone) && phone.replaceAll("\\D", "").length() != 10)
            return "Phone must be 10 digits";
        if (TextUtils.isEmpty(roleDisplay))
            return "Account type required";
        return null;
    }

    /**
     * Normalize a display role to its canonical value.
     *
     * @param display "Entrant" | "Organizer" | "Admin" (case-sensitive)
     * @return "entrant" | "organizer" | "admin"
     */
    private String normalizeRole(String display) {
        switch (display) {
            case "Organizer": return "organizer";
            case "Admin":     return "admin";
            default:          return "entrant";
        }
    }

    /**
     * Attach a simple US/CA 10-digit phone mask ("555 555 5555") to the given EditText.
     *
     * @param editText the phone input field
     */
    public static void attachUsPhoneMask(EditText editText) {
        editText.addTextChangedListener(new UsPhoneMaskTextWatcher(editText));
    }

    /**
     * TextWatcher that formats the input as "555 555 5555" while typing.
     * Assist with Chatgpt 2025/10/31
     */
    public static class UsPhoneMaskTextWatcher implements TextWatcher {
        private final EditText edit;
        private boolean selfChange;

        /**
         * Create a new mask-watcher for the given EditText.
         *
         * @param edit the target EditText
         */
        public UsPhoneMaskTextWatcher(EditText edit) {
            this.edit = edit;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        /**
         * After text changes, reformat as "3 3 4" pattern with spaces.
         *
         * @param s current editable string
         */
        @Override
        public void afterTextChanged(Editable s) {
            if (selfChange) return;
            selfChange = true;

            // Keep only digits, clamp to 10
            String digits = s.toString().replaceAll("\\D", "");
            if (digits.length() > 10) digits = digits.substring(0, 10);

            // Format as 3 3 4 with spaces
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                out.append(digits.charAt(i));
                if (i == 2 || i == 5) out.append(' ');
            }

            // Replace entire text and move cursor to end
            s.replace(0, s.length(), out.toString());
            edit.setSelection(s.length());

            selfChange = false;
        }
    }
}