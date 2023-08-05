package io.github.fastaes.test;


import android.util.Log;

import java.util.Arrays;
import java.util.Random;

import io.github.fastaes.FastAES;


public class AESTest {
    private static final String TAG = "MyTag";

    private static final Random r = RandomUtil.random;

    public static boolean test() {
        if (checkAES(128) && checkAES(256)) {
            Log.d(TAG, "Test AES success");
            return true;
        } else {
            Log.d(TAG, "Test AES failed");
            return false;
        }
    }

    private static boolean checkAES(int bits) {
        final int n = 1000;

        int keyLen = (bits == 128) ? 16 : 32;
        byte[] key = new byte[keyLen];
        r.nextBytes(key);
        for (int i = 0; i < n; i++) {
            int len = r.nextInt(1000);
            byte[] bytes = new byte[len];
            byte[] iv = new byte[16];
            r.nextBytes(bytes);
            r.nextBytes(iv);
            byte[] cipherBytes = FastAES.encrypt(bytes, key, iv);

            if (!Arrays.equals(bytes, FastAES.decrypt(cipherBytes, key, iv))) {
                return false;
            }

            if (!Arrays.equals(cipherBytes, SDK_AES.encrypt(bytes, key, iv))) {
                return false;
            }
        }
        return true;
    }
}
