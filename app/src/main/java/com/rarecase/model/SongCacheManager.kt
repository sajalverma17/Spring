package com.rarecase.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.util.*

/**
 * Class to saved and fetch song details as cached objects
 */

class SongCacheManager(val context : Context){

    //Function for caching individual songs is only for songs which currently being downloaded
    /*
    Caches the song in the /downloading directory only.
     */
    fun cacheSong(song : Song){
        val cachePath = setUpCache(PidType.Downloading,false)
        writeToCache(song,cachePath + song.id + ".md")
    }

    /*
    Fetches the song from the /downloading directory.
     Returns null if the song is not found
     */
    fun getCachedSong(songId : String) : Song?{
        val cachePath = setUpCache(PidType.Downloading,false)
        return  readFromCache(cachePath + songId + ".md")
    }

    /*
    Delete the song from the /downloading directory
     */
    fun deleteCache(songId : String) {
        val cachePath = setUpCache(PidType.Downloading,false)
        deleteFromCache(cachePath + songId + ".md")
    }


    fun cacheSongs(songs: List<Song>, pidType: PidType) {
        val cachePath = setUpCache(pidType,true)
        for (s in songs) {
            writeToCache(s, cachePath + s.id + ".md")
        }
    }


    fun getCachedSongs(pidType : PidType) : HashMap<String,Song?>{
        val dictionary : HashMap<String,Song?> = hashMapOf()

        //Get all songs of this pidType
        val cachePath = setUpCache(pidType,false)
        val cacheFolder = File(cachePath)
        if(cacheFolder.isDirectory){
            val pids = cacheFolder.list().copyOf()
            for (i in pids.indices){
                dictionary.put(pids[i],readFromCache(cachePath + pids[i]))
            }
        }
        return dictionary

    }

    fun cacheImage(album : String, albumArt : Bitmap){
            val cachePath = context.externalCacheDir?.path + "/albumArts"
            val cacheDir = File(cachePath)
            cacheDir.mkdir()

            val os = FileOutputStream("$cachePath/$album.png")
            albumArt.compress(Bitmap.CompressFormat.PNG,100,os)
    }

    fun getImageCache() : HashMap<String,Bitmap> {
        val cachePath = context.externalCacheDir?.path+ "/albumArts"
        val cacheDir = File(cachePath)
        cacheDir.mkdir()
        val hashMap = HashMap<String,Bitmap>()

        val files = cacheDir.listFiles()
        if(files != null){
            for (file in files){
                val bitmap = BitmapFactory.decodeFile(cachePath+"/"+file.name)
                hashMap.put(file.nameWithoutExtension,bitmap)
            }
        }
        return hashMap
    }

    fun getImage(album: String?) : Bitmap?{
        val cachePath = context.externalCacheDir?.path + "/albumArts"
        val cacheDir = File(cachePath)
        cacheDir.mkdir()
        val files = cacheDir.listFiles()

        var bitmap : Bitmap? = null
        if(files != null){
            for (file in files){
                if(file.nameWithoutExtension == album) {
                   bitmap = BitmapFactory.decodeFile(cachePath+"/"+file.name)
                }
            }
        }
        return  bitmap
    }





    private fun setUpCache(pidType: PidType,clearSharedCache : Boolean = true): String {
            var cachePath = ""
            if (pidType == PidType.Offline)
                cachePath = context.externalCacheDir?.path + "/offline"
            if (pidType == PidType.Shared)
                cachePath = context.externalCacheDir?.path + "/shared"
            if (pidType == PidType.Downloading)
                cachePath = context.externalCacheDir?.path + "/downloading"

            val cacheDir = File(cachePath)

            if(clearSharedCache) {
                    //Delete previously shared cache of song metadata
                    if (pidType == PidType.Shared && cacheDir.exists()) {
                        cacheDir.deleteRecursively()

                    //Delete download history when a new list of Songs are shared to Spring
                    val downloadedFolder = File(context.externalCacheDir?.path + "/downloaded")
                    if(downloadedFolder.exists() && downloadedFolder.isDirectory) {
                        downloadedFolder.deleteRecursively()
                    }

                    //Delete album Arts as well when new list of songs are shared to Spring
                    val imageCacheFolder = File(context.externalCacheDir?.path + "/albumArts")
                    if(imageCacheFolder.exists() && imageCacheFolder.isDirectory) {
                        imageCacheFolder.deleteRecursively()
                    }
                }
            }
            cacheDir.mkdir()
            return "$cachePath/"
    }

    private fun writeToCache(song: Song, cacheFilePath: String) {

            val cacheFile: File? = File(cacheFilePath)
            if (cacheFile != null) {
                if (!cacheFile.exists()) {
                    val fileOS = FileOutputStream(cacheFile)
                    val objectOS = ObjectOutputStream(fileOS)
                    objectOS.writeObject(song)
                    objectOS.flush()
                    objectOS.close()
                }
            }
    }

    private fun readFromCache(cacheFilePath: String): Song? {
        val file = File(cacheFilePath)
        var songDetailsObj: Song? = null
        if (file.exists() && file.isFile) {
                val fileIS = FileInputStream(file)
                val objectIS = ObjectInputStream(fileIS)
                songDetailsObj = objectIS.readObject() as Song
            }
            return songDetailsObj
    }

    private fun deleteFromCache(cacheFilePath: String) {
        val file = File(cacheFilePath)
        if(file.exists() && file.isFile){
            file.delete()
        }
    }




}
