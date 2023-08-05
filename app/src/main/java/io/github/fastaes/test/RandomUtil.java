package io.github.fastaes.test;

import android.util.Log;

import java.util.Random;

public class RandomUtil {
    private static final long seed = System.nanoTime() ^ System.currentTimeMillis();
    public static final Random random = new Random(seed);
    static {
        Log.d("MyTag", "seed:" + seed);
    }
}
