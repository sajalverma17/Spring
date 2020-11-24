package com.saavn.android;

import android.support.compat.*;
import android.util.Base64;
import android.util.Log;

import com.rarecase.utils.CachedPidsReader;

import java.util.List;

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
    public native int ndkdecrypt(String str,String str2,String str3);
    public native int ndkdecryptPartial(String str,String str2,String str3);

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

    /**
     * Decrypts the file whose name equals the pid passed as parameter.
     * @param pid The pid of song which needs to be decrypted.
     *            An encrypted file of this name must be present
     *            inside the cache directory.
     * @param fileName The name with which the audio file is created.
     * @param storagePath Path where the decrypted file will be saved.
     * @return 0 if decryption was successful. -1 if unsuccessful.
     */
    public static int decryptSongPartial(String pid,String fileName,String storagePath) {
        int decryptionResult = -1;
        DRMManager drmManager = new DRMManager();
        List<String> pidsFiles = CachedPidsReader.getCachedPidFileNames();
        for (String pidFile : pidsFiles) {
            if (pidFile.substring(0, 8).equals(pid)) {
                decryptionResult = drmManager.ndkdecryptPartial(CachedPidsReader.CACHE_PATH + pidFile, storagePath, fileName+".mp3");
                break;
            }
        }
        return decryptionResult;
    }

    /**
     * Fallback method for when decryptSongPartial method fails.
     * Yet to observe failure of decryptSongPartial method.
     * @param pid The pid of song which needs to be decrypted.
     *            An encrypted file of this name must be present
     *            inside the cache directory.
     * @param fileName The name with which the audio file is created.
     * @param storagePath Path where the decrypted file will be saved.
     * @return 0 if decryption was successful. -1 if unsuccessful.
     */
    public static int decryptSong(String pid,String fileName,String storagePath) {
        int decryptionResult = -1;
        DRMManager drmManager = new DRMManager();
        List<String> pidsFiles = CachedPidsReader.getCachedPidFileNames();
        for (String pidFile : pidsFiles) {
            if (pidFile.substring(0, 8).equals(pid)) {
                decryptionResult = drmManager.ndkdecrypt(CachedPidsReader.CACHE_PATH + pidFile, storagePath, fileName + ".mp3");
                break;
            }
        }
        return decryptionResult;
    }
}