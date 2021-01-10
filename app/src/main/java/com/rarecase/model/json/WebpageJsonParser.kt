package com.rarecase.model.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.rarecase.model.PageType
import java.lang.reflect.Type


/*
Parses a json string into a list of song-json elements. These song json elements will then be passed to
{SongJsonParser} to convert them into list of Song objects by scraper.
 */
class WebpageJsonParser(val pageType: PageType) {

    private val _pageType : PageType = pageType
    private val _songJsonElements : Type = object : TypeToken<List<JsonElement>>(){}.type

    fun getSongJsonElements(pageJson: String) : List<JsonElement> {
        val gsonBuilder = GsonBuilder()

        gsonBuilder.registerTypeAdapter(_songJsonElements, WebpageJsonDeserializer(_pageType))
        val gson = gsonBuilder.create()

        return gson.fromJson(pageJson, _songJsonElements)
    }
}