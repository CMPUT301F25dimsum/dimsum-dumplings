package com.example.lotteryapp.entrant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotteryapp.MainActivity;
import com.example.lotteryapp.R;
import com.example.lotteryapp.reusecomponent.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * EntrantUpdateAccountFragment
 *
 * Description:
 *  Fragment that allows an entrant to update their account information
 *  (email, name, phone number). Validates inputs and writes changes to Firestore.
 *
 * Responsibilities:
 *  - Load the current user's profile from Firestore.
 *  - Validate email format and phone length.
 *  - Update email separately or update name + phone together.
 *  - Redirect to the sign-up screen if no UID is stored locally.
 *
 * Author: Xindi Li
 */


public class EntrantUpdateAccountFragment extends Fragment {

    /** SharedPreferences file name used to cache login info. */
    private static final String PREF_FILE = "loginInfo";
    /** Key for storing the signed-in user's UID in SharedPreferences. */
    private static final String KEY_UID   = "UID";

    private TextInputEditText etEmail, etName, etPhone;
    private TextInputLayout tilEmail, tilName, tilPhone;

    private MaterialButton btnEmailConfirm, btnContactConfirm;

    private FirebaseFirestore db;
    private String uid;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  layout inflater provided by the hosting activity
     * @param container parent view that the fragment UI will be attached to
     * @param savedInstanceState previously saved state, or {@code null} if none
     * @return the root {@link View} for this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_update_account, container, false);
    }

    /**
     * Called after the fragment's view has been created.
     * <p>
     * Responsibilities:
     * <ul>
     *     <li>Bind UI fields and buttons.</li>
     *     <li>Load the current user's UID from SharedPreferences.</li>
     *     <li>Fetch user profile from Firestore and display it.</li>
     *     <li>Wire up button listeners for updating email and contact info.</li>
     * </ul>
     *
     * @param v the root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved state, or {@code null} if none
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // ========== Bind UI ==========
        tilEmail = v.findViewById(R.id.tilEmail);
        tilName  = v.findViewById(R.id.tilName);
        tilPhone = v.findViewById(R.id.tilPhone);

        etEmail = v.findViewById(R.id.ua_email);
        etName  = v.findViewById(R.id.ua_name);
        etPhone = v.findViewById(R.id.ua_phone);

        btnEmailConfirm   = v.findViewById(R.id.ua_btn_email_confirm);
        btnContactConfirm = v.findViewById(R.id.ua_btn_contact_confirm);

        db = FirebaseFirestore.getInstance();

        // ========== Load UID ==========
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_FILE, 0);
        uid = sp.getString(KEY_UID, null);
        if (uid == null || uid.isEmpty()) {
            goToSignup();
            return;
        }

        // ========== Load user info ==========
        db.collection("user").document(uid)
                .get()
                .addOnSuccessListener(snap -> {
                    UserProfile up = snap.toObject(UserProfile.class);
                    if (up != null) {
                        etEmail.setText(up.email);
                        etName.setText(up.name);
                        etPhone.setText(up.phone_num);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Load failed", Toast.LENGTH_SHORT).show()
                );

        // ========== Button: Email Update ==========
        btnEmailConfirm.setOnClickListener(view -> {
            clearErrors();

            String email = text(etEmail);

            if (TextUtils.isEmpty(email)) {
                tilEmail.setError("Email cannot be empty");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Invalid email format");
                return;
            }

            db.collection("user").document(uid)
                    .update("email", email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(requireContext(), "Email updated", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    );
        });

        // ========== Button: Contact Update (Name + Phone) ==========
        btnContactConfirm.setOnClickListener(view -> {
            clearErrors();

            String name  = text(etName);
            String phone = text(etPhone);

            boolean hasError = false;

            if (TextUtils.isEmpty(name)) {
                tilName.setError("Name cannot be empty");
                hasError = true;
            }

            if (TextUtils.isEmpty(phone)) {
                tilPhone.setError("Phone cannot be empty");
                hasError = true;
            } else if (!phone.matches("\\d{10}")) {
                tilPhone.setError("Phone must be exactly 10 digits");
                hasError = true;
            }

            if (hasError) return;

            db.collection("user").document(uid)
                    .update("name", name, "phone_num", phone)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(requireContext(), "Contact info updated", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    /**
     * Safely reads and trims the text content of a {@link TextInputEditText}.
     *
     * @param et the text field to read from; may be empty but not {@code null}
     * @return a trimmed {@link String}, or an empty string if the field has no text
     */
    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * Clears any validation errors currently shown on the email, name, and phone fields.
     * <p>
     * This is typically called before running new validation.
     */
    private void clearErrors() {
        tilEmail.setError(null);
        tilName.setError(null);
        tilPhone.setError(null);
    }

    /**
     * Navigates back to the sign-up screen and clears the current back stack.
     * <p>
     * This is used when no UID is found in SharedPreferences, or when
     * the account state is invalid and the user must sign in again.
     */
    private void goToSignup() {
        Intent it = new Intent(requireContext(), MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        requireActivity().finish();
    }
}