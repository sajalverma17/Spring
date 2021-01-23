package com.rarecase.model.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.rarecase.model.PageType
import java.lang.reflect.Type

class WebpageJsonDeserializer(pageType: PageType) : JsonDeserializer<List<JsonElement>> {

    private val _pageType : PageType

    init {
        _pageType = pageType
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): List<JsonElement> {
        val returnList : MutableList<JsonElement> = mutableListOf()
        val jsonObject = json?.asJsonObject

        when (_pageType) {
            PageType.Song -> {
                val songMetaMember = jsonObject?.getAsJsonObject("song")
                val songJsonElement = songMetaMember?.getAsJsonObject("song")
                if (songJsonElement != null) {
                    returnList.add(songJsonElement)
                }
            }
            PageType.Album -> {
                val albumMetaMember = jsonObject?.getAsJsonObject("albumView")
                val albumJson = albumMetaMember?.getAsJsonObject("album")
                val songsJsonArray = albumJson?.getAsJsonArray("songs")
                if (songsJsonArray != null) {
                    for (songJsonElement in songsJsonArray){
                        returnList.add(songJsonElement)
                    }
                }
            }
            PageType.Featured -> {
                val playlistMetaMember = jsonObject?.getAsJsonObject("playlist")
                val playlist = playlistMetaMember?.getAsJsonObject("playlist")
                val songJsonArray = playlist?.getAsJsonArray("list")
                if(songJsonArray != null){
                    for (songJsonElement in songJsonArray){
                        returnList.add(songJsonElement)
                    }
                }
            }
        }

     return returnList
    }


}