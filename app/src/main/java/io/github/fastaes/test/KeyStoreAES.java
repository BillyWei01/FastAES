package io.github.fastaes.test;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


/*
 * Use AndroidKeyStore to encrypt/decrypt is too slow,
 * It's not suggest to use AndroidKeyStore to encrypt every key-value.
 * Instead, use AndroidKeyStore to generate a symmetric key is a good choice.
 */

public class KeyStoreAES {
    private static final String ALIAS = "KeyStoreCipher";
    private static final int IV_SIZE = 16;

    private static volatile KeyStoreAES INSTANCE = null;

    private final byte[] buffer = new byte[16];
    private final SecretKey secretKey;
    private final Cipher cipher;
    private boolean noCipher;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private KeyStoreAES() {
        secretKey = getKeyFromKeyStore();
        cipher = getCipher();
        noCipher = cipher == null || secretKey == null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static synchronized KeyStoreAES getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeyStoreAES();
        }
        return INSTANCE;
    }

    private Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (Exception e) {
            logError(e);
        }
        return null;
    }

    public byte[] encrypt(@NonNull byte[] src) {
        if (noCipher || cipher == null) {
            return src;
        }

        try {
            byte[] cipherText;
            byte[] iv;
            synchronized (KeyStoreAES.class) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipherText = cipher.doFinal(src);
                iv = cipher.getIV();
                if (iv.length != IV_SIZE) {
                    noCipher = true;
                    return src;
                }
            }
            byte[] result = new byte[IV_SIZE + cipherText.length];
            System.arraycopy(iv, 0, result, 0, IV_SIZE);
            System.arraycopy(cipherText, 0, result, IV_SIZE, cipherText.length);
            return result;
        } catch (Exception e) {
            logError(e);
            noCipher = true;
        }
        return src;
    }

    public byte[] decrypt(@NonNull byte[] dst) {
        if (noCipher || cipher == null || dst.length < IV_SIZE) {
            return dst;
        }
        try {
            synchronized (KeyStoreAES.class) {
                System.arraycopy(dst, 0, buffer, 0, IV_SIZE);
                IvParameterSpec ivSpec = new IvParameterSpec(buffer);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
                return cipher.doFinal(dst, IV_SIZE, dst.length - IV_SIZE);
            }
        } catch (Exception e) {
            logError(e);
            noCipher = true;
        }
        return dst;
    }

    private void logError(Exception e) {
        Log.e("Cipher", e.getMessage(), e);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey getKeyFromKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias(ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                )
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        //.setUserAuthenticationRequired(false)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build();
                keyGenerator.init(spec);
                return keyGenerator.generateKey();
            } else {
                return ((KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null)).getSecretKey();
            }
        } catch (Exception e) {
            logError(e);
        }
        return null;
    }
}
