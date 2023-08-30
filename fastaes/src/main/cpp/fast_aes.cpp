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
    // Java层其实校验了参数，这里再check一下
    if (input == nullptr || key == nullptr || iv == nullptr) {
        throwIllegalArgumentException(env, "Illegal argument");
        return nullptr;
    }

    int inputLen = env->GetArrayLength(input);
    int keyLen = env->GetArrayLength(key);

    if ((keyLen != 16 && keyLen != 32)) {
        throwIllegalArgumentException(env, "Illegal argument");
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

    ByteArray result;
    if (is_encrypt) {
        result = aes_cbc_encrypt(&aesKey, (uint8_t *) p_iv, &content, out_buffer);
    } else {
        result = aes_cbc_decrypt(&aesKey, (uint8_t *) p_iv, &content, out_buffer);
    }

    if (p_input != in_buffer) {
        env->ReleaseByteArrayElements(input, p_input, 0);
    }

    if (result.value != nullptr) {
        jbyteArray output = env->NewByteArray(result.len);
        env->SetByteArrayRegion(output, 0, result.len, (jbyte *) result.value);
        if (result.value != out_buffer) {
            free(result.value);
        }
        return output;
    } else {
        // 加解密失败，除了解密可能会Bad Padding之外，就剩下内存不足了
        if (is_encrypt) {
            // 内存不足，加密失败
            throwIllegalStateException(env, "Encrypt failed");
        } else {
            if (result.len == 0) {
                // 内存不足，解密失败
                throwIllegalStateException(env, "Decrypt failed");
            } else {
                // input的padding模式不是PKCS7Padding
                env->ThrowNew(env->FindClass("javax/crypto/BadPaddingException"), "Bad padding");
            }
        }
        return nullptr;
    }
}