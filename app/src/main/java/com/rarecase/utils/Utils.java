package com.rarecase.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.rarecase.model.Song;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean tagAudioFile(Song song, @Nullable File albumArtFile, File downloadedFile)
    {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.readAs(downloadedFile, "mp4");
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
            return false;
        }
        Tag mp4Tag = audioFile.getTagAndConvertOrCreateAndSetDefault();

        // Write text fields
        try {
            mp4Tag.setField(FieldKey.TITLE, song.getSong());
            mp4Tag.setField(FieldKey.ALBUM, song.getAlbum());
            String artists = song.getFeatured_artists().equals("") ? song.getPrimary_artists() : song.getPrimary_artists() + " ft. " + song.getFeatured_artists();
            mp4Tag.setField(FieldKey.ARTIST, artists);
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }

        // Try writing image data, catch exception but continue committing to the file, we don't care that much about this.
        if(albumArtFile != null) {
            try {
                Artwork artwork = ArtworkFactory.createArtworkFromFile(albumArtFile);
                artwork.setMimeType("image/png");
                mp4Tag.setField(artwork);
            } catch (FieldDataInvalidException | IOException e) {
                e.printStackTrace();
            }
        }

        audioFile.setTag(mp4Tag);

        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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

    public static boolean hasWritePermission(Context context){
        PackageManager packageManager = context.getPackageManager();
        return packageManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
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
