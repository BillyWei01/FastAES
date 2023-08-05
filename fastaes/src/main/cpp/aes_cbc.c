
#include <stdlib.h>
#include <string.h>
#include "aes.h"
#include "aes_cbc.h"

ByteArray aes_cbc_encrypt(ByteArray *key, uint8_t *iv, ByteArray *plain, uint8_t *out_buffer) {
    uint8_t *plaintext = plain->value;
    int len = plain->len;

    AES_KEY extended_key;
    AES_set_encrypt_key(key->value, key->len << 3, &extended_key);

    int padding = AES_BLOCK_SIZE - (len & 0xF);
    int cipher_len = len + padding;

    uint8_t *ciphertext = cipher_len <= BUFFER_SIZE ? out_buffer : (uint8_t *) malloc(cipher_len);
    if (ciphertext != NULL) {
        memcpy(ciphertext, plaintext, len);
        memset(ciphertext + len, padding, padding);
        uint8_t *end = ciphertext + cipher_len;
        for (uint8_t *p = ciphertext; p < end; p += AES_BLOCK_SIZE) {
            uint64_t *p_text = (uint64_t *) p;
            uint64_t *p_iv = (uint64_t *) iv;
            p_text[0] ^= p_iv[0];
            p_text[1] ^= p_iv[1];
            iv = p;
            AES_encrypt(p, p, &extended_key);
        }
    }
    ByteArray result;
    result.value = ciphertext;
    result.len = cipher_len;
    return result;
}

ByteArray aes_cbc_decrypt(ByteArray *key, uint8_t *iv, ByteArray *cipher, uint8_t *out_buffer) {
    uint8_t *ciphertext = cipher->value;
    int len = cipher->len;
    uint8_t *plaintext;
    if (len < AES_BLOCK_SIZE ||
        ((len & (AES_BLOCK_SIZE - 1)) != 0) ||
        (plaintext = len <= 512 ? out_buffer : (uint8_t *) malloc(len)
        ) == NULL
            ) {
        // Invalid block or out of memory
        ByteArray result;
        result.value = NULL;
        result.len = 0;
        return result;
    }

    AES_KEY extended_key;
    AES_set_decrypt_key(key->value, key->len << 3, &extended_key);

    uint8_t *end = ciphertext + len;
    for (uint8_t *p = ciphertext; p < end; p += AES_BLOCK_SIZE) {
        uint8_t *pt = plaintext + (p - ciphertext);
        AES_decrypt(p, pt, &extended_key);
        uint64_t *p_text = (uint64_t *) pt;
        uint64_t *p_iv = (uint64_t *) iv;
        p_text[0] ^= p_iv[0];
        p_text[1] ^= p_iv[1];
        iv = p;
    }

    int padding = plaintext[len - 1] & 0xFF;
    ByteArray result;
    if (padding >= 1 && padding <= 16) {
        // unnecessary to compare last byte
        end = plaintext + len - 1;
        uint8_t *p = plaintext + len - padding;
        while (p < end && *p == padding) {
            p++;
        }
        if (p == end) {
            // valid padding
            result.value = plaintext;
            result.len = len - padding;
            return result;
        }
    }

    // bad padding
    if(plaintext != out_buffer){
        free(plaintext);
    }
    result.value = NULL;
    result.len = -1;
    return result;
}