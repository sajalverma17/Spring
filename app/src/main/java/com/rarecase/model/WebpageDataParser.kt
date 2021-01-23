package com.rarecase.model

import android.util.Log
import com.google.gson.JsonElement
import com.rarecase.model.json.WebpageJsonParser
import com.rarecase.utils.Utils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

class WebpageDataParser(url: String) {
    private val _webpage : Document
    private val _url : String

    init {
        _url = url
        _webpage = Jsoup.connect(url).get()
    }

    fun getSongJsonElements() : List<JsonElement> {

        /*
        <script> in <body> contains the json where we find pids.
        */

        val pageBody: Element = _webpage.getElementsByTag("body").get(0)
        val scriptTagInBody = pageBody.getElementsByTag("script")[0]
        val scriptContent = scriptTagInBody.html()
        val pageJson = scriptContent.split("=")[1].trim()

        /*
        Pids can be in different places in the JSON based on the page type (song/album/playlist)
        */
        var pagetype : PageType = PageType.Song
        when {
            _url.startsWith("https://www.jiosaavn.com/song/") -> {
                // Song json, there should be a song json element
                pagetype = PageType.Song
            }
            _url.startsWith(("https://www.jiosaavn.com/album")) -> {
                pagetype = PageType.Album
            }
            _url.startsWith(("https://www.jiosaavn.com/featured")) -> {
                pagetype = PageType.Featured
            }
        }

        return WebpageJsonParser(pagetype).getSongJsonElements(sanitiseJson(pageJson))
    }

    private fun sanitiseJson(pageJson : String) : String {

        val dateTimeSanitized = Utils.RegexReplaceGroup(pageJson, "(new Date\\(\"[0-9A-Za-z.:-]+?\"\\))", "\"JUNK\"")
        val sanitized = Utils.RegexReplaceGroup(dateTimeSanitized, "(:undefined)", ":\"JUNK\"")
        Log.i("WebpageDataParser", "Sanitised Json extracted from url to scrape:$sanitized")
        return sanitized
    }
}

enum class PageType {
    Song,
    Album,

    // Featured playlist for a user
    Featured
}