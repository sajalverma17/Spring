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
        val returnList : List<JsonElement> = listOf()
        val jsonObject = json?.asJsonObject


     return returnList
    }


}