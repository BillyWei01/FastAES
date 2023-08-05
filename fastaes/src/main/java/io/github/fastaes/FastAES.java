package io.github.fastaes;

public class FastAES {
    static {
        System.loadLibrary("fastaes");
    }

    /**
     * Encrypt by AES/CBC/PKCS7Padding
     *
     * @param input Plain text
     * @param key The key to encrypt, must be length of 16 or 32 (AES 128/256)
     * @param iv  The initialization vector, must be length of 16,
     *            If the iv is null, e
     * @return encrypted text
     * @throws IllegalArgumentException If the the key is null or the key length is not 16 or 32
     * @throws IllegalStateException    If there's not enough memory
     */
    public static byte[] encrypt(byte[] input, byte[] key, byte[] iv) {
        if (iv == null) {
            throw new IllegalArgumentException("IV must be specified in CBC mode");
        }
        return crypt(input, key, iv, true);
    }

    /**
     * Decrypt by AES/CBC/PKCS7Padding
     *
     * @param input Encrypted text
     * @param key The key to decrypt, must be length of 16 or 32 (AES 128/256)
     * @param iv  The initialization vector, must be length of 16
     * @return Plain text
     * @throws IllegalArgumentException If the the key is null or the key/iv length is not 16 or 32,
     *                                  or the key/iv failed to decrypt the encrypted text
     * @throws IllegalStateException    If there's not enough memory.
     */
    public static byte[] decrypt(byte[] input, byte[] key, byte[] iv) {
        if (iv == null) {
            throw new IllegalArgumentException("IV must be specified in CBC mode");
        }
        return crypt(input, key, iv, false);
    }

    private native static byte[] crypt(byte[] input, byte[] key, byte[] iv, boolean isEncrypt);
}
