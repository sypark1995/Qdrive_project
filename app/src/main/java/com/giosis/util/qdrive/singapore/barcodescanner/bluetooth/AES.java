package com.giosis.util.qdrive.singapore.barcodescanner.bluetooth;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String TAG = "AES";
    private static byte[] in = new byte[16];
    private static byte[] out = new byte[16];
    private Cipher decryptCipher;


    public AES() {
        Log.d(TAG, "AES");

        try {

            String stringKey = "KDCKoamKDCTacKDC";
            SecretKeySpec sks = new SecretKeySpec(stringKey.getBytes(), "AES");

            decryptCipher = Cipher.getInstance("AES/ECB/ZeroBytePadding"); // Change to CBC and use appropriate IV
            decryptCipher.init(Cipher.DECRYPT_MODE, sks);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            Log.d(TAG, "<init>", e);
        }
    }

    public byte[] decrypt() {

        try {
            // Decrypt the bytes, returning decrypted bytes
            return decryptCipher.doFinal(in);
        } catch (Exception e) {
            Log.d(TAG, "decrypt", e);
        }

        return null;
    }

    public int DecryptData(int length) {
        Log.d(TAG, "Decryption is started...");

        String tmp = "";
        String msr = new String("<M/S|R]");

        int i, j, offset = 0, doffset = 0, dataend = 0;

        int loopcnt = (length / 16) + 1;

        // Copy the Key and PlainText
        for (i = 0; i < loopcnt; i++) {
            for (j = 0; j < 16; j++) {
                if (offset < length) in[j] = KTSyncData.BarcodeBuffer[offset++];
                else in[j] = 0;
            }

            out = decrypt();

            tmp += new String(out);
            dataend = tmp.indexOf(msr);

            Log.d(TAG, tmp);

            if (dataend != -1) break;
        }
        if (dataend != -1) {
            byte[] result = tmp.getBytes();
            for (j = 0; j < dataend; j++) KTSyncData.BarcodeBuffer[doffset++] = result[j];
        }
        return doffset;
    }
}
