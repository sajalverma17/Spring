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

        // What the content of script tag looks like:
        /*
        // WARNING: See the following for security issues around embedding JSON in HTML:
        // https://redux.js.org/recipes/server-rendering/#security-considerations
           window.__INITIAL_DATA__ = {"dragdrop":{"hasDropped":false,.......rest of the json with song details......}}
        */

        // So we split based on =
        val allDelimitedStrings = scriptContent.split("=")
        // Skip the element at 0 index which is "window.__INITIAL_DATA__"
        val jsonStrings = allDelimitedStrings.subList(1, allDelimitedStrings.size)
        // There can be more than one string elements after 0 index, as the json itself might contain an "=" inside it, so we concat them back
        val pageJson = jsonStrings.joinToString().trim()


        /*
        Pids can be in different places in the JSON based on the page type (song/album/featured).
        Playlist usually end up as named featured in the redirected URL
        */
        var pagetype : PageType = PageType.Unsupported
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

            else -> {
                throw UnknownItemSharedException("Shared something other than song/album/featured: $_url");
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
    Featured,
    Unsupported,
}