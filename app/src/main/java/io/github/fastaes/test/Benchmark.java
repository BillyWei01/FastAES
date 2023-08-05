package io.github.fastaes.test;

import android.os.Build;

import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;

import io.github.fastaes.FastAES;

public class Benchmark {
    public interface Callback {
        void onResult(String result);
    }

    public static void start(Callback callback) throws BadPaddingException {
        Random r = new Random();
        int n = 500;

        ArrayList<byte[]> testData = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int len = r.nextInt(100);
            byte[] bytes = new byte[len];
            r.nextBytes(bytes);
            testData.add(bytes);
        }

        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        r.nextBytes(iv);
        r.nextBytes(key);

        long t1 = System.nanoTime();
        for (byte[] data : testData) {
            byte[] cipher = FastAES.encrypt(data, key, iv);
            byte[] plain = FastAES.decrypt(cipher, key, iv);
        }
        long t2 = System.nanoTime();

        callback.onResult("FastAES: " + getTime(t2, t1) + " ms\n");

        for (byte[] data : testData) {
            byte[] cipher = SDK_AES.encrypt(data, key, iv);
            byte[] plain = SDK_AES.decrypt(cipher, key, iv);
        }
        long t3 = System.nanoTime();

        callback.onResult("SDK AES: " + getTime(t3, t2) + " ms\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyStoreAES keyStoreAES = KeyStoreAES.getInstance();
            for (byte[] data : testData) {
                byte[] cipher = keyStoreAES.encrypt(data);
                byte[] plain = keyStoreAES.decrypt(cipher);
//                if(!Arrays.equals(data ,plain)){
//                    callback.onResult("Key store encrypt failed");
//                    break;
//                }
            }
            long t4 = System.nanoTime();
            callback.onResult("KeyStore AES: " + getTime(t4, t3) + " ms\n");
        }
    }

    private static long getTime(long end, long start) {
        return (end - start) / 1000000L;
    }
}
