package com.example.lotteryapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation tests for SignUpService that do NOT require any extra Gradle deps.
 * Strategy:
 *  - Drive the public signUp(...) with invalid inputs to verify onError(...) is called
 *    (validation happens before any Firestore call, so no network/mock needed).
 *  - Verify phone mask formatting via attachUsPhoneMask(...) + EditText.
 *  Assist with Chatgpt 2025/11/2
 */
@RunWith(AndroidJUnit4.class)
public class SignUpServiceTest {

    // --------- helpers ---------
    private Context ctx() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private static class CapturingCb implements SignUpService.Callback {
        String okUid;
        String okRole;
        String errMsg;
        Exception errEx;

        @Override public void onSuccess(String uid, String canonicalRole) {
            this.okUid = uid;
            this.okRole = canonicalRole;
        }
        @Override public void onError(String message, Exception e) {
            this.errMsg = message;
            this.errEx = e;
        }
    }
    // ---------------------------

    /**
     * signUp: invalid email -> onError("Invalid email"), no success.
     */
    @Test
    public void signUp_invalidEmail_triggersOnError() {
        // FirebaseFirestore instance is not needed for validation-fail path; pass null-safe wrapper.
        SignUpService svc = new SignUpService(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        CapturingCb cb = new CapturingCb();

        svc.signUp(ctx(), "bad@", "Alice", "5555555555", "Entrant", cb);

        assertEquals("Invalid email", cb.errMsg);
        assertNull(cb.okUid);
        assertNull(cb.okRole);
    }

    /**
     * signUp: missing name -> onError("Name required").
     */
    @Test
    public void signUp_missingName_triggersOnError() {
        SignUpService svc = new SignUpService(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        CapturingCb cb = new CapturingCb();

        svc.signUp(ctx(), "a@b.com", "", "5555555555", "Entrant", cb);

        assertEquals("Name required", cb.errMsg);
        assertNull(cb.okUid);
    }

    /**
     * signUp: wrong phone length -> onError("Phone must be 10 digits").
     */
    @Test
    public void signUp_wrongPhone_triggersOnError() {
        SignUpService svc = new SignUpService(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        CapturingCb cb = new CapturingCb();

        svc.signUp(ctx(), "a@b.com", "Alice", "12345", "Entrant", cb);

        assertEquals("Phone must be 10 digits", cb.errMsg);
        assertNull(cb.okUid);
    }

    /**
     * signUp: missing role -> onError("Account type required").
     */
    @Test
    public void signUp_missingRole_triggersOnError() {
        SignUpService svc = new SignUpService(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        CapturingCb cb = new CapturingCb();

        svc.signUp(ctx(), "a@b.com", "Alice", "5555555555", "", cb);

        assertEquals("Account type required", cb.errMsg);
        assertNull(cb.okUid);
    }

    /**
     * Phone mask: formats 10 digits to "123 456 7890".
     */
    @Test
    public void phoneMask_formatsTo_123_456_7890() {
        Context ctx = ctx();
        EditText et = new EditText(ctx);
        SignUpService.attachUsPhoneMask(et);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> et.setText("1234567890"));

        assertEquals("123 456 7890", et.getText().toString());
    }

    /**
     * Phone mask: clamps input longer than 10 digits.
     */
    @Test
    public void phoneMask_clampsTo10Digits() {
        Context ctx = ctx();
        EditText et = new EditText(ctx);
        SignUpService.attachUsPhoneMask(et);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> et.setText("123456789012345"));

        assertEquals("123 456 7890", et.getText().toString());
    }
}
