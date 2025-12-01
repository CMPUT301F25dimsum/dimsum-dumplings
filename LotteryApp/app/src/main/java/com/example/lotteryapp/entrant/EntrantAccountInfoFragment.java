package com.example.lotteryapp.entrant;

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
 * EntrantAccountInfoFragment
 *
 * Description:
 *  Fragment that displays the signed-in entrant's account information
 *  (email, name, phone) in read-only fields and allows the user to delete
 *  their account.
 *
 * Responsibilities:
 *  - Read the signed-in user's UID from SharedPreferences.
 *  - Load the user profile from Firestore using that UID.
 *  - Render email, name, and phone number as read-only text fields.
 *  - Provide a delete-account button with a confirmation dialog.
 *  - On delete:
 *      * Remove the Firestore document at user/{uid}.
 *      * Clear local SharedPreferences used for auto-login.
 *      * Navigate back to the sign-up screen (MainActivity) with a cleared back stack.
 *
 * Author: Xindi Li
 */
public class EntrantAccountInfoFragment extends Fragment {

    /** SharedPreferences file name used to cache login information. */
    private static final String PREF_FILE = "loginInfo";
    /** Key under which the signed-in user's UID is stored in SharedPreferences. */
    private static final String KEY_UID   = "UID";

    private TextInputEditText etEmail, etName, etPhone;
    private FirebaseFirestore db;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  the {@link LayoutInflater} from the hosting activity
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState previously saved state, or {@code null} if none
     * @return the root {@link View} representing this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_account_info, container, false);
    }

    /**
     * Called after the fragment's view hierarchy has been created.
     * @param v the root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
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
     * Clears all locally cached login state and then redirects the user
     * back to the sign-up screen.
     * <p>
     * This is called after successfully deleting the Firestore user document
     * or when the local user state becomes invalid.
     */
    private void clearPrefsAndGoHome() {
        requireContext().getSharedPreferences(PREF_FILE, 0)
                .edit()
                .clear()
                .apply();
        goToSignup();
    }

    /**
     * Starts {@link MainActivity} as the sign-up / entry screen and clears the
     * current task's back stack so the user cannot navigate back to this fragment.
     */
    private void goToSignup() {
        Intent it = new Intent(requireContext(), MainActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        requireActivity().finish();
    }

}
