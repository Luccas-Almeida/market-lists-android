package com.LuccasAlmeida.marketlists.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SessionManager {

    private static final String PREF_NAME = "market_lists_secure_session";
    private static final String KEY_ALIAS = "MarketListsKeystoreKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private static final String PREF_ACCESS_TOKEN_ENCRYPTED = "enc_access_token";
    private static final String PREF_ACCESS_TOKEN_IV = "iv_access_token";
    private static final String PREF_REFRESH_TOKEN_ENCRYPTED = "enc_refresh_token";
    private static final String PREF_REFRESH_TOKEN_IV = "iv_refresh_token";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    private SecretKey getOrCreateSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            keyGenerator.init(spec);
            return keyGenerator.generateKey();
        }

        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        return entry.getSecretKey();
    }

    private String encryptData(String data, String keyEncryptedPref, String keyIvPref) {
        if (data == null || data.isEmpty()) return null;
        try {
            SecretKey secretKey = getOrCreateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            String encDataStr = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
            String ivStr = Base64.encodeToString(iv, Base64.NO_WRAP);

            prefs.edit()
                    .putString(keyEncryptedPref, encDataStr)
                    .putString(keyIvPref, ivStr)
                    .apply();

            return encDataStr;
        } catch (Exception e) {
            Log.e("API_LISTA", "Erro ao criptografar token", e);
            clearSession();
            return null;
        }
    }

    private String decryptData(String keyEncryptedPref, String keyIvPref) {
        String encDataStr = prefs.getString(keyEncryptedPref, null);
        String ivStr = prefs.getString(keyIvPref, null);

        if (encDataStr == null || ivStr == null) return null;

        try {
            byte[] encryptedBytes = Base64.decode(encDataStr, Base64.NO_WRAP);
            byte[] iv = Base64.decode(ivStr, Base64.NO_WRAP);

            SecretKey secretKey = getOrCreateSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e("API_LISTA", "Erro ao descriptografar token (chave indisponível/sessão inválida)", e);
            clearSession();
            return null;
        }
    }

    public void saveSession(String accessToken, String refreshToken) {
        encryptData(accessToken, PREF_ACCESS_TOKEN_ENCRYPTED, PREF_ACCESS_TOKEN_IV);
        if (refreshToken != null) {
            encryptData(refreshToken, PREF_REFRESH_TOKEN_ENCRYPTED, PREF_REFRESH_TOKEN_IV);
        }
    }

    public String getAccessToken() {
        return decryptData(PREF_ACCESS_TOKEN_ENCRYPTED, PREF_ACCESS_TOKEN_IV);
    }

    public String getRefreshToken() {
        return decryptData(PREF_REFRESH_TOKEN_ENCRYPTED, PREF_REFRESH_TOKEN_IV);
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}