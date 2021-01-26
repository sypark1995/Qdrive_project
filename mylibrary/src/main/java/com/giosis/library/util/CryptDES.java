package com.giosis.library.util;

import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptDES {

    public final static byte[] myIV = {10, 20, 30, 40, 50, 60, 70, 80};

    public static String encrypt(String data, String key) throws Exception {

        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(key))
            return "";
        try {
            if (key.length() > 8) {
                key = key.substring(0, 8);
            }
            byte[] desKeyData = key.getBytes();

            Cipher c3des = Cipher.getInstance("DES/CBC/PKCS7Padding");

            SecretKeySpec myKey = new SecretKeySpec(desKeyData, "DES");

            IvParameterSpec ivspec = new IvParameterSpec(myIV);

            c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);

            byte[] inputBytes1 = data.getBytes("UTF8");
            byte[] outputBytes1 = c3des.doFinal(inputBytes1);

            return Base64.encodeToString(outputBytes1, Base64.NO_WRAP | Base64.CRLF);
        } catch (Exception e) {
            throw new Exception("FAIL_ENCRYPT");
        }
    }

    public static String decrypt(String data, String key) throws Exception {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(key))
            return "";
        try {
            if (key.length() > 8) {
                key = key.substring(0, 8);
            }
            byte[] desKeyData = key.getBytes();

            Cipher c3des = Cipher.getInstance("DES/CBC/PKCS7Padding");

            SecretKeySpec myKey = new SecretKeySpec(desKeyData, "DES");

            IvParameterSpec ivspec = new IvParameterSpec(myIV);

            c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);

            byte[] inputBytes1 = Base64.decode(data, Base64.NO_WRAP | Base64.CRLF);
            byte[] outputBytes2 = c3des.doFinal(inputBytes1);

            return new String(outputBytes2, "UTF8");
        } catch (Exception e) {
            throw new Exception("FAIL_DECRYPT");
        }
    }
}
