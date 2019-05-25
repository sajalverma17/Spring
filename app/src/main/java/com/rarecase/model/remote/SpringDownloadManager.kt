package com.rarecase.model.remote

import android.app.DownloadManager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rarecase.model.Song
import com.rarecase.model.SongCacheManager

/**
 * Using Android's DownloadManager.
 */


class SpringDownloadManager(val context: Context, val song : Song){

    fun isDownloadInProgress() : Boolean{
        val cacheManager = SongCacheManager(context)
        return cacheManager.getCachedSong(song.id) != null
    }

    fun enqueueSongDownload(mediaUrl : String, song: Song, destination : String){

        val httpsMediaUrl = mediaUrl.replace("http://","https://") //Android Pie restriction: use https instead of cleartext HTTP URL.
        Log.i("SpringDownloadManager: ","Replaced http with https. Media URL:"+httpsMediaUrl)
        val request = DownloadManager.Request(Uri.parse(httpsMediaUrl))
        request.setTitle(song.song)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.parse("file:$destination/"+ song.song +".mp3"))
        request.setDescription(song.id)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        val cacheManager = SongCacheManager(context)
        cacheManager.cacheSong(song)
    }


}


