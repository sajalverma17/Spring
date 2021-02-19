package com.rarecase.spring

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import android.util.Log
import com.rarecase.model.Song
import com.rarecase.model.SongCacheManager
import com.rarecase.utils.Utils
import java.io.File
import java.io.InputStream
import java.net.URI

/**
 * Remove cache from /downloading folder, adds ID3 tags to file on disk.
 */
class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if(intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            if(context?.packageName == "com.rarecase.spring") {
                val downloadRef = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val songDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = (songDownloadManager).query(DownloadManager.Query().setFilterById(downloadRef))

                if (cursor.moveToFirst()) {
                    val colIndex_LocalUri: Int
                    val colIndex_Status: Int
                    val colIndex_Desc : Int
                    try {
                        colIndex_LocalUri = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
                        colIndex_Status = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        colIndex_Desc = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_DESCRIPTION)
                    } catch (e: IllegalArgumentException) {
                        return
                    }

                    val fileURIString = cursor.getString(colIndex_LocalUri)
                    val status = cursor.getInt(colIndex_Status)
                    val songId = cursor.getString(colIndex_Desc) //Set by us. Contains pid used to clean cache

                    val songDetails = getSongDetails(context,songId)
                    removeSongDetailsFromCache(context,songId)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.i("DownloadComplete", "Download of ${songDetails?.song} successful")

                        if(songDetails != null) {

                            // On >= Android Q, Add ID3 tags on temp file then overwrite downloaded file in Music MediaStore collection
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                val contentUri = getContentUri(context.contentResolver, songDetails)
                                tagMediaFile(context, contentUri, songDetails)
                            }

                            // On versions Android Pie and below, tag downloaded file directly (We have write access)
                            else {
                                val fileURI = URI.create(fileURIString)
                                Utils.tagAudioFileJAudioTagger(songDetails, songDetails.albumArt, File(fileURI))
                            }
                        }
                        Utils.showToastFromService(Handler(), context, "Finished downloading "+songDetails?.song)
                    }
                    else if (status == DownloadManager.STATUS_FAILED) {
                        Log.i("DownloadComplete", "Download of $downloadRef failed")
                        Utils.showToastFromService(Handler(), context, "Download failed: "+songId)
                    }
                }
                cursor.close()
            }
        }
    }
    private fun getSongDetails(context : Context?, pid : String) : Song? {
        val songCacheManager = SongCacheManager(context!!)
        val song = songCacheManager.getCachedFromDownloadingFolder(pid)
        song?.albumArt = songCacheManager.getImage(song?.album)
        return song
    }

    private fun removeSongDetailsFromCache(context: Context?, pid : String) {
        SongCacheManager(context!!).deleteCachedFromDownloadingFolder(pid)
    }

    // Android Q and above: Access file from audio collection
    private fun getContentUri(contentResolver: ContentResolver, song : Song) : Uri {
        val projection = arrayOf(
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.DATE_ADDED)
        var contentUri : Uri = Uri.EMPTY

        // Look for the most recent added downloaded file like this name.
        contentResolver
                .query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        MediaStore.Audio.AudioColumns.DISPLAY_NAME+" like '${Utils.contentUriFileName(song)}%'",
                        null,
                        "${MediaStore.Video.Media.DISPLAY_NAME} DESC")
                ?.use {
                    Log.i("Count:", ""+it.count)
                    val idColIndex = it.getColumnIndex(MediaStore.Audio.AudioColumns._ID)
                    if(it.moveToFirst()){
                        contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.getLong(idColIndex))
                    }
                }
        return contentUri
    }

    // Get the file from content URI. Copy it to app-specific file, and add metadata tags to app-specific file then overwrite Audio collection file.
    private fun tagMediaFile(context: Context, contentUri: Uri, songDetails: Song){
        val contentResolver = context.contentResolver
        val isPendingTrue = ContentValues().apply {
            this.put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        contentResolver.update(contentUri, isPendingTrue, null, null)

        val tempFile = File(context.externalCacheDir?.path + "/downloading/"+ songDetails.id+ ".mp3")
        contentResolver.openInputStream(contentUri)?.use {
            inputStream -> tempFile.copyInputStreamToFile(inputStream)
        }

        Utils.tagAudioFileJAudioTagger(songDetails, songDetails.albumArt, tempFile)

        contentResolver.openOutputStream(contentUri, "w")?.use { outputStream ->
            outputStream.write(tempFile.readBytes())
        }

        tempFile.delete()

        val isPendingFalse = ContentValues().apply {
            this.put(MediaStore.Audio.Media.IS_PENDING, 0)
        }
        contentResolver.update(contentUri, isPendingFalse, null, null)
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream?) {
        this.outputStream().use { fileOut ->
            inputStream?.copyTo(fileOut)
        }
    }

}