package com.rarecase.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.rarecase.model.Song;

/*
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
*/
import org.cmc.music.myid3.ID3Tag;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24FieldKey;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
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

    public static void tagAudioFile(Song song, @Nullable Bitmap albumArt, File downloadedFile) {

        ImageData imageData = null;
        if (albumArt != null) {
            int imageSize = albumArt.getRowBytes() * albumArt.getHeight();
            ByteBuffer buffer = ByteBuffer.allocate(imageSize);
            albumArt.copyPixelsToBuffer(buffer);
            buffer.rewind();
            imageData = new ImageData(buffer.array(), "image/png", "AlbumArt", 3);
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
                musicMetadata.addPicture(imageData); // Doesn't work (Probably due to original file being MP4 encoded). Won't fix.
            }

            id3.update(downloadedFile, musicMetadataSet, musicMetadata);
        } catch (IOException | ID3WriteException e) {
            Log.i("Utils", "Error tagging " + song.getSong() + " at " + downloadedFile.getAbsolutePath() + "\n" + e.getMessage());
        }

    }
     */

    public static boolean tagAudioFileJAudioTagger(Song song, @Nullable Bitmap albumArt, File downloadedFile)
    {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.readAs(downloadedFile, "mp4");
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
            return false;
        }

        Tag id4Tag = audioFile.getTagAndConvertOrCreateAndSetDefault();

        try {
            id4Tag.setField(FieldKey.TITLE, song.getSong());
            id4Tag.setField(FieldKey.ALBUM, song.getAlbum());
            String artists = song.getFeatured_artists().equals("") ? song.getPrimary_artists() : song.getPrimary_artists() + " ft. " + song.getFeatured_artists();
            id4Tag.setField(FieldKey.ARTIST, artists);
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }

        audioFile.setTag(id4Tag);

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
