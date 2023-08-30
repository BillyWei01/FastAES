package io.github.fastaes;

import javax.crypto.BadPaddingException;

public class FastAES {
    static {
        System.loadLibrary("fastaes");
    }

    /**
     * Encrypt by AES/CBC/PKCS7Padding
     *
     * @param input Plain text
     * @param key   The key to encrypt, must be length of 16 or 32 (AES 128/256)
     * @param iv    The initialization vector, must be length of 16,
     *              If the iv is null, e
     * @return encrypted text
     * @throws IllegalArgumentException If the the key is null or the key length is not 16 or 32
     * @throws IllegalStateException    If there's not enough memory
     */
    public static byte[] encrypt(byte[] input, byte[] key, byte[] iv) {
        if (input == null) {
            return null;
        }
        checkParam(key, iv);
        return crypt(input, key, iv, true);
    }

    /**
     * Decrypt by AES/CBC/PKCS7Padding
     *
     * @param input Encrypted text
     * @param key   The key to decrypt, must be length of 16 or 32 (AES 128/256)
     * @param iv    The initialization vector, must be length of 16
     * @return Plain text
     * @throws IllegalArgumentException If the the key is null or the key/iv length is not 16 or 32.
     * @throws IllegalStateException    If there's not enough memory.
     * @throws BadPaddingException      If the padding of input is not PKCS7Padding,
     *                                  (may be wrong padding or invalid input)
     */
    public static byte[] decrypt(byte[] input, byte[] key, byte[] iv) throws BadPaddingException {
        if (input == null) {
            return null;
        }
        int inputLen = input.length;
        if (inputLen < 16 || (inputLen & 15) != 0) {
            throw new IllegalArgumentException("Illegal block size");
        }
        checkParam(key, iv);
        return crypt(input, key, iv, false);
    }

    private static void checkParam(byte[] key, byte[] iv) {
        if (key == null) {
            throw new IllegalArgumentException("key can't nbe null");
        }
        if (key.length != 16 && key.length != 32) {
            throw new IllegalArgumentException("Only support the key with 16/32 bytes");
        }
        if (iv == null) {
            throw new IllegalArgumentException("iv can't be null");
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("iv's length must be 16");
        }
    }

    private native static byte[] crypt(byte[] input, byte[] key, byte[] iv, boolean isEncrypt);
}
