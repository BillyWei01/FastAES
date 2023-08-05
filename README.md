# FastKV
[![Maven Central](https://img.shields.io/maven-central/v/io.github.billywei01/fastaes)](https://search.maven.org/artifact/io.github.billywei01/fastaes)

## 1. 概述
FastAES是基于Android平台的AES快速加解密库。<br>

加解密核心部分来源于：OPENSSL <br>
[https://github.com/openssl/openssl/blob/master/crypto/aes/aes_core.c](https://github.com/openssl/openssl/blob/master/crypto/aes/aes_core.c)

OPENSSL提供了几种AES的实现，本项目取其中的查表的实现，并封装了AES/CBC/PKCS7Padding模式。<br>
和Android SDK的AES实现对比，要快一个数量级。<br>
同时测试了一下KeyStore提供的AES加解密，慢的出奇。

下面是测试结果（测试设备，HUAWEI P30 Pro)：

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
    implementation 'io.github.billywei01:fastaes:1.0.1'
}
```

### 2.2 加解密
```java
    byte[] cipher = FastAES.encrypt(data, key, iv);
    byte[] plain = FastAES.decrypt(cipher, key, iv);
```


## License
See the [LICENSE](LICENSE) file for license rights and limitations.



