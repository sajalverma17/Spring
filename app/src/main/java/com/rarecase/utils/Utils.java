package com.rarecase.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import java.nio.ByteBuffer;

public class Utils {

    /**
     * Adds ID3 tags to .mp3 files decrypted/downloaded
     * as a result of a Spring action performed successfully
     *
     * The untagged files just dercypted/downloaded are saved
     * in storage directory with the name {song}-{pid}.mp3
     *
     * @param song The object containing info to be tagged
     * @param albumArt The bitmap to be added as album art of the audio file of song.
     * @param downloadedFilePath The URL to the untagged file downloaded
     */
    public static void tagAudioFile(Song song, @Nullable Bitmap albumArt,String downloadedFilePath){
        File springActionFile = new File(downloadedFilePath);

        ImageData imageData = null;
        if(albumArt != null){
            int imageSize = albumArt.getRowBytes() * albumArt.getHeight();
            ByteBuffer buffer = ByteBuffer.allocate(imageSize);
            albumArt.copyPixelsToBuffer(buffer);
            buffer.rewind();
            imageData = new ImageData(buffer.array(),"","",3);
        }

        try {
            MyID3 id3 = new MyID3();
            MusicMetadataSet musicMetadataSet = id3.read(springActionFile);
            MusicMetadata musicMetadata =  musicMetadataSet.id3v2Clean;
            //Decrypted file will have null musicMetaData
            if(musicMetadata == null){
                musicMetadata = MusicMetadata.createEmptyMetadata();
            }

            musicMetadata.setSongTitle(song.getSong());
            musicMetadata.setAlbum(song.getAlbum());
            String artists = song.getFeatured_artists().equals("") ? song.getPrimary_artists() : song.getPrimary_artists()+" ft. "+song.getFeatured_artists();
            musicMetadata.setArtist(artists);

            if(imageData != null) {
                musicMetadata.addPicture(imageData); //TODO : Doesn't work. Open issue. Probably need to use a diff ID3 tag library
            }

            id3.update(springActionFile,musicMetadataSet,musicMetadata);
        } catch (IOException | ID3WriteException e) {
            Log.i("Utils","Error tagging "+song.getSong()+" at "+downloadedFilePath);
        }

    }

    /**
     * songName must not contain any special characters like '/'.
     * Use the curateSongName method to form a proper file name for file system
     */
    public static String curateSongName(String originalName){
            return originalName.replaceAll("/","-");
    }

    public static boolean isOnline(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null){
            if(networkInfo.isConnected()){
                return true;
            }
        }
            return false;
    }

    public static void showToastFromService(Handler uiHandler, final Context context, final String message){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
            }
        });
    }
}
