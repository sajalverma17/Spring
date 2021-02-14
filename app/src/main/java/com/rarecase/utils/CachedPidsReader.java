package com.rarecase.utils;

import android.os.Environment;
import androidx.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Static methods to read access cache directory by user.
 */
public class CachedPidsReader {
    public static final String CACHE_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.saavn.android/songs/";

    /**
     * Checks if internal memory (Android/data) can be read
     * @return true if permission to read internal storage
     */
    public static boolean canReadInternalStorage() {
        File cacheDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/");
        return cacheDir.canRead();
    }

    /**
     * Checks if one or more Pids exist in the cache directory
     * @return true if pid(s) are present
     */
    public static boolean pidsExist() {
        File cacheDir = new File(CACHE_PATH);
        if(cacheDir.exists()) {
            if(cacheDir.list().length > 0) {                //Songs present
                List<String> pids = getCachedPidFileNames();
                return pids.size() > 0;
            }
            else{                                           //Folder present but no songs in it
                return  false;
            }
        }
        else                                                //Folder not present
            return false;
    }

    /**
     * Gets the list of pids of songs saved offline.
     * @return Returns {@Code List<String>} of pids, null if unable to read directory
     */
    @Nullable
    public static List<String> getCachedPids(){

        File cacheDir = new File(CACHE_PATH);
        List<String> pidsList = new ArrayList<>();
        String[] pids = cacheDir.list();

        for (String pid: pids) {
            if (!pid.startsWith("curr")) {
                pid = pid.substring(0, 8);
                pidsList.add(pid);
            }
        }
        return pidsList;
    }

    /**
     * Gets the list of pids of songs saved offline with .mp3 or .mp4 extension
     * @return Returns {@Code List<String>} of pids, null if unable to read directory
     */
    public static List<String> getCachedPidFileNames(){
        File cacheDir = new File(CACHE_PATH);
        List<String> pidsList = new ArrayList<>();
        String[] pids = cacheDir.list();
        for (String pid : pids) {
            if (!pid.startsWith("curr")) {
                pidsList.add(pid);
            }
        }
        return pidsList;
    }
}
