# FastAES
[![Maven Central](https://img.shields.io/maven-central/v/io.github.billywei01/fastaes)](https://search.maven.org/artifact/io.github.billywei01/fastaes)

## 1. 概述
FastAES是基于Android平台的AES加解密实现。<br>

其加解密速度比Android SDK提供的实现要快一个数量级。

加解密核心部分来源于：OPENSSL <br>
[https://github.com/openssl/openssl/blob/master/crypto/aes/aes_core.c](https://github.com/openssl/openssl/blob/master/crypto/aes/aes_core.c)

OPENSSL提供了几种AES的实现，本项目取其中的查表的实现，并封装了AES/CBC/PKCS7Padding模式。<br>
和Android SDK的AES实现对比，要快一个数量级。<br>
同时测试了一下KeyStore提供的AES加解密，慢的离谱。

下面是测试情况。<br>
测试数据：1000个长度在100字节以内的随机数组。<br>
测试设备：HUAWEI P30 Pro。<br>
测试结果：

|              | 耗时(ms) 
--------------|---
FastAES      | 1  
SDK AES      | 24 
KeyStore AES | 20036


额外提一下，如果要使用KeyStore，建议选用其HMAC算法（Android 6.0以上可用）加密一段固定数组，所的结果作为AES的key。<br>
再用FastAES来加解密。
   
## 2. 使用方法

### 2.1 导入

```gradle
dependencies {
    implementation 'io.github.billywei01:fastaes:1.1.2'
}
```

目前aar包只编译了 'armeabi-v7a', 'arm64-v8a' 两种abi架构。 <br>
如果需更多架构可以自行下载源码编译，或者联系我添加。

### 2.2 使用
```java
    byte[] cipher = FastAES.encrypt(data, key, iv);
    byte[] plain = FastAES.decrypt(cipher, key, iv);
```

## 3. 相关链接
博客：
https://juejin.cn/spost/7263784662698754103

用例：
[FastKV-AESCipher](https://github.com/BillyWei01/FastKV/blob/main/app/src/main/java/io/fastkv/fastkvdemo/fastkv/cipher/AESCipher.java)

更多加密的实现：
[EasyCipher](https://github.com/BillyWei01/EasyCipher)

## License
See the [LICENSE](LICENSE) file for license rights and limitations.



