package com.example.lotteryapp.organizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * # OrganizerAccountInfoFragment
 * Purpose:
 * - Display the signed-in organizer's account details (email, name, phone) in read-only fields.
 * - Allow the user to delete their account:
 *   * Deletes the Firestore document at `user/{uid}`.
 *   * Clears local SharedPreferences used for auto-login.
 *   * Navigates back to the sign-up screen with a cleared back stack.
 *   Author: Xindi Li
 */
public class OrganizerAccountInfoFragment extends Fragment {

    // Local cache file/keys must match MainActivity / SignUpService.
    private static final String PREF_FILE = "loginInfo";
    private static final String KEY_UID   = "UID";

    private TextInputEditText etEmail, etName, etPhone;
    private FirebaseFirestore db;

    /**
     * Inflate the fragment layout.
     *
     * @param inflater           LayoutInflater from the host activity
     * @param container          parent ViewGroup that the fragment's UI should be attached to
     * @param savedInstanceState previously saved state, or {@code null} if none
     * @return the root {@link View} for this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_account_info, container, false);
    }

    /**
     * Bind views, read UID from SharedPreferences, load profile from Firestore,
     * and wire the delete button with a confirmation dialog.
     *
     * @param v                  root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved state, or {@code null} if none
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // --- Bind UI ---
        etEmail = v.findViewById(R.id.ai_email);
        etName  = v.findViewById(R.id.ai_name);
        etPhone = v.findViewById(R.id.ai_phone);
        MaterialButton btnDelete = v.findViewById(R.id.ai_btn_delete);

        // Make fields read-only for a pure "info" screen.
        etEmail.setEnabled(false);
        etName.setEnabled(false);
        etPhone.setEnabled(false);

        db = FirebaseFirestore.getInstance();

        // --- Read UID from local prefs; if missing, go back to sign-up ---
        SharedPreferences sp = requireContext().getSharedPreferences(PREF_FILE, 0);
        String uid = sp.getString(KEY_UID, null);
        if (uid == null || uid.isEmpty()) {
            goToSignup();
            return;
        }

        // --- Load profile and render ---
        db.collection("user").document(uid).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        // If remote document was removed, clear local state and return to sign-up.
                        clearPrefsAndGoHome();
                        return;
                    }
                    UserProfile up = snap.toObject(UserProfile.class);
                    if (up != null) {
                        etEmail.setText(up.email);
                        etName.setText(up.name);
                        etPhone.setText(up.phone_num);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Load failed.", Toast.LENGTH_SHORT).show());

        // --- Delete account flow ---
        btnDelete.setOnClickListener(view ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Warning")
                        .setMessage("Pressing \"Yes\" will delete your account. Continue?")
                        .setPositiveButton("Yes", (d, w) -> {
                            db.collection("user").document(uid).delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(requireContext(), "Account deleted.", Toast.LENGTH_SHORT).show();
                                        clearPrefsAndGoHome();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(requireContext(), "Delete failed.", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    /**
     * Clear all local login state, then navigate to the sign-up screen (MainActivity)
     * with a cleared back stack.
     */
    private void clearPrefsAndGoHome() {
        requireContext().getSharedPreferences(PREF_FILE, 0).edit().clear().apply();
        goToSignup();
    }

    /**
     * Start {@link MainActivity} and clear the history so the user cannot
     * navigate back to this fragment.
     */
    private void goToSignup() {
        Intent it = new Intent(requireContext(), MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        requireActivity().finish();
    }

}
