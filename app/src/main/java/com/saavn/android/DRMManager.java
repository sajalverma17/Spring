package com.saavn.android;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * The key.
 */

public class DRMManager {

    private DRMManager(){
        try {
            System.loadLibrary("ndkdrm");
            Log.i("DRMManager", "NDK Library loaded");
        }catch (Exception e){
            Log.i("DRMManager","Error loading NDK Library");
        }
    }

    public static String decryptMediaURL(String str) {
        String str2 = "1.0";
        try {
            //byte[] decode = R == Base64Exists.YES ? Base64.decode(str, 0) : d.a(str, 0);
            byte[] decode = Base64.decode(str,0);
            Cipher aG = null;
            if (aG == null) {

                aG = Cipher.getInstance("DES/ECB/PKCS5Padding");
                aG.init(2, new SecretKeySpec("38346591".getBytes(), "DES"));
            }
            return new String(aG.doFinal(decode));
        } catch (Exception e) {
            Log.i("DRMManager: ","Error decrypting media Url"+e.getMessage());
            return str2;
        }
    }
}