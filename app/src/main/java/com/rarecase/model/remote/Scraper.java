package com.rarecase.model.remote;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.util.MalformedJsonException;
import android.util.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.rarecase.model.Song;
import com.rarecase.model.UnknownItemSharedException;
import com.rarecase.model.json.SongJsonParser;
import com.rarecase.model.WebpageDataParser;
import com.rarecase.utils.HttpHelper;

import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Class to scrape web page asynchronously.
 *
 */

public class Scraper extends Observable{

    private String _url;

    public Scraper(String sharedURL) {
        _url = sharedURL;
    }


    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
    }

    @SuppressLint("StaticFieldLeak")
    public void getRedirectURL()
    {
        new AsyncTask<String, Void, URL>() {
            @Override
            protected URL doInBackground(String... params) {
                URL redirectURL = null;
                Log.i("Scraper","URL to redirect:"+_url);
                try {
                    String urlString = HttpHelper.getRedirectURL(_url);
                    redirectURL = new URL(urlString);
                } catch (Exception e) {
                    Log.i("Scraper:", "Exception getting redirect URL: "+e.getMessage());
                    return null;
                }
                return redirectURL;
            }

            @Override
            protected void onPostExecute(URL redirectedURL){
                Log.i("Scraper","Got redirected URL: "+redirectedURL);
                setChanged();
                if(redirectedURL == null) {
                    notifyObservers("UNK");
                }
                else {
                    notifyObservers(redirectedURL);
                }
            }
        }.execute();
    }

    /**
     * Gets the pids from web page and notifies the SongListPresenter in the update method
     * In update, presenter loads song details using pids and displays the list in view
     */
    @SuppressLint("StaticFieldLeak")
    public void scrapeForPidsAsync() {

        new AsyncTask<String, Void, Pair<String,List<String>>>() {
            @Override
            protected Pair<String, List<String>> doInBackground(String... params) {
                List<String> pids;
                List<JsonElement> songJsonElementsInPage;
                Log.i("Scraper", "URL to scrape for Pids:"+_url);

                try {
                    WebpageDataParser webPageParser = new WebpageDataParser(_url);
                    songJsonElementsInPage = webPageParser.getSongJsonElements();
                }
                catch (UnknownItemSharedException e){
                    Log.i("Scraper",""+e.getMessage());
                    return new Pair<>("UIS", null);
                }
                catch (JsonParseException e){
                    Log.i("Scraper", "Error de-serializing json of page: "+e.getMessage());
                    return new Pair<>("UNK", null);
                }

                try{
                    List<Song> tempList = new ArrayList<Song>();
                    for (JsonElement songJson : songJsonElementsInPage) {
                        SongJsonParser jsonParser = new SongJsonParser();
                        Song song = jsonParser.getSong(songJson.toString());
                        tempList.add(song);
                    }

                    pids = new ArrayList<>();
                    for (Song s : tempList) {
                        pids.add(s.getId());
                    }
                    Log.i("Scraper","Output from web scraping: "+pids.toString());
                }
                catch (JsonParseException e){
                    Log.i("Scraper", "Error de-serializing song json element:"+e.getMessage());
                    return new Pair<>("UNK", null);
                }

                return new Pair<>(null, pids);
            }

            @Override
            protected void onPostExecute(Pair<String, List<String>> errorOrPids){
                setChanged();
                if(errorOrPids.first != null) {
                    notifyObservers(errorOrPids.first);
                }
                else {
                    notifyObservers(errorOrPids.second);
                }
            }
        }.execute();
    }
}
