package net.dloud.platform.common.security;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 摘要算法
 *
 * @author QuDasheng
 * @create 2017-01-27 09:32
 **/
@Slf4j
public class Digests {
    /**
     * MD5部分
     */
    public static String md5(byte[] data) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.warn("encryptMD5 failed: ", e);
            throw new SecurityException(e.getMessage());
        }
        md5.update(data);
        byte[] resultBytes = md5.digest();

        return bytesToHex(resultBytes);
    }

    public static String md5File(String path) {
        FileInputStream fis = null;
        DigestInputStream dis = null;
        try {
            fis = new FileInputStream(new File(path));
            dis = new DigestInputStream(fis, MessageDigest.getInstance("MD5"));

            byte[] buffer = new byte[1024];
            int read = dis.read(buffer, 0, 1024);
            while (read != -1) {
                read = dis.read(buffer, 0, 1024);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            log.warn("encryptMD5FromFile failed: ", e);
            throw new SecurityException(e.getMessage());
        }

        MessageDigest md = dis.getMessageDigest();
        byte[] resultBytes = md.digest();
        return bytesToHex(resultBytes);
    }

    /**
     * SHA1部分
     */
    public static String sha1(byte[] data) {
        return encryptSHA(data, "SHA-1");
    }

    /**
     * SHA256部分
     */
    public static String sha256(byte[] data) {
        return encryptSHA(data, "SHA-256");
    }

    /**
     * SHA512部分
     */
    public static String sha512(byte[] data) {
        return encryptSHA(data, "SHA-512");
    }

    private static String encryptSHA(byte[] data, String type) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            log.warn("encryptSHA failed: ", e);
            throw new SecurityException(e.getMessage());
        }
        sha.update(data);
        byte[] resultBytes = sha.digest();

        return bytesToHex(resultBytes);
    }

    private static String bytesToHex(byte[] input) {
        final StringBuilder builder = new StringBuilder();
        for (final byte b : input) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
