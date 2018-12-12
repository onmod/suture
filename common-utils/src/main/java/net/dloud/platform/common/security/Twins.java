package net.dloud.platform.common.security;

import com.google.crypto.tink.BinaryKeysetReader;
import com.google.crypto.tink.BinaryKeysetWriter;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.HybridDecrypt;
import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.Mac;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.hybrid.HybridDecryptFactory;
import com.google.crypto.tink.hybrid.HybridEncryptFactory;
import com.google.crypto.tink.hybrid.HybridKeyTemplates;
import com.google.crypto.tink.mac.MacFactory;
import com.google.crypto.tink.mac.MacKeyTemplates;
import com.google.crypto.tink.subtle.Base64;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * 加密解密类
 *
 * @author QuDasheng
 * @create 2018-07-27 15:10
 **/
@Slf4j
public class Twins {
    private static final byte[] associate = new byte[]{116, 119, 105, 110, 115, 32, 105, 110, 32, 116, 105, 110, 107};

    static {
        try {
            TinkConfig.register();
        } catch (GeneralSecurityException e) {
            log.warn("twins init failed", e);
            throw new SecurityException("Twins模块初始化失败");
        }
    }

    /**
     * key部分
     */
    public static byte[] getEncKey() {
        try {
            return storeKey(KeysetHandle.generateNew(HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM));
        } catch (GeneralSecurityException e) {
            log.warn("twins get encrypt key failed", e);
            throw new SecurityException("获取加密密钥失败");
        }
    }

    public static byte[] getSignKey() {
        try {
            return storeKey(KeysetHandle.generateNew(MacKeyTemplates.HMAC_SHA256_128BITTAG));
        } catch (GeneralSecurityException e) {
            log.warn("twins get sign key failed", e);
            throw new SecurityException("获取签名密钥失败");
        }
    }

    private static byte[] storeKey(KeysetHandle keysetHandle) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            CleartextKeysetHandle.write(keysetHandle, BinaryKeysetWriter.withOutputStream(bos));
        } catch (IOException e) {
            log.warn("twins store key failed: ", e);
            throw new SecurityException(e.getMessage());
        }
        return bos.toByteArray();
    }

    private static KeysetHandle readKey(byte[] pwd) {
        try {
            return CleartextKeysetHandle.read(BinaryKeysetReader.withBytes(pwd));
        } catch (GeneralSecurityException | IOException e) {
            log.warn("twins read key failed", e);
            throw new SecurityException("读取密钥失败");
        }
    }

    public static byte[] sign(byte[] input, String pwd) {
        return sign(input, Base64.decode(pwd));
    }

    public static byte[] sign(byte[] input, byte[] pwd) {
        final KeysetHandle handle = readKey(pwd);
        try {
            final Mac primitive = MacFactory.getPrimitive(handle);
            return primitive.computeMac(input);
        } catch (GeneralSecurityException e) {
            log.warn("twins sign failed", e);
            throw new SecurityException("签名数据失败");
        }
    }

    public static boolean verify(byte[] input, byte[] origin, String pwd) {
        return verify(input, origin, Base64.decode(pwd));
    }

    public static boolean verify(byte[] input, byte[] origin, byte[] pwd) {
        final KeysetHandle handle = readKey(pwd);
        try {
            final Mac primitive = MacFactory.getPrimitive(handle);
            primitive.verifyMac(input, origin);
            return true;
        } catch (GeneralSecurityException e) {
            log.warn("twins encrypt failed", e);
            throw new SecurityException("读取密钥失败");
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static byte[] encrypt(byte[] input, String pwd) {
        return encrypt(input, Base64.decode(pwd));
    }

    public static byte[] encrypt(byte[] input, byte[] pwd) {
        final KeysetHandle handle = readKey(pwd);
        try {
            final HybridEncrypt primitive = HybridEncryptFactory.getPrimitive(handle.getPublicKeysetHandle());
            return primitive.encrypt(input, associate);
        } catch (GeneralSecurityException e) {
            log.warn("twins decrypt failed", e);
            throw new SecurityException("读取密钥失败");
        }
    }

    public static String encrypt(String input, byte[] pwd) {
        if (null == input || input.isEmpty()) {
            return "";
        }
        return Base64.encode(encrypt(input.getBytes(), pwd));
    }

    public static byte[] decrypt(byte[] input, String pwd) {
        return decrypt(input, Base64.decode(pwd));
    }

    public static byte[] decrypt(byte[] input, byte[] pwd) {
        final KeysetHandle handle = readKey(pwd);
        try {
            final HybridDecrypt primitive = HybridDecryptFactory.getPrimitive(handle);
            return primitive.decrypt(input, associate);
        } catch (GeneralSecurityException e) {
            log.warn("twins decrypt failed", e);
            throw new SecurityException("读取密钥失败");
        }
    }

    public static String decrypt(String input, byte[] pwd) {
        if (null == input || input.isEmpty()) {
            return "";
        }
        return new String(decrypt(Base64.decode(input), pwd));
    }
}
