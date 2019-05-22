package com.rarecase.model.remote;


import android.os.AsyncTask;
import android.util.Log;

import com.rarecase.model.Song;
import com.rarecase.model.SongJsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Class to scrape web page asynchronously.
 */

public class Scraper extends Observable{

    private String _url;

    /**
     * Set the URL
     * @param sharedUrl The exact URL extracted in presenter from shared string extra.
     */
    public Scraper(String sharedUrl) {
        _url = sharedUrl;
    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
    }

    /**
     * Gets the pids from webpage and notifies the SongListPresenter in the update method
     * In update, presenter loads song details using pids and displays the list in view
     */
    public void scrapeForPidsAsync() {

        new AsyncTask<String, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(String... params) {
                List<String> pids;
                Log.i("Scraper",_url);
                try {
                    Document webpage = Jsoup.connect(_url).get();

                    Elements songJsonElements = new Elements();

                    if(_url.contains("www.saavn.com/s/song/")){
                        songJsonElements.add(webpage.getElementsByClass("hide song-json").first());
                    }else {
                        songJsonElements = webpage.getElementsByClass("hide song-json");
                    }

                    List<Song> tempList = new ArrayList<Song>();
                    for (Element songJson :
                            songJsonElements) {
                        SongJsonParser jsonParser = new SongJsonParser();
                        Song song = jsonParser.getSong(songJson.text());
                        tempList.add(song);
                    }

                    pids = new ArrayList<>();
                    for (Song s :
                            tempList) {
                        pids.add(s.getId());
                    }
                    Log.i("Scraper",pids.toString());
                } catch (Exception e) {
                    Log.i("Scraper:", "Exception parsing using JSoup"+e.getMessage());
                    return null;
                }
                return pids;
            }
            @Override
            protected void onPostExecute(List<String> pids){
                Log.i("Scraper","Output from web scraping: "+pids);
                setChanged();
                if(pids == null) {
                    notifyObservers("UNK");
                }
                else {
                    notifyObservers(pids);
                }
            }
        }.execute();
    }
}
