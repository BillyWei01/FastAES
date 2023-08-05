#include <jni.h>
#include <string>

#include "aes_cbc.h"


void throwIllegalArgumentException(JNIEnv *env, const char *message) {
    env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), message);
}

void throwIllegalStateException(JNIEnv *env, const char *message) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), message);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_io_github_fastaes_FastAES_crypt(JNIEnv *env, jclass clazz, jbyteArray input, jbyteArray key,
                                     jbyteArray iv, jboolean is_encrypt) {
    if (key == nullptr) {
        throwIllegalArgumentException(env, "key is null");
        return nullptr;
    }
    int keyLen = env->GetArrayLength(key);
    if (keyLen != 16 && keyLen != 32) {
        throwIllegalArgumentException(env, "Only support the key with 16/32 bytes");
        return nullptr;
    }
    if (iv == nullptr || env->GetArrayLength(iv) != 16) {
        throwIllegalArgumentException(env, "iv's length must be 16");
        return nullptr;
    }

    if (input == nullptr) {
        return nullptr;
    }

    int inputLen = env->GetArrayLength(input);
    if (!is_encrypt && (inputLen < 16 || ((inputLen & 15) != 0))) {
        throwIllegalArgumentException(env, "Illegal block size");
        return nullptr;
    }

    jbyte p_key[32];
    jbyte p_iv[16];
    env->GetByteArrayRegion(key, 0, keyLen, p_key);
    env->GetByteArrayRegion(iv, 0, 16, p_iv);

    // 如果input比较短，可以用栈空间来计算，减少动态内存申请和回收
    jbyte in_buffer[BUFFER_SIZE];
    uint8_t out_buffer[BUFFER_SIZE];

    jbyte *p_input;
    if (inputLen <= BUFFER_SIZE) {
        env->GetByteArrayRegion(input, 0, inputLen, in_buffer);
        p_input = in_buffer;
    } else {
        p_input = env->GetByteArrayElements(input, JNI_FALSE);
        if (p_input == nullptr) {
            throwIllegalStateException(env, "Get params failed");
            return nullptr;
        }
    }

    ByteArray content;
    content.value = (uint8_t *) p_input;
    content.len = inputLen;

    ByteArray aesKey;
    aesKey.value = (uint8_t *) p_key;
    aesKey.len = keyLen;

    ByteArray cipher;
    if (is_encrypt) {
        cipher = aes_cbc_encrypt(&aesKey, (uint8_t *) p_iv, &content, out_buffer);
    } else {
        cipher = aes_cbc_decrypt(&aesKey, (uint8_t *) p_iv, &content, out_buffer);
    }

    if (p_input != in_buffer) {
        env->ReleaseByteArrayElements(input, p_input, 0);
    }

    if (cipher.value != nullptr) {
        jbyteArray result = env->NewByteArray(cipher.len);
        env->SetByteArrayRegion(result, 0, cipher.len, (jbyte *) cipher.value);
        if (cipher.value != out_buffer) {
            free(cipher.value);
        }
        return result;
    } else {
        if (is_encrypt) {
            throwIllegalStateException(env, "encrypt failed");
        } else {
            if (cipher.len == 0) {
                throwIllegalStateException(env, "decrypt failed");
            } else {
                throwIllegalArgumentException(env, "Bad padding");
            }
        }
        return nullptr;
    }
}