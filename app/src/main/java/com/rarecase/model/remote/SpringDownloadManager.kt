package com.rarecase.model.remote

import android.app.DownloadManager

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.rarecase.model.Song
import com.rarecase.model.SongCacheManager
import com.rarecase.utils.Utils
import java.io.File

/**
 * Using Android's DownloadManager.
 */


class SpringDownloadManager(val context: Context, val song : Song){

    fun isDownloadInProgress() : Boolean{
        val cacheManager = SongCacheManager(context)
        return cacheManager.getCachedFromDownloadingFolder(song.id) != null
    }

    fun enqueueSongDownload(mediaUrl : String, song: Song, destination : String){

        val httpsMediaUrl = mediaUrl.replace("http://","https://") //Android Pie restriction: use https instead of cleartext HTTP URL.
        Log.i("SpringDownloadManager: ","Replaced http with https. Media URL:"+httpsMediaUrl)

        val request = DownloadManager.Request(Uri.parse(httpsMediaUrl))
        request.setTitle(song.song)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDescription(song.id)
        //With scoped storage, this file should be accessible with Uri: content://media/audio/Spring/songName-songId.mp3
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "Spring"+File.separator+Utils.contentUriFileName(song))
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        val cacheManager = SongCacheManager(context)
        cacheManager.cacheSongToDownloadingFolder(song)
    }


}


