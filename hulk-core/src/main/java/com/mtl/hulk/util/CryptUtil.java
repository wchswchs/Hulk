package com.mtl.hulk.util;

import com.google.common.base.Throwables;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptUtil {

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final int AES_BLOCK_LEN = 16;
    public static final int KCV_LEN = 3;

    public static byte[] hmacSha256(byte[] keyBytes, String data)   {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return  sha256_HMAC.doFinal(data.getBytes(CHARSET));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] aesEcbPkcs5PaddedCrypt(SecretKeySpec key, byte[] data, int mode) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(mode, key);
            return cipher.doFinal(data);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] aesEcbCrypt(SecretKeySpec key, byte[] data, int mode) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(mode, key);
            return cipher.doFinal(data);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] aesKcv(byte[] userKey) {
        byte[] data = new byte[AES_BLOCK_LEN];

        byte[] bytes = aesEcbCrypt(aesKey(userKey), data, Cipher.ENCRYPT_MODE);

        byte[] kcv = new byte[KCV_LEN];
        System.arraycopy(bytes, 0, kcv, 0, kcv.length);
        return kcv;
    }

    public static byte[] aesEncrypt(byte[] userKey, byte[] data) {
        return aesEcbCrypt(aesKey(userKey), data, Cipher.ENCRYPT_MODE);
    }

    public static byte[] aesDecrypt(byte[] userKey, byte[] data) {
        return aesEcbCrypt(aesKey(userKey), data, Cipher.DECRYPT_MODE);
    }

    public static byte[] aesEncryptP5Pad(byte[] userKey, byte[] data) {
        return aesEcbPkcs5PaddedCrypt(aesKey(userKey), data, Cipher.ENCRYPT_MODE);
    }

    public static byte[] aesDecryptP5Pad(byte[] userKey, byte[] data) {
        return aesEcbPkcs5PaddedCrypt(aesKey(userKey), data, Cipher.DECRYPT_MODE);
    }

    @NotNull
    private static SecretKeySpec aesKey(byte[] userKey) {
        return new SecretKeySpec(userKey, "AES");
    }

}
