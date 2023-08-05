

#ifndef AES_CBC_H
#define AES_CBC_H

#include "array.h"

#ifdef __cplusplus
extern "C" {
#endif

#define BUFFER_SIZE 512

ByteArray aes_cbc_encrypt(ByteArray *key, uint8_t *iv, ByteArray *plain, uint8_t* out_buffer);

ByteArray aes_cbc_decrypt(ByteArray *key, uint8_t *iv, ByteArray *cipher, uint8_t* out_buffer);

#ifdef __cplusplus
}
#endif

#endif //AES_CBC_H
