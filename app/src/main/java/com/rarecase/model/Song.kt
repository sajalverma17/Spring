package com.rarecase.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Song(var id : String,
                var song: String?="",
                var album: String?="",
                var primary_artists: String?="",
                var featured_artists: String?="",
                var image_url: String?="",
                var perma_url: String?="",
                var enc_media_url : String?="",
                var duration: String?="",
                var albumArt : Bitmap?=null
): Parcelable , Serializable {

    constructor(songid: String):this(id = songid)

    constructor(data : Parcel):this(id = data.readString()!!){
        //id = data?.readString()
        song = data.readString()
        album = data.readString()
        primary_artists = data.readString()
        featured_artists = data.readString()
        image_url = data.readString()
        perma_url= data.readString()
        enc_media_url = data.readString()
        duration = data.readString()
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Song> = object : Parcelable.Creator<Song> {
            /**
             * Create a new instance of the Parcelable class, instantiating it
             * from the given Parcel whose data had previously been written by
             * [Parcelable.writeToParcel()][Parcelable.writeToParcel].

             * @param source The Parcel to read the object's data from.
             * *
             * @return Returns a new instance of the Parcelable class.
             */

            override fun createFromParcel(source: Parcel): Song {
                return Song(source)
            }

            /**
             * Create a new array of the Parcelable class.

             * @param size Size of the array.
             * *
             * @return Returns an array of the Parcelable class, with every entry
             * * initialized to null.
             */
            override fun newArray(size: Int): Array<Song?> {
                val songList: Array<Song?> = arrayOfNulls(size)
                return songList
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(this.id)
        dest?.writeString(this.song)
        dest?.writeString(this.album)
        dest?.writeString(this.primary_artists)
        dest?.writeString(this.featured_artists)
        dest?.writeString(this.image_url)
        dest?.writeString(this.perma_url)
        dest?.writeString(this.enc_media_url)
        dest?.writeString(this.duration)
    }


}