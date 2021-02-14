package com.rarecase.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.rarecase.model.Song;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Adds ID3 tags to .mp3 files decrypted/downloaded
     * as a result of a Spring action performed successfully
     * <p>
     * The untagged files just dercypted/downloaded are saved
     * in storage directory with the name {song}-{pid}.mp3
     *
     * @param song           The object containing info to be tagged
     * @param albumArt       The bitmap to be added as album art of the audio file of song.
     * @param downloadedFile The Uri to the untagged file downloaded. This will be file:// URI on android 9 and lower.
     */
    public static void tagAudioFile(Song song, @Nullable Bitmap albumArt, File downloadedFile) {

        ImageData imageData = null;
        if (albumArt != null) {
            int imageSize = albumArt.getRowBytes() * albumArt.getHeight();
            ByteBuffer buffer = ByteBuffer.allocate(imageSize);
            albumArt.copyPixelsToBuffer(buffer);
            buffer.rewind();
            imageData = new ImageData(buffer.array(), "", "", 3);
        }

        try {
            MyID3 id3 = new MyID3();
            MusicMetadataSet musicMetadataSet = id3.read(downloadedFile);
            MusicMetadata musicMetadata = musicMetadataSet.id3v2Clean;
            //Decrypted file will have null musicMetaData
            if (musicMetadata == null) {
                musicMetadata = MusicMetadata.createEmptyMetadata();
            }

            musicMetadata.setSongTitle(song.getSong());
            musicMetadata.setAlbum(song.getAlbum());
            String artists = song.getFeatured_artists().equals("") ? song.getPrimary_artists() : song.getPrimary_artists() + " ft. " + song.getFeatured_artists();
            musicMetadata.setArtist(artists);

            if (imageData != null) {
                musicMetadata.addPicture(imageData); // Doesn't work. Won't fix.
            }

            id3.update(downloadedFile, musicMetadataSet, musicMetadata);
        } catch (IOException | ID3WriteException e) {
            Log.i("Utils", "Error tagging " + song.getSong() + " at " + downloadedFile.getAbsolutePath() + "\n" + e.getMessage());
        }

    }

    public static void showToastFromService(Handler uiHandler, final Context context, final String message) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static String RegexReplaceGroup(String targetString, String regexGroupPattern, String replaceValue) {

        // Remove all -> {new Date("some date time representation")} that I found
        Pattern pattern = Pattern.compile(regexGroupPattern);
        Matcher matcher = pattern.matcher(targetString);

        String sanitizedString = targetString;

        if (matcher.find()) {

            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount; i++) {
                String match = matcher.group(i);
                if (match != null) {
                    sanitizedString = matcher.replaceAll(replaceValue);
                }
            }
        }
        return sanitizedString;
    }

    public static String contentUriFileName(Song song) {
        return song.getSong() + "-" + song.getId();
    }

    /**
     * songName must not contain any special characters like '/'.
     * Use the curateSongName method to form a proper file name for file system
     */
    public static String curateSongName(String originalName) {
        return originalName.replaceAll("/", "-");
    }
}
