package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class AdminSettings {

    public static final String DEFAULT_PIN = "1019";

    private static final String PREFS = "admin_prefs";
    private static final String KEY_PIN = "pin";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_PIN_SALT = "pin_salt";
    private static final String KEY_LOCKOUT_UNTIL = "pin_lockout_until";
    private static final String KEY_ATTEMPTS = "pin_attempts";

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private AdminSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getPin(Context context) {
        String pin = prefs(context).getString(KEY_PIN, "");
        return pin.isEmpty() ? DEFAULT_PIN : pin;
    }

    public static void setPin(Context context, String pin) {
        if (pin == null || pin.isEmpty()) {
            return;
        }
        byte[] salt = newSalt();
        byte[] hash = pbkdf2(pin, salt);
        if (hash == null) {
            return;
        }
        prefs(context).edit()
                .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .remove(KEY_PIN)
                .apply();
    }

    public static boolean verifyPin(Context context, CharSequence entered) {
        if (entered == null) {
            return false;
        }
        SharedPreferences prefs = prefs(context);
        String storedSalt = prefs.getString(KEY_PIN_SALT, null);
        String storedHash = prefs.getString(KEY_PIN_HASH, null);

        if (storedSalt == null || storedHash == null) {
            String legacy = prefs.getString(KEY_PIN, "");
            String expected = legacy.isEmpty() ? DEFAULT_PIN : legacy;
            boolean match = constantTimeEquals(expected, entered.toString());
            if (match && !legacy.isEmpty()) {
                setPin(context, legacy);
            }
            return match;
        }

        byte[] salt = Base64.decode(storedSalt, Base64.NO_WRAP);
        byte[] expectedHash = Base64.decode(storedHash, Base64.NO_WRAP);
        byte[] actualHash = pbkdf2(entered.toString(), salt);
        if (actualHash == null) {
            return false;
        }
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    public static long getLockoutUntil(Context context) {
        return prefs(context).getLong(KEY_LOCKOUT_UNTIL, 0);
    }

    public static void setLockoutUntil(Context context, long until) {
        prefs(context).edit().putLong(KEY_LOCKOUT_UNTIL, until).apply();
    }

    public static int getAttempts(Context context) {
        return prefs(context).getInt(KEY_ATTEMPTS, 0);
    }

    public static int incrementAttempts(Context context) {
        int next = getAttempts(context) + 1;
        prefs(context).edit().putInt(KEY_ATTEMPTS, next).apply();
        return next;
    }

    public static void resetAttempts(Context context) {
        prefs(context).edit().putInt(KEY_ATTEMPTS, 0).apply();
    }

    private static byte[] newSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] pbkdf2(String pin, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] ba = a.getBytes();
        byte[] bb = b.getBytes();
        return MessageDigest.isEqual(ba, bb);
    }
}
