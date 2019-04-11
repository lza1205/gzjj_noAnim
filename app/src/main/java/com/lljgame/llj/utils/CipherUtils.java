package com.lljgame.llj.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Davia.Li on 2017-07-31.
 */

public class CipherUtils {

    public static String sha256(String src) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(src.getBytes());
            byte[] byteBuffer = messageDigest.digest();

            return Utils.byteArrayToHexString(byteBuffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha1(String src) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(src.getBytes());
            byte[] byteBuffer = messageDigest.digest();

            return Utils.byteArrayToHexString(byteBuffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] base64Decode(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }


    //AES解码，获取扫码时间
    public static String aesDecrypt(byte[] srcData, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key_spec = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(key, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv);

            byte[] original = cipher.doFinal(srcData);
            byte[] bytes = PKCS7UnPadding(original);

            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] PKCS7UnPadding(byte[] decrypted) {
        int pad = (int) decrypted[decrypted.length - 1];
        if (pad < 1 || pad > 32) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }
}


